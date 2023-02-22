package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class CommonButtons {
	public static TextAndImageButton languageTextAndImage(Button.OnPress onPress) {
		return TextAndImageButton.builder(Component.translatable("options.language"), Button.WIDGETS_LOCATION, onPress)
			.texStart(3, 109)
			.offset(65, 3)
			.yDiffTex(20)
			.usedTextureSize(14, 14)
			.textureSize(256, 256)
			.build();
	}

	public static TextAndImageButton accessibilityTextAndImage(Button.OnPress onPress) {
		return TextAndImageButton.builder(Component.translatable("options.accessibility.title"), Button.ACCESSIBILITY_TEXTURE, onPress)
			.texStart(3, 2)
			.offset(65, 2)
			.yDiffTex(20)
			.usedTextureSize(14, 16)
			.textureSize(32, 64)
			.build();
	}
}
