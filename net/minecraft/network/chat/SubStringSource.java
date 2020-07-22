/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

@Environment(value=EnvType.CLIENT)
public class SubStringSource {
    private final String plainText;
    private final List<Style> charStyles;
    private final Int2IntFunction reverseCharModifier;

    private SubStringSource(String string, List<Style> list, Int2IntFunction int2IntFunction) {
        this.plainText = string;
        this.charStyles = ImmutableList.copyOf(list);
        this.reverseCharModifier = int2IntFunction;
    }

    public String getPlainText() {
        return this.plainText;
    }

    public List<FormattedCharSequence> substring(int i, int j, boolean bl) {
        if (j == 0) {
            return ImmutableList.of();
        }
        ArrayList<FormattedCharSequence> list = Lists.newArrayList();
        Style style = this.charStyles.get(i);
        int k = i;
        for (int l = 1; l < j; ++l) {
            int m = i + l;
            Style style2 = this.charStyles.get(m);
            if (style2.equals(style)) continue;
            String string = this.plainText.substring(k, m);
            list.add(bl ? FormattedCharSequence.backward(string, style, this.reverseCharModifier) : FormattedCharSequence.forward(string, style));
            style = style2;
            k = m;
        }
        if (k < i + j) {
            String string2 = this.plainText.substring(k, i + j);
            list.add(bl ? FormattedCharSequence.backward(string2, style, this.reverseCharModifier) : FormattedCharSequence.forward(string2, style));
        }
        return bl ? Lists.reverse(list) : list;
    }

    public static SubStringSource create(FormattedText formattedText, Int2IntFunction int2IntFunction, UnaryOperator<String> unaryOperator) {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Style> list = Lists.newArrayList();
        formattedText.visit((style2, string) -> {
            StringDecomposer.iterateFormatted(string, style2, (i, style, j) -> {
                stringBuilder.appendCodePoint(j);
                int k = Character.charCount(j);
                for (int l = 0; l < k; ++l) {
                    list.add(style);
                }
                return true;
            });
            return Optional.empty();
        }, Style.EMPTY);
        return new SubStringSource((String)unaryOperator.apply(stringBuilder.toString()), list, int2IntFunction);
    }
}

