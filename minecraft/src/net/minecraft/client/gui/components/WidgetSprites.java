package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public record WidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused, ResourceLocation disabledFocused) {
	public WidgetSprites(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		this(resourceLocation, resourceLocation, resourceLocation2, resourceLocation2);
	}

	public WidgetSprites(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3) {
		this(resourceLocation, resourceLocation2, resourceLocation3, resourceLocation2);
	}

	public ResourceLocation get(boolean bl, boolean bl2) {
		if (bl) {
			return bl2 ? this.enabledFocused : this.enabled;
		} else {
			return bl2 ? this.disabledFocused : this.disabled;
		}
	}
}
