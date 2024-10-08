package com.example.socketactivity.Presenter;

import static com.example.socketactivity.Codes.CodeError.DESCRIPTION_IS_EMPTY;
import static com.example.socketactivity.Codes.CodeError.DESCRIPTION_NO_VALID;
import static com.example.socketactivity.Codes.CodeError.IP_IS_EMPTY;
import static com.example.socketactivity.Codes.CodeError.READING_BYTES_IN_PROGRESS;

import android.content.Context;

import com.example.socketactivity.Model.DownloadFirmware;
import com.example.socketactivity.View.MessagePropertiesUtil;
import com.example.socketactivity.View.ViewFirmware;

public class InfoValidate implements PresenterFirmware {

    public Context context;
    public MessagePropertiesUtil err;
    public DownloadFirmware downloadFirmware;
    public ViewFirmware viewFirmware;

    public InfoValidate(ViewFirmware viewFirmware) {
        this.context = viewFirmware.getApplicationContext();
        this.err = new MessagePropertiesUtil(context);
        this.downloadFirmware = new DownloadFirmware(this);
        this.viewFirmware = viewFirmware;
    }

    @Override
    public int validateBytesReads(long totalBytesRead, String inputIp, String inputDescription) {
        if (totalBytesRead != 0)
            return READING_BYTES_IN_PROGRESS;

        if (inputIp.equals("") || inputIp.isEmpty())
            return IP_IS_EMPTY;

        if (inputDescription.equals("") || inputDescription.isEmpty())
            return DESCRIPTION_IS_EMPTY;

        if (!inputDescription.equals("DOWNLOAD"))
            return DESCRIPTION_NO_VALID;

        return 0;
    }

    @Override
    public String showInfo(int retVal, Context context) {
        this.context = context;
        return err.getMessageView(retVal);
    }

    @Override
    public void receivingFile(String inputIp, String inputDescription, Context context) {
        downloadFirmware.receivedFile(inputIp, inputDescription, context);
    }

    @Override
    public void uploadPorsentaje(String porsentajeString, String processDownload) {
        viewFirmware.chargePorsentaje(porsentajeString, processDownload);
    }

    @Override
    public void validateRouteFirmware() {
        downloadFirmware.routeCache();
    }

    @Override
    public void modalInstallFirmware(String pathTo, boolean validateExtension) {
        viewFirmware.viewModalInstall(pathTo, validateExtension);
    }

    @Override
    public int installFirmware(String pathTo) {
        return downloadFirmware.updateFirmware(pathTo);
    }
}
