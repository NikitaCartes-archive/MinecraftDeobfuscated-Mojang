package net.minecraft.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyBlockTagsProvider extends TagsProvider<Block> {
	public UpdateOneTwentyBlockTagsProvider(PackOutput packOutput) {
		super(packOutput, Registry.BLOCK);
	}

	@Override
	protected void addTags() {
		this.tag(BlockTags.PLANKS).add(Blocks.BAMBOO_PLANKS);
		this.tag(BlockTags.WOODEN_BUTTONS).add(Blocks.BAMBOO_BUTTON);
		this.tag(BlockTags.WOODEN_DOORS).add(Blocks.BAMBOO_DOOR);
		this.tag(BlockTags.WOODEN_STAIRS).add(Blocks.BAMBOO_STAIRS, Blocks.BAMBOO_MOSAIC_STAIRS);
		this.tag(BlockTags.WOODEN_SLABS).add(Blocks.BAMBOO_SLAB, Blocks.BAMBOO_MOSAIC_SLAB);
		this.tag(BlockTags.WOODEN_FENCES).add(Blocks.BAMBOO_FENCE);
		this.tag(BlockTags.WOODEN_PRESSURE_PLATES).add(Blocks.BAMBOO_PRESSURE_PLATE);
		this.tag(BlockTags.WOODEN_TRAPDOORS).add(Blocks.BAMBOO_TRAPDOOR);
		this.tag(BlockTags.STANDING_SIGNS).add(Blocks.BAMBOO_SIGN);
		this.tag(BlockTags.WALL_SIGNS).add(Blocks.BAMBOO_WALL_SIGN);
		this.tag(BlockTags.FENCE_GATES).add(Blocks.BAMBOO_FENCE_GATE);
		this.tag(BlockTags.CEILING_HANGING_SIGNS)
			.add(
				Blocks.OAK_HANGING_SIGN,
				Blocks.SPRUCE_HANGING_SIGN,
				Blocks.BIRCH_HANGING_SIGN,
				Blocks.ACACIA_HANGING_SIGN,
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
				Blocks.JUNGLE_WALL_HANGING_SIGN,
				Blocks.DARK_OAK_WALL_HANGING_SIGN,
				Blocks.CRIMSON_WALL_HANGING_SIGN,
				Blocks.WARPED_WALL_HANGING_SIGN,
				Blocks.MANGROVE_WALL_HANGING_SIGN,
				Blocks.BAMBOO_WALL_HANGING_SIGN
			);
		this.tag(BlockTags.ALL_HANGING_SIGNS).addTag(BlockTags.CEILING_HANGING_SIGNS).addTag(BlockTags.WALL_HANGING_SIGNS);
		this.tag(BlockTags.MINEABLE_WITH_AXE).addTag(BlockTags.ALL_HANGING_SIGNS).add(Blocks.BAMBOO_MOSAIC, Blocks.CHISELED_BOOKSHELF);
	}
}
