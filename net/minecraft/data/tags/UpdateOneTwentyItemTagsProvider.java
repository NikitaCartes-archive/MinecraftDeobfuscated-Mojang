/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class UpdateOneTwentyItemTagsProvider
extends ItemTagsProvider {
    public UpdateOneTwentyItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagsProvider.TagLookup<Item>> completableFuture2, CompletableFuture<TagsProvider.TagLookup<Block>> completableFuture3) {
        super(packOutput, completableFuture, completableFuture2, completableFuture3);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.copy(BlockTags.PLANKS, ItemTags.PLANKS);
        this.copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        this.copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        this.copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        this.copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        this.copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        this.copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        this.copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        this.copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        this.copy(BlockTags.STANDING_SIGNS, ItemTags.SIGNS);
        this.copy(BlockTags.BAMBOO_BLOCKS, ItemTags.BAMBOO_BLOCKS);
        this.copy(BlockTags.CEILING_HANGING_SIGNS, ItemTags.HANGING_SIGNS);
        this.copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
        this.copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        this.copy(BlockTags.LEAVES, ItemTags.LEAVES);
        this.copy(BlockTags.FLOWERS, ItemTags.FLOWERS);
        this.copy(BlockTags.SAND, ItemTags.SAND);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.CHEST_BOATS)).add(Items.BAMBOO_CHEST_RAFT);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.BOATS)).add(Items.BAMBOO_RAFT);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.BOOKSHELF_BOOKS)).add(Items.BOOK, Items.WRITTEN_BOOK, Items.ENCHANTED_BOOK, Items.WRITABLE_BOOK);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.NON_FLAMMABLE_WOOD)).add(Items.WARPED_HANGING_SIGN, Items.CRIMSON_HANGING_SIGN);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS)).add(Items.ZOMBIE_HEAD, Items.SKELETON_SKULL, Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.WITHER_SKELETON_SKULL, Items.PIGLIN_HEAD, Items.PLAYER_HEAD);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.TRIMMABLE_ARMOR)).add(Items.NETHERITE_HELMET).add(Items.NETHERITE_CHESTPLATE).add(Items.NETHERITE_LEGGINGS).add(Items.NETHERITE_BOOTS).add(Items.DIAMOND_HELMET).add(Items.DIAMOND_CHESTPLATE).add(Items.DIAMOND_LEGGINGS).add(Items.DIAMOND_BOOTS).add(Items.GOLDEN_HELMET).add(Items.GOLDEN_CHESTPLATE).add(Items.GOLDEN_LEGGINGS).add(Items.GOLDEN_BOOTS).add(Items.IRON_HELMET).add(Items.IRON_CHESTPLATE).add(Items.IRON_LEGGINGS).add(Items.IRON_BOOTS).add(Items.CHAINMAIL_HELMET).add(Items.CHAINMAIL_CHESTPLATE).add(Items.CHAINMAIL_LEGGINGS).add(Items.CHAINMAIL_BOOTS).add(Items.LEATHER_HELMET).add(Items.LEATHER_CHESTPLATE).add(Items.LEATHER_LEGGINGS).add(Items.LEATHER_BOOTS).add(Items.TURTLE_HELMET);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.TRIM_MATERIALS)).add(Items.IRON_INGOT).add(Items.COPPER_INGOT).add(Items.GOLD_INGOT).add(Items.LAPIS_LAZULI).add(Items.EMERALD).add(Items.DIAMOND).add(Items.NETHERITE_INGOT).add(Items.REDSTONE).add(Items.QUARTZ).add(Items.AMETHYST_SHARD);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.TRIM_TEMPLATES)).add(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE).add(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE).add(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE).add(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE).add(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE).add(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE).add(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE).add(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE).add(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE).add(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE).add(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.SNIFFER_FOOD)).add(Items.TORCHFLOWER_SEEDS);
        this.copy(BlockTags.SMALL_FLOWERS, ItemTags.SMALL_FLOWERS);
        this.copy(BlockTags.CHERRY_LOGS, ItemTags.CHERRY_LOGS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.BOATS)).add(Items.CHERRY_BOAT);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.CHEST_BOATS)).add(Items.CHERRY_CHEST_BOAT);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.DECORATED_POT_SHARDS)).add(Items.BRICK, Items.POTTERY_SHARD_ARCHER, Items.POTTERY_SHARD_PRIZE, Items.POTTERY_SHARD_ARMS_UP, Items.POTTERY_SHARD_SKULL);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)ItemTags.BREAKS_DECORATED_POTS)).addTag((TagKey)ItemTags.TOOLS);
    }
}

