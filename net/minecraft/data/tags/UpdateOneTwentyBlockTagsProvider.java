/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyBlockTagsProvider
extends IntrinsicHolderTagsProvider<Block> {
    public UpdateOneTwentyBlockTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagsProvider.TagLookup<Block>> completableFuture2) {
        super(packOutput, Registries.BLOCK, completableFuture, completableFuture2, block -> block.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.PLANKS)).add(Blocks.BAMBOO_PLANKS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_BUTTONS)).add(Blocks.BAMBOO_BUTTON);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_DOORS)).add(Blocks.BAMBOO_DOOR);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_STAIRS)).add(Blocks.BAMBOO_STAIRS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_SLABS)).add(Blocks.BAMBOO_SLAB);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_FENCES)).add(Blocks.BAMBOO_FENCE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_PRESSURE_PLATES)).add(Blocks.BAMBOO_PRESSURE_PLATE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_TRAPDOORS)).add(Blocks.BAMBOO_TRAPDOOR);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.STANDING_SIGNS)).add(Blocks.BAMBOO_SIGN);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WALL_SIGNS)).add(Blocks.BAMBOO_WALL_SIGN);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.FENCE_GATES)).add(Blocks.BAMBOO_FENCE_GATE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.BAMBOO_BLOCKS)).add(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.CEILING_HANGING_SIGNS)).add(Blocks.OAK_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.CRIMSON_HANGING_SIGN, Blocks.WARPED_HANGING_SIGN, Blocks.MANGROVE_HANGING_SIGN, Blocks.BAMBOO_HANGING_SIGN);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WALL_HANGING_SIGNS)).add(Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.ALL_HANGING_SIGNS)).addTag((TagKey)BlockTags.CEILING_HANGING_SIGNS)).addTag((TagKey)BlockTags.WALL_HANGING_SIGNS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.ALL_SIGNS)).addTag((TagKey)BlockTags.ALL_HANGING_SIGNS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)((IntrinsicHolderTagsProvider.IntrinsicTagAppender)((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.MINEABLE_WITH_AXE)).addTag((TagKey)BlockTags.ALL_HANGING_SIGNS)).add(Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_MOSAIC_SLAB, Blocks.BAMBOO_MOSAIC_STAIRS).addTag((TagKey)BlockTags.BAMBOO_BLOCKS)).add(Blocks.CHISELED_BOOKSHELF);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.SNIFFER_DIGGABLE_BLOCK)).add(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.MOSS_BLOCK, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.SMALL_FLOWERS)).add(Blocks.TORCHFLOWER);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.CROPS)).add(Blocks.TORCHFLOWER_CROP);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.CHERRY_LOGS)).add(Blocks.CHERRY_LOG, Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_CHERRY_WOOD);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.PLANKS)).add(Blocks.CHERRY_PLANKS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_BUTTONS)).add(Blocks.CHERRY_BUTTON);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_DOORS)).add(Blocks.CHERRY_DOOR);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_STAIRS)).add(Blocks.CHERRY_STAIRS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_SLABS)).add(Blocks.CHERRY_SLAB);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_FENCES)).add(Blocks.CHERRY_FENCE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.SAPLINGS)).add(Blocks.CHERRY_SAPLING);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.LOGS_THAT_BURN)).addTag((TagKey)BlockTags.CHERRY_LOGS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.OVERWORLD_NATURAL_LOGS)).add(Blocks.CHERRY_LOG);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.FLOWER_POTS)).add(Blocks.POTTED_CHERRY_SAPLING);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_PRESSURE_PLATES)).add(Blocks.CHERRY_PRESSURE_PLATE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.LEAVES)).add(Blocks.CHERRY_LEAVES);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WOODEN_TRAPDOORS)).add(Blocks.CHERRY_TRAPDOOR);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.STANDING_SIGNS)).add(Blocks.CHERRY_SIGN);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.WALL_SIGNS)).add(Blocks.CHERRY_WALL_SIGN);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.FENCE_GATES)).add(Blocks.CHERRY_FENCE_GATE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.MINEABLE_WITH_HOE)).add(Blocks.CHERRY_LEAVES);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.FLOWERS)).add(Blocks.CHERRY_LEAVES, Blocks.PINK_PETALS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.INSIDE_STEP_SOUND_BLOCKS)).add(Blocks.PINK_PETALS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.MINEABLE_WITH_HOE)).add(Blocks.PINK_PETALS);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)BlockTags.SAND)).add(Blocks.SUSPICIOUS_SAND);
    }
}

