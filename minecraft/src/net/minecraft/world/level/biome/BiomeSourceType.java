package net.minecraft.world.level.biome;

import net.minecraft.core.Registry;

public class BiomeSourceType {
	public static final BiomeSourceType CHECKERBOARD = register("checkerboard");
	public static final BiomeSourceType FIXED = register("fixed");
	public static final BiomeSourceType VANILLA_LAYERED = register("vanilla_layered");
	public static final BiomeSourceType THE_END = register("the_end");
	public static final BiomeSourceType MULTI_NOISE = register("multi_noise");

	private static BiomeSourceType register(String string) {
		return Registry.register(Registry.BIOME_SOURCE_TYPE, string, new BiomeSourceType());
	}
}
