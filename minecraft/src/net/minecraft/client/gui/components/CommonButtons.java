package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class CommonButtons {
	public static TextAndImageButton languageTextAndImage(Minecraft minecraft, Screen screen) {
		return TextAndImageButton.builder(
				Component.translatable("options.language"),
				Button.WIDGETS_LOCATION,
				button -> minecraft.setScreen(new LanguageSelectScreen(screen, minecraft.options, minecraft.getLanguageManager()))
			)
			.texStart(4, 110)
			.offset(65, 3)
			.yDiffTex(20)
			.usedTextureSize(13, 13)
			.textureSize(256, 256)
			.build();
	}

	public static TextAndImageButton accessibilityTextAndImage(Minecraft minecraft, Screen screen) {
		return TextAndImageButton.builder(
				Component.translatable("options.accessibility.title"),
				Button.ACCESSIBILITY_TEXTURE,
				button -> minecraft.setScreen(new AccessibilityOptionsScreen(screen, minecraft.options))
			)
			.texStart(3, 3)
			.offset(65, 3)
			.yDiffTex(20)
			.usedTextureSize(15, 15)
			.textureSize(32, 64)
			.build();
	}
}
