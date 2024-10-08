package com.example.socketactivity.Presenter;

import android.content.Context;

public interface PresenterFirmware {

    int validateBytesReads(long totalBytesRead, String inputIp, String inputDescription);

    String showInfo(int retVal, Context context);

    void receivingFile(String inputIp, String inputDescription, Context context);

    void uploadPorsentaje(String porsentajeString, String processDownload);

    void validateRouteFirmware();

    void modalInstallFirmware(String pathTo, boolean validateExtension);

    int installFirmware(String pathTo);
}
