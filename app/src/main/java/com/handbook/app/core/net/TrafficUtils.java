package com.handbook.app.core.net;

import android.net.TrafficStats;

import androidx.annotation.WorkerThread;

import org.jetbrains.annotations.Nullable;

public final class TrafficUtils {

    @WorkerThread
    public static long getNetworkSpeed() {
        String downloadSpeedOutput  = "";
        String units                = "";

        final long bytes1 = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
        try { Thread.sleep(1000); }
        catch (InterruptedException ignore) {}

        final long bytes2 = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
        return bytes2 - bytes1;
    }

    @WorkerThread
    public static @Nullable long[] getCurrentNetworkBytes(int uid) {
        final long[] bytes = new long[2];
        bytes[0] = TrafficStats.getUidTxBytes(uid);
        bytes[1] = TrafficStats.getUidRxBytes(uid);
        return bytes;
    }
}
