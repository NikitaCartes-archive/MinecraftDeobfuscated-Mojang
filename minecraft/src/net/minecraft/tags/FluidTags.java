package net.minecraft.tags;

import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidTags {
	private static final StaticTagHelper<Fluid> HELPER = new StaticTagHelper<>();
	public static final Tag.Named<Fluid> WATER = bind("water");
	public static final Tag.Named<Fluid> LAVA = bind("lava");

	private static Tag.Named<Fluid> bind(String string) {
		return HELPER.bind(string);
	}

	public static void reset(TagCollection<Fluid> tagCollection) {
		HELPER.reset(tagCollection);
	}

	@Environment(EnvType.CLIENT)
	public static void resetToEmpty() {
		HELPER.resetToEmpty();
	}

	public static TagCollection<Fluid> getAllTags() {
		return HELPER.getAllTags();
	}

	public static List<StaticTagHelper.Wrapper<Fluid>> getWrappers() {
		return HELPER.getWrappers();
	}

	public static Set<ResourceLocation> getMissingTags(TagCollection<Fluid> tagCollection) {
		return HELPER.getMissingTags(tagCollection);
	}
}
