package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class UpdateOneTwentyOneItemTagsProvider extends ItemTagsProvider {
	public UpdateOneTwentyOneItemTagsProvider(
		PackOutput packOutput,
		CompletableFuture<HolderLookup.Provider> completableFuture,
		CompletableFuture<TagsProvider.TagLookup<Item>> completableFuture2,
		CompletableFuture<TagsProvider.TagLookup<Block>> completableFuture3
	) {
		super(packOutput, completableFuture, completableFuture2, completableFuture3);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(ItemTags.STAIRS).add(Items.TUFF_STAIRS, Items.POLISHED_TUFF_STAIRS, Items.TUFF_BRICK_STAIRS);
		this.tag(ItemTags.SLABS).add(Items.TUFF_SLAB, Items.POLISHED_TUFF_SLAB, Items.TUFF_BRICK_SLAB);
		this.tag(ItemTags.WALLS).add(Items.TUFF_WALL, Items.POLISHED_TUFF_WALL, Items.TUFF_BRICK_WALL);
		this.tag(ItemTags.DOORS)
			.add(
				Items.COPPER_DOOR,
				Items.EXPOSED_COPPER_DOOR,
				Items.WEATHERED_COPPER_DOOR,
				Items.OXIDIZED_COPPER_DOOR,
				Items.WAXED_COPPER_DOOR,
				Items.WAXED_EXPOSED_COPPER_DOOR,
				Items.WAXED_WEATHERED_COPPER_DOOR,
				Items.WAXED_OXIDIZED_COPPER_DOOR
			);
		this.tag(ItemTags.TRAPDOORS)
			.add(
				Items.COPPER_TRAPDOOR,
				Items.EXPOSED_COPPER_TRAPDOOR,
				Items.WEATHERED_COPPER_TRAPDOOR,
				Items.OXIDIZED_COPPER_TRAPDOOR,
				Items.WAXED_COPPER_TRAPDOOR,
				Items.WAXED_EXPOSED_COPPER_TRAPDOOR,
				Items.WAXED_WEATHERED_COPPER_TRAPDOOR,
				Items.WAXED_OXIDIZED_COPPER_TRAPDOOR
			);
	}
}
