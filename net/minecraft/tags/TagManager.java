/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.SynchronizableTagCollection;
import net.minecraft.tags.Tag;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class TagManager
implements PreparableReloadListener {
    private final SynchronizableTagCollection<Block> blocks = new SynchronizableTagCollection<Block>(Registry.BLOCK, "tags/blocks", "block");
    private final SynchronizableTagCollection<Item> items = new SynchronizableTagCollection<Item>(Registry.ITEM, "tags/items", "item");
    private final SynchronizableTagCollection<Fluid> fluids = new SynchronizableTagCollection<Fluid>(Registry.FLUID, "tags/fluids", "fluid");
    private final SynchronizableTagCollection<EntityType<?>> entityTypes = new SynchronizableTagCollection(Registry.ENTITY_TYPE, "tags/entity_types", "entity_type");

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
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> completableFuture = this.blocks.prepare(resourceManager, executor);
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> completableFuture2 = this.items.prepare(resourceManager, executor);
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> completableFuture3 = this.fluids.prepare(resourceManager, executor);
        CompletableFuture<Map<ResourceLocation, Tag.Builder>> completableFuture4 = this.entityTypes.prepare(resourceManager, executor);
        return ((CompletableFuture)CompletableFuture.allOf(completableFuture, completableFuture2, completableFuture3, completableFuture4).thenCompose(preparationBarrier::wait)).thenAcceptAsync(void_ -> {
            this.blocks.load((Map)completableFuture.join());
            this.items.load((Map)completableFuture2.join());
            this.fluids.load((Map)completableFuture3.join());
            this.entityTypes.load((Map)completableFuture4.join());
            BlockTags.reset(this.blocks);
            ItemTags.reset(this.items);
            FluidTags.reset(this.fluids);
            EntityTypeTags.reset(this.entityTypes);
        }, executor2);
    }
}

