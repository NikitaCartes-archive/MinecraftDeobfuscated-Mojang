package net.minecraft.world.level.block.entity;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record BannerPattern() {
	public static ResourceLocation location(ResourceKey<BannerPattern> resourceKey, boolean bl) {
		String string = bl ? "banner" : "shield";
		return resourceKey.location().withPrefix("entity/" + string + "/");
	}
}
