package com.example.socketactivity.Model;

import static com.example.socketactivity.Codes.CodeError.FORMAT_DECIMAL_2F;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.example.socketactivity.Presenter.PresenterFirmware;
import com.pos.device.sys.SystemManager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DownloadFirmware {

    public PresenterFirmware presenterFirmware;
    public Context context;
    String porsentajeString = "";
    String processDownload = "";
    int bytesRead = 0;
    long totalBytesRead = 0;
    long remainingBytes = 0;
    long fileSizeLong = 0;
    int ret = -1;
    boolean fileReceivedCorrectly = false;
    File fileReceived;

    public DownloadFirmware(PresenterFirmware presenterFirmware) {
        this.presenterFirmware = presenterFirmware;
    }

    public void receivedFile(String inputIp, String inputDescription, Context context) {
        this.context = context;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new Thread(() -> {
            try {

                while (!fileReceivedCorrectly) {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(inputIp, 5000), 60000);

                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    out.println(inputDescription);

                    DataInputStream fileIn = new DataInputStream(socket.getInputStream());
                    fileReceived = new File("/cache" + File.separator, in.readLine());

                    if (fileReceived.exists())
                        fileReceived.delete();

                    FileOutputStream fileOut = new FileOutputStream(fileReceived);

                    String fileSize = in.readLine();
                    fileSizeLong = Long.parseLong(fileSize);
                    remainingBytes = fileSizeLong;

                    writeBytesFile(fileIn, fileSizeLong, fileOut);

                    fileOut.flush();
                    fileOut.close();

                    if (compareChecksun(in, fileReceived))
                        break;

                    socket.close();
                }
            } catch (Exception e) {
                presenterFirmware.uploadPorsentaje("0", "Error en la descarga");
                e.printStackTrace();
            }
        }).start();
    }

    public boolean compareChecksun(BufferedReader in, File fileCompare) throws IOException {
        String serverChecksum = in.readLine();
        String clientChecksum = calculateChecksum(fileCompare, "SHA-256");

        if (clientChecksum.equals(serverChecksum)) {
            fileReceivedCorrectly = false;
            totalBytesRead = 0;
            fileSizeLong = 0;
            bytesRead = 0;
            remainingBytes = 0;
            serverChecksum = "";
            clientChecksum = "";

            presenterFirmware.uploadPorsentaje(porsentajeString, "ARCHIVO RECIVIDO CORRECTAMENTE Y VERIFICADO");
            return true;
        } else {
            fileReceivedCorrectly = false;
            presenterFirmware.uploadPorsentaje(porsentajeString, "EL CHECKSUM NO COINCIDE. ELIMINANDO ARCHIVO Y REINTENTANDO...");
            Log.e("Error", "El checksum no coincide. Eliminando archivo y reintentando...");
            return false;
        }
    }

    public void writeBytesFile(DataInputStream fileIn, Long fileSizeLong, FileOutputStream fileOut) throws IOException {
        byte[] buffer = new byte[4096];

        while (remainingBytes > 0) {
            bytesRead = fileIn.read(buffer, 0, (int) Math.min(buffer.length, remainingBytes));

            if (bytesRead == -1)
                break;

            fileOut.write(buffer, 0, bytesRead);
            remainingBytes -= bytesRead;
            totalBytesRead += bytesRead;

            double porcentaje = (double) totalBytesRead / fileSizeLong * 100;
            porsentajeString = String.format(FORMAT_DECIMAL_2F, porcentaje);

            processDownload = porcentaje < 100.00 ? "DESCARGANDO..." : "DESCARGA COMPLETA, VERIFICANDO ARCHIVO...";

            presenterFirmware.uploadPorsentaje(porsentajeString, processDownload);
        }
    }

    private String calculateChecksum(File file, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[4096];
            int bytesCount;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }

            fis.close();
            byte[] bytes = digest.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void routeCache() {
        try {
            final File pathTo = fileReceived;
            boolean validateExtension = pathTo.toString().endsWith(".img") ? true : false;
            sleep(6000);

            presenterFirmware.modalInstallFirmware(pathTo.toString(), validateExtension);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public int updateFirmware(String pathTo) {
        try {
            ret = SystemManager.updateFirmware(pathTo);
        } catch (Exception e) {
            ret = 400;
        }
        return ret;
    }
}