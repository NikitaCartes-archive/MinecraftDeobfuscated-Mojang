package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class WinterDropBlockTagsProvider extends IntrinsicHolderTagsProvider<Block> {
	public WinterDropBlockTagsProvider(
		PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagsProvider.TagLookup<Block>> completableFuture2
	) {
		super(packOutput, Registries.BLOCK, completableFuture, completableFuture2, block -> block.builtInRegistryHolder().key());
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(BlockTags.PLANKS).add(Blocks.PALE_OAK_PLANKS);
		this.tag(BlockTags.WOODEN_BUTTONS).add(Blocks.PALE_OAK_BUTTON);
		this.tag(BlockTags.WOODEN_DOORS).add(Blocks.PALE_OAK_DOOR);
		this.tag(BlockTags.WOODEN_STAIRS).add(Blocks.PALE_OAK_STAIRS);
		this.tag(BlockTags.WOODEN_SLABS).add(Blocks.PALE_OAK_SLAB);
		this.tag(BlockTags.WOODEN_FENCES).add(Blocks.PALE_OAK_FENCE);
		this.tag(BlockTags.SAPLINGS).add(Blocks.PALE_OAK_SAPLING);
		this.tag(BlockTags.PALE_OAK_LOGS).add(Blocks.PALE_OAK_LOG, Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_LOG, Blocks.STRIPPED_PALE_OAK_WOOD);
		this.tag(BlockTags.LOGS_THAT_BURN).addTag(BlockTags.PALE_OAK_LOGS);
		this.tag(BlockTags.OVERWORLD_NATURAL_LOGS).addTag(BlockTags.PALE_OAK_LOGS);
		this.tag(BlockTags.DIRT).add(Blocks.PALE_MOSS_BLOCK);
		this.tag(BlockTags.FLOWER_POTS).add(Blocks.POTTED_PALE_OAK_SAPLING);
		this.tag(BlockTags.WOODEN_PRESSURE_PLATES).add(Blocks.PALE_OAK_PRESSURE_PLATE);
		this.tag(BlockTags.LEAVES).add(Blocks.PALE_OAK_LEAVES);
		this.tag(BlockTags.WOODEN_TRAPDOORS).add(Blocks.PALE_OAK_TRAPDOOR);
		this.tag(BlockTags.STANDING_SIGNS).add(Blocks.PALE_OAK_SIGN);
		this.tag(BlockTags.WALL_SIGNS).add(Blocks.PALE_OAK_WALL_SIGN);
		this.tag(BlockTags.CEILING_HANGING_SIGNS).add(Blocks.PALE_OAK_HANGING_SIGN);
		this.tag(BlockTags.WALL_HANGING_SIGNS).add(Blocks.PALE_OAK_WALL_HANGING_SIGN);
		this.tag(BlockTags.FENCE_GATES).add(Blocks.PALE_OAK_FENCE_GATE);
		this.tag(BlockTags.MINEABLE_WITH_HOE).add(Blocks.PALE_OAK_LEAVES);
		this.tag(BlockTags.MINEABLE_WITH_AXE).add(Blocks.CREAKING_HEART);
	}
}
