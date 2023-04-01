package net.minecraft.world.level.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class BuiltinDimensionTypes {
	public static final ResourceKey<DimensionType> OVERWORLD = register("overworld");
	public static final ResourceKey<DimensionType> NETHER = register("the_nether");
	public static final ResourceKey<DimensionType> END = register("the_end");
	public static final ResourceKey<DimensionType> MOON = register("the_moon");
	public static final ResourceKey<DimensionType> OVERWORLD_CAVES = register("overworld_caves");
	public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
	public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
	public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");
	public static final ResourceLocation MOON_EFFECTS = new ResourceLocation("the_moon");

	private static ResourceKey<DimensionType> register(String string) {
		return ResourceKey.create(Registries.DIMENSION_TYPE, new ResourceLocation(string));
	}
}
