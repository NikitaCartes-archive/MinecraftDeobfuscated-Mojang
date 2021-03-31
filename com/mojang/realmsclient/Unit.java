/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public enum Unit {
    B,
    KB,
    MB,
    GB;

    private static final int BASE_UNIT = 1024;

    public static Unit getLargest(long l) {
        if (l < 1024L) {
            return B;
        }
        try {
            int i = (int)(Math.log(l) / Math.log(1024.0));
            String string = String.valueOf("KMGTPE".charAt(i - 1));
            return Unit.valueOf(string + "B");
        } catch (Exception exception) {
            return GB;
        }
    }

    public static double convertTo(long l, Unit unit) {
        if (unit == B) {
            return l;
        }
        return (double)l / Math.pow(1024.0, unit.ordinal());
    }

    public static String humanReadable(long l) {
        int i = 1024;
        if (l < 1024L) {
            return l + " B";
        }
        int j = (int)(Math.log(l) / Math.log(1024.0));
        String string = "KMGTPE".charAt(j - 1) + "";
        return String.format(Locale.ROOT, "%.1f %sB", (double)l / Math.pow(1024.0, j), string);
    }

    public static String humanReadable(long l, Unit unit) {
        return String.format("%." + (unit == GB ? "1" : "0") + "f %s", Unit.convertTo(l, unit), unit.name());
    }
}

