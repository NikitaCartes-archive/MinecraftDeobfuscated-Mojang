package net.minecraft.tags;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class SerializationTags {
	private static volatile SerializationTags instance = new SerializationTags(
		BlockTags.getAllTags(), ItemTags.getAllTags(), FluidTags.getAllTags(), EntityTypeTags.getAllTags()
	);
	private final TagCollection<Block> blocks;
	private final TagCollection<Item> items;
	private final TagCollection<Fluid> fluids;
	private final TagCollection<EntityType<?>> entityTypes;

	private SerializationTags(
		TagCollection<Block> tagCollection, TagCollection<Item> tagCollection2, TagCollection<Fluid> tagCollection3, TagCollection<EntityType<?>> tagCollection4
	) {
		this.blocks = tagCollection;
		this.items = tagCollection2;
		this.fluids = tagCollection3;
		this.entityTypes = tagCollection4;
	}

	public TagCollection<Block> getBlocks() {
		return this.blocks;
	}

	public TagCollection<Item> getItems() {
		return this.items;
	}

	public TagCollection<Fluid> getFluids() {
		return this.fluids;
	}

	public TagCollection<EntityType<?>> getEntityTypes() {
		return this.entityTypes;
	}

	public static SerializationTags getInstance() {
		return instance;
	}

	public static void bind(
		TagCollection<Block> tagCollection, TagCollection<Item> tagCollection2, TagCollection<Fluid> tagCollection3, TagCollection<EntityType<?>> tagCollection4
	) {
		instance = new SerializationTags(tagCollection, tagCollection2, tagCollection3, tagCollection4);
	}
}
