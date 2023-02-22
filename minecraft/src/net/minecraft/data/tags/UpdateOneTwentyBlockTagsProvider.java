package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyBlockTagsProvider extends IntrinsicHolderTagsProvider<Block> {
	public UpdateOneTwentyBlockTagsProvider(
		PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagsProvider.TagLookup<Block>> completableFuture2
	) {
		super(packOutput, Registries.BLOCK, completableFuture, completableFuture2, block -> block.builtInRegistryHolder().key());
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(BlockTags.PLANKS).add(Blocks.BAMBOO_PLANKS);
		this.tag(BlockTags.WOODEN_BUTTONS).add(Blocks.BAMBOO_BUTTON);
		this.tag(BlockTags.WOODEN_DOORS).add(Blocks.BAMBOO_DOOR);
		this.tag(BlockTags.WOODEN_STAIRS).add(Blocks.BAMBOO_STAIRS);
		this.tag(BlockTags.WOODEN_SLABS).add(Blocks.BAMBOO_SLAB);
		this.tag(BlockTags.WOODEN_FENCES).add(Blocks.BAMBOO_FENCE);
		this.tag(BlockTags.WOODEN_PRESSURE_PLATES).add(Blocks.BAMBOO_PRESSURE_PLATE);
		this.tag(BlockTags.WOODEN_TRAPDOORS).add(Blocks.BAMBOO_TRAPDOOR);
		this.tag(BlockTags.STANDING_SIGNS).add(Blocks.BAMBOO_SIGN);
		this.tag(BlockTags.WALL_SIGNS).add(Blocks.BAMBOO_WALL_SIGN);
		this.tag(BlockTags.FENCE_GATES).add(Blocks.BAMBOO_FENCE_GATE);
		this.tag(BlockTags.BAMBOO_BLOCKS).add(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK);
		this.tag(BlockTags.CEILING_HANGING_SIGNS)
			.add(
				Blocks.OAK_HANGING_SIGN,
				Blocks.SPRUCE_HANGING_SIGN,
				Blocks.BIRCH_HANGING_SIGN,
				Blocks.ACACIA_HANGING_SIGN,
				Blocks.CHERRY_HANGING_SIGN,
				Blocks.JUNGLE_HANGING_SIGN,
				Blocks.DARK_OAK_HANGING_SIGN,
				Blocks.CRIMSON_HANGING_SIGN,
				Blocks.WARPED_HANGING_SIGN,
				Blocks.MANGROVE_HANGING_SIGN,
				Blocks.BAMBOO_HANGING_SIGN
			);
		this.tag(BlockTags.WALL_HANGING_SIGNS)
			.add(
				Blocks.OAK_WALL_HANGING_SIGN,
				Blocks.SPRUCE_WALL_HANGING_SIGN,
				Blocks.BIRCH_WALL_HANGING_SIGN,
				Blocks.ACACIA_WALL_HANGING_SIGN,
				Blocks.CHERRY_WALL_HANGING_SIGN,
				Blocks.JUNGLE_WALL_HANGING_SIGN,
				Blocks.DARK_OAK_WALL_HANGING_SIGN,
				Blocks.CRIMSON_WALL_HANGING_SIGN,
				Blocks.WARPED_WALL_HANGING_SIGN,
				Blocks.MANGROVE_WALL_HANGING_SIGN,
				Blocks.BAMBOO_WALL_HANGING_SIGN
			);
		this.tag(BlockTags.ALL_HANGING_SIGNS).addTag(BlockTags.CEILING_HANGING_SIGNS).addTag(BlockTags.WALL_HANGING_SIGNS);
		this.tag(BlockTags.ALL_SIGNS).addTag(BlockTags.ALL_HANGING_SIGNS);
		this.tag(BlockTags.MINEABLE_WITH_AXE)
			.addTag(BlockTags.ALL_HANGING_SIGNS)
			.add(Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_MOSAIC_SLAB, Blocks.BAMBOO_MOSAIC_STAIRS)
			.addTag(BlockTags.BAMBOO_BLOCKS)
			.add(Blocks.CHISELED_BOOKSHELF);
		this.tag(BlockTags.SNIFFER_DIGGABLE_BLOCK)
			.add(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.MOSS_BLOCK, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS);
		this.tag(BlockTags.SMALL_FLOWERS).add(Blocks.TORCHFLOWER);
		this.tag(BlockTags.CROPS).add(Blocks.TORCHFLOWER_CROP);
		this.tag(BlockTags.CHERRY_LOGS).add(Blocks.CHERRY_LOG, Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_CHERRY_WOOD);
		this.tag(BlockTags.PLANKS).add(Blocks.CHERRY_PLANKS);
		this.tag(BlockTags.WOODEN_BUTTONS).add(Blocks.CHERRY_BUTTON);
		this.tag(BlockTags.WOODEN_DOORS).add(Blocks.CHERRY_DOOR);
		this.tag(BlockTags.WOODEN_STAIRS).add(Blocks.CHERRY_STAIRS);
		this.tag(BlockTags.WOODEN_SLABS).add(Blocks.CHERRY_SLAB);
		this.tag(BlockTags.WOODEN_FENCES).add(Blocks.CHERRY_FENCE);
		this.tag(BlockTags.SAPLINGS).add(Blocks.CHERRY_SAPLING);
		this.tag(BlockTags.LOGS_THAT_BURN).addTag(BlockTags.CHERRY_LOGS);
		this.tag(BlockTags.OVERWORLD_NATURAL_LOGS).add(Blocks.CHERRY_LOG);
		this.tag(BlockTags.FLOWER_POTS).add(Blocks.FLOWER_POT);
		this.tag(BlockTags.WOODEN_PRESSURE_PLATES).add(Blocks.CHERRY_PRESSURE_PLATE);
		this.tag(BlockTags.LEAVES).add(Blocks.CHERRY_LEAVES);
		this.tag(BlockTags.WOODEN_TRAPDOORS).add(Blocks.CHERRY_TRAPDOOR);
		this.tag(BlockTags.STANDING_SIGNS).add(Blocks.CHERRY_SIGN);
		this.tag(BlockTags.WALL_SIGNS).add(Blocks.CHERRY_WALL_SIGN);
		this.tag(BlockTags.FENCE_GATES).add(Blocks.CHERRY_FENCE_GATE);
		this.tag(BlockTags.MINEABLE_WITH_HOE).add(Blocks.CHERRY_LEAVES);
		this.tag(BlockTags.FLOWERS).add(Blocks.CHERRY_LEAVES, Blocks.PINK_PETALS);
		this.tag(BlockTags.INSIDE_STEP_SOUND_BLOCKS).add(Blocks.PINK_PETALS);
		this.tag(BlockTags.MINEABLE_WITH_HOE).add(Blocks.PINK_PETALS);
		this.tag(BlockTags.SAND).add(Blocks.SUSPICIOUS_SAND);
	}
}
