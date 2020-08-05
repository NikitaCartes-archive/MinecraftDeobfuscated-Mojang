package net.minecraft.tags;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public final class EntityTypeTags {
	protected static final StaticTagHelper<EntityType<?>> HELPER = StaticTags.create(new ResourceLocation("entity_type"), TagContainer::getEntityTypes);
	public static final Tag.Named<EntityType<?>> SKELETONS = bind("skeletons");
	public static final Tag.Named<EntityType<?>> RAIDERS = bind("raiders");
	public static final Tag.Named<EntityType<?>> BEEHIVE_INHABITORS = bind("beehive_inhabitors");
	public static final Tag.Named<EntityType<?>> ARROWS = bind("arrows");
	public static final Tag.Named<EntityType<?>> IMPACT_PROJECTILES = bind("impact_projectiles");

	private static Tag.Named<EntityType<?>> bind(String string) {
		return HELPER.bind(string);
	}

	public static TagCollection<EntityType<?>> getAllTags() {
		return HELPER.getAllTags();
	}

	public static List<? extends Tag.Named<EntityType<?>>> getWrappers() {
		return HELPER.getWrappers();
	}
}
