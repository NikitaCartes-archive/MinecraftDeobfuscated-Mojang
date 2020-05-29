/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Unit;

@Environment(value=EnvType.CLIENT)
public class StringDecomposer {
    private static final Optional<Object> STOP_ITERATION = Optional.of(Unit.INSTANCE);

    private static boolean feedChar(Style style, Output output, int i, char c) {
        if (Character.isSurrogate(c)) {
            return output.onChar(i, style, 65533);
        }
        return output.onChar(i, style, c);
    }

    public static boolean iterate(String string, Style style, Output output) {
        int i = string.length();
        for (int j = 0; j < i; ++j) {
            char c = string.charAt(j);
            if (Character.isHighSurrogate(c)) {
                if (j + 1 >= i) {
                    if (output.onChar(j, style, 65533)) break;
                    return false;
                }
                char d = string.charAt(j + 1);
                if (Character.isLowSurrogate(d)) {
                    if (!output.onChar(j, style, Character.toCodePoint(c, d))) {
                        return false;
                    }
                    ++j;
                    continue;
                }
                if (output.onChar(j, style, 65533)) continue;
                return false;
            }
            if (StringDecomposer.feedChar(style, output, j, c)) continue;
            return false;
        }
        return true;
    }

    public static boolean iterateBackwards(String string, Style style, Output output) {
        int i = string.length();
        for (int j = i - 1; j >= 0; --j) {
            char c = string.charAt(j);
            if (Character.isLowSurrogate(c)) {
                if (j - 1 < 0) {
                    if (output.onChar(0, style, 65533)) break;
                    return false;
                }
                char d = string.charAt(j - 1);
                if (!(Character.isHighSurrogate(d) ? !output.onChar(--j, style, Character.toCodePoint(d, c)) : !output.onChar(j, style, 65533))) continue;
                return false;
            }
            if (StringDecomposer.feedChar(style, output, j, c)) continue;
            return false;
        }
        return true;
    }

    public static boolean iterateFormatted(String string, Style style, Output output) {
        return StringDecomposer.iterateFormatted(string, 0, style, output);
    }

    public static boolean iterateFormatted(String string, int i, Style style, Output output) {
        return StringDecomposer.iterateFormatted(string, i, style, style, output);
    }

    public static boolean iterateFormatted(String string, int i, Style style, Style style2, Output output) {
        int j = string.length();
        Style style3 = style;
        for (int k = i; k < j; ++k) {
            char d;
            char c = string.charAt(k);
            if (c == '\u00a7') {
                if (k + 1 >= j) break;
                d = string.charAt(k + 1);
                ChatFormatting chatFormatting = ChatFormatting.getByCode(d);
                if (chatFormatting != null) {
                    style3 = chatFormatting == ChatFormatting.RESET ? style2 : style3.applyLegacyFormat(chatFormatting);
                }
                ++k;
                continue;
            }
            if (Character.isHighSurrogate(c)) {
                if (k + 1 >= j) {
                    if (output.onChar(k, style3, 65533)) break;
                    return false;
                }
                d = string.charAt(k + 1);
                if (Character.isLowSurrogate(d)) {
                    if (!output.onChar(k, style3, Character.toCodePoint(c, d))) {
                        return false;
                    }
                    ++k;
                    continue;
                }
                if (output.onChar(k, style3, 65533)) continue;
                return false;
            }
            if (StringDecomposer.feedChar(style3, output, k, c)) continue;
            return false;
        }
        return true;
    }

    public static boolean iterateFormatted(FormattedText formattedText, Style style2, Output output) {
        return !formattedText.visit((style, string) -> StringDecomposer.iterateFormatted(string, 0, style, output) ? Optional.empty() : STOP_ITERATION, style2).isPresent();
    }

    public static String filterBrokenSurrogates(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        StringDecomposer.iterate(string, Style.EMPTY, (i, style, j) -> {
            stringBuilder.appendCodePoint(j);
            return true;
        });
        return stringBuilder.toString();
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface Output {
        public boolean onChar(int var1, Style var2, int var3);
    }
}

