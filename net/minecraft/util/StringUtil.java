/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class StringUtil {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
    private static final Pattern LINE_PATTERN = Pattern.compile("\r\n|[\n\r\u2028\u2029\u0085]");

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

    @Environment(value=EnvType.CLIENT)
    public static int lineCount(String string) {
        if (string.isEmpty()) {
            return 0;
        }
        Matcher matcher = LINE_PATTERN.matcher(string);
        int i = 1;
        while (matcher.find()) {
            ++i;
        }
        return i;
    }
}

