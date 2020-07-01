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

	public static TagCollection<Fluid> getAllTags() {
		return HELPER.getAllTags();
	}

	public static List<? extends Tag<Fluid>> getWrappers() {
		return HELPER.getWrappers();
	}
}
