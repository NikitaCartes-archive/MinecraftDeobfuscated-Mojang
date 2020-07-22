/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;

@FunctionalInterface
public interface FormattedCharSequence {
    public static final FormattedCharSequence EMPTY = formattedCharSink -> true;

    @Environment(value=EnvType.CLIENT)
    public boolean accept(FormattedCharSink var1);

    @Environment(value=EnvType.CLIENT)
    public static FormattedCharSequence codepoint(int i, Style style) {
        return formattedCharSink -> formattedCharSink.accept(0, style, i);
    }

    @Environment(value=EnvType.CLIENT)
    public static FormattedCharSequence forward(String string, Style style) {
        if (string.isEmpty()) {
            return EMPTY;
        }
        return formattedCharSink -> StringDecomposer.iterate(string, style, formattedCharSink);
    }

    @Environment(value=EnvType.CLIENT)
    public static FormattedCharSequence backward(String string, Style style, Int2IntFunction int2IntFunction) {
        if (string.isEmpty()) {
            return EMPTY;
        }
        return formattedCharSink -> StringDecomposer.iterateBackwards(string, style, FormattedCharSequence.decorateOutput(formattedCharSink, int2IntFunction));
    }

    @Environment(value=EnvType.CLIENT)
    public static FormattedCharSink decorateOutput(FormattedCharSink formattedCharSink, Int2IntFunction int2IntFunction) {
        return (i, style, j) -> formattedCharSink.accept(i, style, (Integer)int2IntFunction.apply(j));
    }

    @Environment(value=EnvType.CLIENT)
    public static FormattedCharSequence composite(FormattedCharSequence formattedCharSequence, FormattedCharSequence formattedCharSequence2) {
        return FormattedCharSequence.fromPair(formattedCharSequence, formattedCharSequence2);
    }

    @Environment(value=EnvType.CLIENT)
    public static FormattedCharSequence composite(List<FormattedCharSequence> list) {
        int i = list.size();
        switch (i) {
            case 0: {
                return EMPTY;
            }
            case 1: {
                return list.get(0);
            }
            case 2: {
                return FormattedCharSequence.fromPair(list.get(0), list.get(1));
            }
        }
        return FormattedCharSequence.fromList(ImmutableList.copyOf(list));
    }

    @Environment(value=EnvType.CLIENT)
    public static FormattedCharSequence fromPair(FormattedCharSequence formattedCharSequence, FormattedCharSequence formattedCharSequence2) {
        return formattedCharSink -> formattedCharSequence.accept(formattedCharSink) && formattedCharSequence2.accept(formattedCharSink);
    }

    @Environment(value=EnvType.CLIENT)
    public static FormattedCharSequence fromList(List<FormattedCharSequence> list) {
        return formattedCharSink -> {
            for (FormattedCharSequence formattedCharSequence : list) {
                if (formattedCharSequence.accept(formattedCharSink)) continue;
                return false;
            }
            return true;
        };
    }
}

