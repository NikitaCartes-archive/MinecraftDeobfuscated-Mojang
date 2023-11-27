package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyOneBlockTagsProvider extends IntrinsicHolderTagsProvider<Block> {
	public UpdateOneTwentyOneBlockTagsProvider(
		PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagsProvider.TagLookup<Block>> completableFuture2
	) {
		super(packOutput, Registries.BLOCK, completableFuture, completableFuture2, block -> block.builtInRegistryHolder().key());
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
			.add(
				Blocks.CRAFTER,
				Blocks.TUFF_SLAB,
				Blocks.TUFF_STAIRS,
				Blocks.TUFF_WALL,
				Blocks.CHISELED_TUFF,
				Blocks.POLISHED_TUFF,
				Blocks.POLISHED_TUFF_SLAB,
				Blocks.POLISHED_TUFF_STAIRS,
				Blocks.POLISHED_TUFF_WALL,
				Blocks.TUFF_BRICKS,
				Blocks.TUFF_BRICK_SLAB,
				Blocks.TUFF_BRICK_STAIRS,
				Blocks.TUFF_BRICK_WALL,
				Blocks.CHISELED_TUFF_BRICKS,
				Blocks.CHISELED_COPPER,
				Blocks.EXPOSED_CHISELED_COPPER,
				Blocks.WEATHERED_CHISELED_COPPER,
				Blocks.OXIDIZED_CHISELED_COPPER,
				Blocks.WAXED_CHISELED_COPPER,
				Blocks.WAXED_EXPOSED_CHISELED_COPPER,
				Blocks.WAXED_WEATHERED_CHISELED_COPPER,
				Blocks.WAXED_OXIDIZED_CHISELED_COPPER,
				Blocks.COPPER_GRATE,
				Blocks.EXPOSED_COPPER_GRATE,
				Blocks.WEATHERED_COPPER_GRATE,
				Blocks.OXIDIZED_COPPER_GRATE,
				Blocks.WAXED_COPPER_GRATE,
				Blocks.WAXED_EXPOSED_COPPER_GRATE,
				Blocks.WAXED_WEATHERED_COPPER_GRATE,
				Blocks.WAXED_OXIDIZED_COPPER_GRATE,
				Blocks.COPPER_BULB,
				Blocks.EXPOSED_COPPER_BULB,
				Blocks.WEATHERED_COPPER_BULB,
				Blocks.OXIDIZED_COPPER_BULB,
				Blocks.WAXED_COPPER_BULB,
				Blocks.WAXED_EXPOSED_COPPER_BULB,
				Blocks.WAXED_WEATHERED_COPPER_BULB,
				Blocks.WAXED_OXIDIZED_COPPER_BULB,
				Blocks.COPPER_DOOR,
				Blocks.EXPOSED_COPPER_DOOR,
				Blocks.WEATHERED_COPPER_DOOR,
				Blocks.OXIDIZED_COPPER_DOOR,
				Blocks.WAXED_COPPER_DOOR,
				Blocks.WAXED_EXPOSED_COPPER_DOOR,
				Blocks.WAXED_WEATHERED_COPPER_DOOR,
				Blocks.WAXED_OXIDIZED_COPPER_DOOR,
				Blocks.COPPER_TRAPDOOR,
				Blocks.EXPOSED_COPPER_TRAPDOOR,
				Blocks.WEATHERED_COPPER_TRAPDOOR,
				Blocks.OXIDIZED_COPPER_TRAPDOOR,
				Blocks.WAXED_COPPER_TRAPDOOR,
				Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR,
				Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR,
				Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR
			);
		this.tag(BlockTags.STAIRS).add(Blocks.TUFF_STAIRS, Blocks.POLISHED_TUFF_STAIRS, Blocks.TUFF_BRICK_STAIRS);
		this.tag(BlockTags.SLABS).add(Blocks.TUFF_SLAB, Blocks.POLISHED_TUFF_SLAB, Blocks.TUFF_BRICK_SLAB);
		this.tag(BlockTags.WALLS).add(Blocks.TUFF_WALL, Blocks.POLISHED_TUFF_WALL, Blocks.TUFF_BRICK_WALL);
		this.tag(BlockTags.NEEDS_STONE_TOOL)
			.add(
				Blocks.CRAFTER,
				Blocks.CHISELED_COPPER,
				Blocks.EXPOSED_CHISELED_COPPER,
				Blocks.WEATHERED_CHISELED_COPPER,
				Blocks.OXIDIZED_CHISELED_COPPER,
				Blocks.WAXED_CHISELED_COPPER,
				Blocks.WAXED_EXPOSED_CHISELED_COPPER,
				Blocks.WAXED_WEATHERED_CHISELED_COPPER,
				Blocks.WAXED_OXIDIZED_CHISELED_COPPER,
				Blocks.COPPER_GRATE,
				Blocks.EXPOSED_COPPER_GRATE,
				Blocks.WEATHERED_COPPER_GRATE,
				Blocks.OXIDIZED_COPPER_GRATE,
				Blocks.WAXED_COPPER_GRATE,
				Blocks.WAXED_EXPOSED_COPPER_GRATE,
				Blocks.WAXED_WEATHERED_COPPER_GRATE,
				Blocks.WAXED_OXIDIZED_COPPER_GRATE,
				Blocks.COPPER_BULB,
				Blocks.EXPOSED_COPPER_BULB,
				Blocks.WEATHERED_COPPER_BULB,
				Blocks.OXIDIZED_COPPER_BULB,
				Blocks.WAXED_COPPER_BULB,
				Blocks.WAXED_EXPOSED_COPPER_BULB,
				Blocks.WAXED_WEATHERED_COPPER_BULB,
				Blocks.WAXED_OXIDIZED_COPPER_BULB,
				Blocks.COPPER_TRAPDOOR,
				Blocks.EXPOSED_COPPER_TRAPDOOR,
				Blocks.WEATHERED_COPPER_TRAPDOOR,
				Blocks.OXIDIZED_COPPER_TRAPDOOR,
				Blocks.WAXED_COPPER_TRAPDOOR,
				Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR,
				Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR,
				Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR
			);
		this.tag(BlockTags.WOODEN_DOORS)
			.add(
				Blocks.COPPER_DOOR,
				Blocks.EXPOSED_COPPER_DOOR,
				Blocks.WEATHERED_COPPER_DOOR,
				Blocks.OXIDIZED_COPPER_DOOR,
				Blocks.WAXED_COPPER_DOOR,
				Blocks.WAXED_EXPOSED_COPPER_DOOR,
				Blocks.WAXED_WEATHERED_COPPER_DOOR,
				Blocks.WAXED_OXIDIZED_COPPER_DOOR
			);
		this.tag(BlockTags.FEATURES_CANNOT_REPLACE).add(Blocks.TRIAL_SPAWNER);
		this.tag(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE).addTag(BlockTags.FEATURES_CANNOT_REPLACE);
		this.tag(BlockTags.TRAPDOORS)
			.add(
				Blocks.COPPER_TRAPDOOR,
				Blocks.EXPOSED_COPPER_TRAPDOOR,
				Blocks.WEATHERED_COPPER_TRAPDOOR,
				Blocks.OXIDIZED_COPPER_TRAPDOOR,
				Blocks.WAXED_COPPER_TRAPDOOR,
				Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR,
				Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR,
				Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR
			);
		this.tag(BlockTags.DOORS)
			.add(
				Blocks.COPPER_DOOR,
				Blocks.EXPOSED_COPPER_DOOR,
				Blocks.WEATHERED_COPPER_DOOR,
				Blocks.OXIDIZED_COPPER_DOOR,
				Blocks.WAXED_COPPER_DOOR,
				Blocks.WAXED_EXPOSED_COPPER_DOOR,
				Blocks.WAXED_WEATHERED_COPPER_DOOR,
				Blocks.WAXED_OXIDIZED_COPPER_DOOR
			);
	}
}
