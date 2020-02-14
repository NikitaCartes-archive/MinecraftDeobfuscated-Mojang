package net.minecraft.tags;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemTags {
	private static TagCollection<Item> source = new TagCollection<>(resourceLocation -> Optional.empty(), "", false, "");
	private static int resetCount;
	public static final Tag<Item> WOOL = bind("wool");
	public static final Tag<Item> PLANKS = bind("planks");
	public static final Tag<Item> STONE_BRICKS = bind("stone_bricks");
	public static final Tag<Item> WOODEN_BUTTONS = bind("wooden_buttons");
	public static final Tag<Item> BUTTONS = bind("buttons");
	public static final Tag<Item> CARPETS = bind("carpets");
	public static final Tag<Item> WOODEN_DOORS = bind("wooden_doors");
	public static final Tag<Item> WOODEN_STAIRS = bind("wooden_stairs");
	public static final Tag<Item> WOODEN_SLABS = bind("wooden_slabs");
	public static final Tag<Item> WOODEN_FENCES = bind("wooden_fences");
	public static final Tag<Item> WOODEN_PRESSURE_PLATES = bind("wooden_pressure_plates");
	public static final Tag<Item> WOODEN_TRAPDOORS = bind("wooden_trapdoors");
	public static final Tag<Item> DOORS = bind("doors");
	public static final Tag<Item> SAPLINGS = bind("saplings");
	public static final Tag<Item> LOGS = bind("logs");
	public static final Tag<Item> DARK_OAK_LOGS = bind("dark_oak_logs");
	public static final Tag<Item> OAK_LOGS = bind("oak_logs");
	public static final Tag<Item> BIRCH_LOGS = bind("birch_logs");
	public static final Tag<Item> ACACIA_LOGS = bind("acacia_logs");
	public static final Tag<Item> JUNGLE_LOGS = bind("jungle_logs");
	public static final Tag<Item> SPRUCE_LOGS = bind("spruce_logs");
	public static final Tag<Item> CRIMSON_STEMS = bind("crimson_stems");
	public static final Tag<Item> WARPED_STEMS = bind("warped_stems");
	public static final Tag<Item> BANNERS = bind("banners");
	public static final Tag<Item> SAND = bind("sand");
	public static final Tag<Item> STAIRS = bind("stairs");
	public static final Tag<Item> SLABS = bind("slabs");
	public static final Tag<Item> WALLS = bind("walls");
	public static final Tag<Item> ANVIL = bind("anvil");
	public static final Tag<Item> RAILS = bind("rails");
	public static final Tag<Item> LEAVES = bind("leaves");
	public static final Tag<Item> TRAPDOORS = bind("trapdoors");
	public static final Tag<Item> SMALL_FLOWERS = bind("small_flowers");
	public static final Tag<Item> BEDS = bind("beds");
	public static final Tag<Item> FENCES = bind("fences");
	public static final Tag<Item> TALL_FLOWERS = bind("tall_flowers");
	public static final Tag<Item> FLOWERS = bind("flowers");
	public static final Tag<Item> BOATS = bind("boats");
	public static final Tag<Item> FISHES = bind("fishes");
	public static final Tag<Item> SIGNS = bind("signs");
	public static final Tag<Item> MUSIC_DISCS = bind("music_discs");
	public static final Tag<Item> COALS = bind("coals");
	public static final Tag<Item> ARROWS = bind("arrows");
	public static final Tag<Item> LECTERN_BOOKS = bind("lectern_books");
	public static final Tag<Item> BEACON_PAYMENT_ITEMS = bind("beacon_payment_items");

	public static void reset(TagCollection<Item> tagCollection) {
		source = tagCollection;
		resetCount++;
	}

	public static TagCollection<Item> getAllTags() {
		return source;
	}

	private static Tag<Item> bind(String string) {
		return new ItemTags.Wrapper(new ResourceLocation(string));
	}

	public static class Wrapper extends Tag<Item> {
		private int check = -1;
		private Tag<Item> actual;

		public Wrapper(ResourceLocation resourceLocation) {
			super(resourceLocation);
		}

		public boolean contains(Item item) {
			if (this.check != ItemTags.resetCount) {
				this.actual = ItemTags.source.getTagOrEmpty(this.getId());
				this.check = ItemTags.resetCount;
			}

			return this.actual.contains(item);
		}

		@Override
		public Collection<Item> getValues() {
			if (this.check != ItemTags.resetCount) {
				this.actual = ItemTags.source.getTagOrEmpty(this.getId());
				this.check = ItemTags.resetCount;
			}

			return this.actual.getValues();
		}

		@Override
		public Collection<Tag.Entry<Item>> getSource() {
			if (this.check != ItemTags.resetCount) {
				this.actual = ItemTags.source.getTagOrEmpty(this.getId());
				this.check = ItemTags.resetCount;
			}

			return this.actual.getSource();
		}
	}
}
