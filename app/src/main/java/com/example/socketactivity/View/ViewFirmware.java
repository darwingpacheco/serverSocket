package com.example.socketactivity.View;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socketactivity.Extras.BatteryStatus;
import com.example.socketactivity.Presenter.InfoValidate;
import com.example.socketactivity.Presenter.PresenterFirmware;
import com.example.socketactivity.R;
import com.pos.device.SDKManager;
import com.pos.device.SDKManagerCallback;
import com.pos.device.sys.SystemManager;

public class ViewFirmware extends AppCompatActivity {

    public PresenterFirmware presenterFirmware;
    public BatteryStatus batteryStatus;
    public Context context;
    private AlertDialog alertDialog;
    public String inputDescription;
    public String inputIp;
    public int retVal;
    long totalBytesRead = 0;
    TextView txtMostrar, txtProgress;
    EditText edtData, edtDataIp;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtMostrar = findViewById(R.id.txtMostrar);
        edtData = findViewById(R.id.edtData);
        btnSend = findViewById(R.id.btnSend);
        edtDataIp = findViewById(R.id.edtDataIp);
        txtProgress = findViewById(R.id.txtProgress);

        this.registerReceiver(batteryStatus, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        context = this;
        presenterFirmware = new InfoValidate(this);
        batteryStatus = new BatteryStatus();

        SDKManager.init(context, () -> {
        });

        SDKManager.init(getApplicationContext(), new SDKManagerCallback() {
            @Override
            public void onFinish() {
            }
        });

        btnSend.setOnClickListener(v -> {
            btnSend.setEnabled(false);
            inputIp = edtDataIp.getText().toString();
            inputDescription = edtData.getText().toString();

            alertDialog = null;

            if ((retVal = presenterFirmware.validateBytesReads(totalBytesRead, inputIp, inputDescription)) != 0) {
                viewToast(retVal);
                return;
            }

            sendData();
        });
    }

    public void sendData() {
        txtMostrar.setText("INICIANDO DESCARGA...");
        presenterFirmware.receivingFile(inputIp, inputDescription, context);
    }

    public void viewToast(int rspCode) {
        runOnUiThread(() -> {
            btnSend.setEnabled(true);
            String msgObtain = presenterFirmware.showInfo(rspCode, context);
            Toast.makeText(context, msgObtain, Toast.LENGTH_SHORT).show();
        });
    }

    public void chargePorsentaje(String porsentajeString, String processDownload) {
        runOnUiThread(() -> {

            txtMostrar.setText(processDownload);
            txtProgress.setText("PORCENTAJE: " + porsentajeString + "%");
            if (porsentajeString.equals("100.00") && processDownload.equals("ARCHIVO RECIVIDO CORRECTAMENTE Y VERIFICADO")) {
                btnSend.setEnabled(true);

                presenterFirmware.validateRouteFirmware();
            }
        });
    }

    public void viewModalInstall(String pathTo, boolean validateExtension) {

        if (!validateExtension)
            return;

        if (alertDialog != null && alertDialog.isShowing())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(ViewFirmware.this);
        builder.setTitle("Actualizador del sistema");
        builder.setMessage("El sistema se reiniciará. Asegúrese de que la carga esté por encima del 50% para la actualización.");
        builder.setCancelable(false);
        builder.setNegativeButton("Cancelar", null);
        builder.setPositiveButton("Continuar", (dialogInterface, i) -> {

            if (batteryStatus.getLevelBattery() <= 50) {
                alertDialog.dismiss();
                viewToast(12);
                return;
            }

            if ((retVal = presenterFirmware.installFirmware(pathTo)) != 0)
                viewToast(retVal);
        });

        alertDialog = builder.create();
        alertDialog.show();
    }
}