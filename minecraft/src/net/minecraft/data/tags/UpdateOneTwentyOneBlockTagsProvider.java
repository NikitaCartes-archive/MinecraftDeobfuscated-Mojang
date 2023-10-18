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
		this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Blocks.CRAFTER);
		this.tag(BlockTags.NEEDS_STONE_TOOL).add(Blocks.CRAFTER);
	}
}
