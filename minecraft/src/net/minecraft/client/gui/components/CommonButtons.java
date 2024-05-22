package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class CommonButtons {
	public static SpriteIconButton language(int i, Button.OnPress onPress, boolean bl) {
		return SpriteIconButton.builder(Component.translatable("options.language"), onPress, bl)
			.width(i)
			.sprite(ResourceLocation.withDefaultNamespace("icon/language"), 15, 15)
			.build();
	}

	public static SpriteIconButton accessibility(int i, Button.OnPress onPress, boolean bl) {
		Component component = bl ? Component.translatable("options.accessibility") : Component.translatable("accessibility.onboarding.accessibility.button");
		return SpriteIconButton.builder(component, onPress, bl).width(i).sprite(ResourceLocation.withDefaultNamespace("icon/accessibility"), 15, 15).build();
	}
}
