/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TextAndImageButton;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class CommonButtons {
    public static TextAndImageButton languageTextAndImage(Button.OnPress onPress) {
        return TextAndImageButton.builder(Component.translatable("options.language"), Button.WIDGETS_LOCATION, onPress).texStart(4, 110).offset(65, 3).yDiffTex(20).usedTextureSize(13, 13).textureSize(256, 256).build();
    }

    public static TextAndImageButton accessibilityTextAndImage(Button.OnPress onPress) {
        return TextAndImageButton.builder(Component.translatable("options.accessibility.title"), Button.ACCESSIBILITY_TEXTURE, onPress).texStart(3, 3).offset(65, 3).yDiffTex(20).usedTextureSize(15, 15).textureSize(32, 64).build();
    }
}

