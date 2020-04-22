/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

@Environment(value=EnvType.CLIENT)
public class ComponentRenderUtils {
    private static String stripColor(String string) {
        return Minecraft.getInstance().options.chatColors ? string : ChatFormatting.stripFormatting(string);
    }

    public static List<Component> wrapComponents(Component component, int i, Font font) {
        ComponentCollector componentCollector = new ComponentCollector();
        component.visit((style, string) -> {
            componentCollector.append(new TextComponent(ComponentRenderUtils.stripColor(string)).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return font.getSplitter().splitLines(componentCollector.getResultOrEmpty(), i, Style.EMPTY, false);
    }
}

