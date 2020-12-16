package net.minecraft.tags;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.world.level.material.Fluid;

public final class FluidTags {
	protected static final StaticTagHelper<Fluid> HELPER = StaticTags.create(Registry.FLUID_REGISTRY, "tags/fluids");
	private static final List<Tag<Fluid>> KNOWN_TAGS = Lists.<Tag<Fluid>>newArrayList();
	public static final Tag.Named<Fluid> WATER = bind("water");
	public static final Tag.Named<Fluid> LAVA = bind("lava");

	private static Tag.Named<Fluid> bind(String string) {
		Tag.Named<Fluid> named = HELPER.bind(string);
		KNOWN_TAGS.add(named);
		return named;
	}

	@Deprecated
	public static List<Tag<Fluid>> getStaticTags() {
		return KNOWN_TAGS;
	}
}
