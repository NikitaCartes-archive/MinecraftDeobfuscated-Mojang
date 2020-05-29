package net.minecraft.tags;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class EntityTypeTags {
	private static final StaticTagHelper<EntityType<?>> HELPER = new StaticTagHelper<>();
	public static final Tag.Named<EntityType<?>> SKELETONS = bind("skeletons");
	public static final Tag.Named<EntityType<?>> RAIDERS = bind("raiders");
	public static final Tag.Named<EntityType<?>> BEEHIVE_INHABITORS = bind("beehive_inhabitors");
	public static final Tag.Named<EntityType<?>> ARROWS = bind("arrows");
	public static final Tag.Named<EntityType<?>> IMPACT_PROJECTILES = bind("impact_projectiles");

	private static Tag.Named<EntityType<?>> bind(String string) {
		return HELPER.bind(string);
	}

	public static void reset(TagCollection<EntityType<?>> tagCollection) {
		HELPER.reset(tagCollection);
	}

	@Environment(EnvType.CLIENT)
	public static void resetToEmpty() {
		HELPER.resetToEmpty();
	}

	public static TagCollection<EntityType<?>> getAllTags() {
		return HELPER.getAllTags();
	}

	public static Set<ResourceLocation> getMissingTags(TagCollection<EntityType<?>> tagCollection) {
		return HELPER.getMissingTags(tagCollection);
	}
}
