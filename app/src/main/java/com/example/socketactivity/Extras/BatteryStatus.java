package com.example.socketactivity.Extras;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryStatus extends BroadcastReceiver {
    int levelBattery;

    public int getLevelBattery() {
        return levelBattery;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        this.levelBattery = level;
    }
}
