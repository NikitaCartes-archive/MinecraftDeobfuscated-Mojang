/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

@Environment(value=EnvType.CLIENT)
public class ComponentRenderUtils {
    private static final FormattedText INDENT = FormattedText.of(" ");

    private static String stripColor(String string) {
        return Minecraft.getInstance().options.chatColors ? string : ChatFormatting.stripFormatting(string);
    }

    public static List<FormattedText> wrapComponents(FormattedText formattedText, int i, Font font) {
        ComponentCollector componentCollector = new ComponentCollector();
        formattedText.visit((style, string) -> {
            componentCollector.append(FormattedText.of(ComponentRenderUtils.stripColor(string), style));
            return Optional.empty();
        }, Style.EMPTY);
        List<FormattedText> list = font.getSplitter().splitLines(componentCollector.getResultOrEmpty(), i, Style.EMPTY, INDENT);
        if (list.isEmpty()) {
            return Lists.newArrayList(FormattedText.EMPTY);
        }
        return list;
    }
}

