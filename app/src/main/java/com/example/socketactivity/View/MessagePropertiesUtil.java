package com.example.socketactivity.View;

import android.content.Context;

import com.example.socketactivity.R;

import java.io.InputStream;
import java.util.Properties;

public class MessagePropertiesUtil {
    private Properties properties;

    public MessagePropertiesUtil(Context context) {
        properties = new Properties();

        try {
            InputStream rawResource = context.getResources().openRawResource(R.raw.list_messages);
            properties.load(rawResource);
            rawResource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMessageView(int codeMessage){
        String message = properties.getProperty(String.valueOf(codeMessage));
        if (message == null){
            return "Error desconocido";
        }
        return message;
    }
}
