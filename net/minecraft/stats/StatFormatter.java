/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;

public interface StatFormatter {
    public static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("########0.00"), decimalFormat -> decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));
    public static final StatFormatter DEFAULT = NumberFormat.getIntegerInstance(Locale.US)::format;
    public static final StatFormatter DIVIDE_BY_TEN = i -> DECIMAL_FORMAT.format((double)i * 0.1);
    public static final StatFormatter DISTANCE = i -> {
        double d = (double)i / 100.0;
        double e = d / 1000.0;
        if (e > 0.5) {
            return DECIMAL_FORMAT.format(e) + " km";
        }
        if (d > 0.5) {
            return DECIMAL_FORMAT.format(d) + " m";
        }
        return i + " cm";
    };
    public static final StatFormatter TIME = i -> {
        double d = (double)i / 20.0;
        double e = d / 60.0;
        double f = e / 60.0;
        double g = f / 24.0;
        double h = g / 365.0;
        if (h > 0.5) {
            return DECIMAL_FORMAT.format(h) + " y";
        }
        if (g > 0.5) {
            return DECIMAL_FORMAT.format(g) + " d";
        }
        if (f > 0.5) {
            return DECIMAL_FORMAT.format(f) + " h";
        }
        if (e > 0.5) {
            return DECIMAL_FORMAT.format(e) + " m";
        }
        return d + " s";
    };

    @Environment(value=EnvType.CLIENT)
    public String format(int var1);
}

