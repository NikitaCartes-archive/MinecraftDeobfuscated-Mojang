/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

@Environment(value=EnvType.CLIENT)
public class ComponentRenderUtils {
    private static final FormattedCharSequence INDENT = FormattedCharSequence.codepoint(32, Style.EMPTY);

    private static String stripColor(String string) {
        return Minecraft.getInstance().options.chatColors ? string : ChatFormatting.stripFormatting(string);
    }

    public static List<FormattedCharSequence> wrapComponents(FormattedText formattedText2, int i, Font font) {
        ComponentCollector componentCollector = new ComponentCollector();
        formattedText2.visit((style, string) -> {
            componentCollector.append(FormattedText.of(ComponentRenderUtils.stripColor(string), style));
            return Optional.empty();
        }, Style.EMPTY);
        ArrayList<FormattedCharSequence> list = Lists.newArrayList();
        font.getSplitter().splitLines(componentCollector.getResultOrEmpty(), i, Style.EMPTY, (formattedText, boolean_) -> {
            FormattedCharSequence formattedCharSequence = Language.getInstance().getVisualOrder((FormattedText)formattedText);
            list.add(boolean_ != false ? FormattedCharSequence.composite(INDENT, formattedCharSequence) : formattedCharSequence);
        });
        if (list.isEmpty()) {
            return Lists.newArrayList(FormattedCharSequence.EMPTY);
        }
        return list;
    }
}

