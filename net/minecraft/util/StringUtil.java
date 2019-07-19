/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class StringUtil {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");

    @Environment(value=EnvType.CLIENT)
    public static String formatTickDuration(int i) {
        int j = i / 20;
        int k = j / 60;
        if ((j %= 60) < 10) {
            return k + ":0" + j;
        }
        return k + ":" + j;
    }

    @Environment(value=EnvType.CLIENT)
    public static String stripColor(String string) {
        return STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    public static boolean isNullOrEmpty(@Nullable String string) {
        return StringUtils.isEmpty(string);
    }
}

