package net.minecraft.tags;

import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class SerializationTags {
	private static volatile TagContainer instance = TagContainer.of(
		TagCollection.of((Map<ResourceLocation, Tag<Block>>)BlockTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, named -> named))),
		TagCollection.of((Map<ResourceLocation, Tag<Item>>)ItemTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, named -> named))),
		TagCollection.of((Map<ResourceLocation, Tag<Fluid>>)FluidTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, named -> named))),
		TagCollection.of(
			(Map<ResourceLocation, Tag<EntityType<?>>>)EntityTypeTags.getWrappers().stream().collect(Collectors.toMap(Tag.Named::getName, named -> named))
		)
	);

	public static TagContainer getInstance() {
		return instance;
	}

	public static void bind(TagContainer tagContainer) {
		instance = tagContainer;
	}
}
