package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

public interface TagContainer {
	TagContainer EMPTY = of(TagCollection.empty(), TagCollection.empty(), TagCollection.empty(), TagCollection.empty());

	TagCollection<Block> getBlocks();

	TagCollection<Item> getItems();

	TagCollection<Fluid> getFluids();

	TagCollection<EntityType<?>> getEntityTypes();

	default void bindToGlobal() {
		StaticTags.resetAll(this);
		Blocks.rebuildCache();
	}

	default void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		this.getBlocks().serializeToNetwork(friendlyByteBuf, Registry.BLOCK);
		this.getItems().serializeToNetwork(friendlyByteBuf, Registry.ITEM);
		this.getFluids().serializeToNetwork(friendlyByteBuf, Registry.FLUID);
		this.getEntityTypes().serializeToNetwork(friendlyByteBuf, Registry.ENTITY_TYPE);
	}

	static TagContainer deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		TagCollection<Block> tagCollection = TagCollection.loadFromNetwork(friendlyByteBuf, Registry.BLOCK);
		TagCollection<Item> tagCollection2 = TagCollection.loadFromNetwork(friendlyByteBuf, Registry.ITEM);
		TagCollection<Fluid> tagCollection3 = TagCollection.loadFromNetwork(friendlyByteBuf, Registry.FLUID);
		TagCollection<EntityType<?>> tagCollection4 = TagCollection.loadFromNetwork(friendlyByteBuf, Registry.ENTITY_TYPE);
		return of(tagCollection, tagCollection2, tagCollection3, tagCollection4);
	}

	static TagContainer of(
		TagCollection<Block> tagCollection, TagCollection<Item> tagCollection2, TagCollection<Fluid> tagCollection3, TagCollection<EntityType<?>> tagCollection4
	) {
		return new TagContainer() {
			@Override
			public TagCollection<Block> getBlocks() {
				return tagCollection;
			}

			@Override
			public TagCollection<Item> getItems() {
				return tagCollection2;
			}

			@Override
			public TagCollection<Fluid> getFluids() {
				return tagCollection3;
			}

			@Override
			public TagCollection<EntityType<?>> getEntityTypes() {
				return tagCollection4;
			}
		};
	}
}
