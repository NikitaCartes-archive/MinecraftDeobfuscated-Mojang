package net.minecraft.tags;

import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagManager implements PreparableReloadListener {
	private final SynchronizableTagCollection<Block> blocks = new SynchronizableTagCollection<>(Registry.BLOCK, "tags/blocks", "block");
	private final SynchronizableTagCollection<Item> items = new SynchronizableTagCollection<>(Registry.ITEM, "tags/items", "item");
	private final SynchronizableTagCollection<Fluid> fluids = new SynchronizableTagCollection<>(Registry.FLUID, "tags/fluids", "fluid");
	private final SynchronizableTagCollection<EntityType<?>> entityTypes = new SynchronizableTagCollection<>(
		Registry.ENTITY_TYPE, "tags/entity_types", "entity_type"
	);

	public SynchronizableTagCollection<Block> getBlocks() {
		return this.blocks;
	}

	public SynchronizableTagCollection<Item> getItems() {
		return this.items;
	}

	public SynchronizableTagCollection<Fluid> getFluids() {
		return this.fluids;
	}

	public SynchronizableTagCollection<EntityType<?>> getEntityTypes() {
		return this.entityTypes;
	}

	public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		this.blocks.serializeToNetwork(friendlyByteBuf);
		this.items.serializeToNetwork(friendlyByteBuf);
		this.fluids.serializeToNetwork(friendlyByteBuf);
		this.entityTypes.serializeToNetwork(friendlyByteBuf);
	}

	public static TagManager deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		TagManager tagManager = new TagManager();
		tagManager.getBlocks().loadFromNetwork(friendlyByteBuf);
		tagManager.getItems().loadFromNetwork(friendlyByteBuf);
		tagManager.getFluids().loadFromNetwork(friendlyByteBuf);
		tagManager.getEntityTypes().loadFromNetwork(friendlyByteBuf);
		return tagManager;
	}

	@Override
	public CompletableFuture<Void> reload(
		PreparableReloadListener.PreparationBarrier preparationBarrier,
		ResourceManager resourceManager,
		ProfilerFiller profilerFiller,
		ProfilerFiller profilerFiller2,
		Executor executor,
		Executor executor2
	) {
		CompletableFuture<Map<ResourceLocation, Tag.Builder<Block>>> completableFuture = this.blocks.prepare(resourceManager, executor);
		CompletableFuture<Map<ResourceLocation, Tag.Builder<Item>>> completableFuture2 = this.items.prepare(resourceManager, executor);
		CompletableFuture<Map<ResourceLocation, Tag.Builder<Fluid>>> completableFuture3 = this.fluids.prepare(resourceManager, executor);
		CompletableFuture<Map<ResourceLocation, Tag.Builder<EntityType<?>>>> completableFuture4 = this.entityTypes.prepare(resourceManager, executor);
		return completableFuture.thenCombine(completableFuture2, Pair::of)
			.thenCombine(
				completableFuture3.thenCombine(completableFuture4, Pair::of),
				(pair, pair2) -> new TagManager.Preparations(
						(Map<ResourceLocation, Tag.Builder<Block>>)pair.getFirst(),
						(Map<ResourceLocation, Tag.Builder<Item>>)pair.getSecond(),
						(Map<ResourceLocation, Tag.Builder<Fluid>>)pair2.getFirst(),
						(Map<ResourceLocation, Tag.Builder<EntityType<?>>>)pair2.getSecond()
					)
			)
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(preparations -> {
				this.blocks.load(preparations.blocks);
				this.items.load(preparations.items);
				this.fluids.load(preparations.fluids);
				this.entityTypes.load(preparations.entityTypes);
				BlockTags.reset(this.blocks);
				ItemTags.reset(this.items);
				FluidTags.reset(this.fluids);
				EntityTypeTags.reset(this.entityTypes);
			}, executor2);
	}

	public static class Preparations {
		final Map<ResourceLocation, Tag.Builder<Block>> blocks;
		final Map<ResourceLocation, Tag.Builder<Item>> items;
		final Map<ResourceLocation, Tag.Builder<Fluid>> fluids;
		final Map<ResourceLocation, Tag.Builder<EntityType<?>>> entityTypes;

		public Preparations(
			Map<ResourceLocation, Tag.Builder<Block>> map,
			Map<ResourceLocation, Tag.Builder<Item>> map2,
			Map<ResourceLocation, Tag.Builder<Fluid>> map3,
			Map<ResourceLocation, Tag.Builder<EntityType<?>>> map4
		) {
			this.blocks = map;
			this.items = map2;
			this.fluids = map3;
			this.entityTypes = map4;
		}
	}
}
