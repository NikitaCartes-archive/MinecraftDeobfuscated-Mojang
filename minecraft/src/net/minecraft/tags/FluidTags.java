package net.minecraft.tags;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public final class FluidTags {
	protected static final StaticTagHelper<Fluid> HELPER = StaticTags.create(new ResourceLocation("fluid"), TagContainer::getFluids);
	public static final Tag.Named<Fluid> WATER = bind("water");
	public static final Tag.Named<Fluid> LAVA = bind("lava");

	private static Tag.Named<Fluid> bind(String string) {
		return HELPER.bind(string);
	}

	public static List<? extends Tag.Named<Fluid>> getWrappers() {
		return HELPER.getWrappers();
	}
}
