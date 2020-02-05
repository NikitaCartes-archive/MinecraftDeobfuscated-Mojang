/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;

public class ItemTags {
    private static TagCollection<Item> source = new TagCollection(resourceLocation -> Optional.empty(), "", false, "");
    private static int resetCount;
    public static final Tag<Item> WOOL;
    public static final Tag<Item> PLANKS;
    public static final Tag<Item> STONE_BRICKS;
    public static final Tag<Item> WOODEN_BUTTONS;
    public static final Tag<Item> BUTTONS;
    public static final Tag<Item> CARPETS;
    public static final Tag<Item> WOODEN_DOORS;
    public static final Tag<Item> WOODEN_STAIRS;
    public static final Tag<Item> WOODEN_SLABS;
    public static final Tag<Item> WOODEN_FENCES;
    public static final Tag<Item> WOODEN_PRESSURE_PLATES;
    public static final Tag<Item> WOODEN_TRAPDOORS;
    public static final Tag<Item> DOORS;
    public static final Tag<Item> SAPLINGS;
    public static final Tag<Item> LOGS;
    public static final Tag<Item> DARK_OAK_LOGS;
    public static final Tag<Item> OAK_LOGS;
    public static final Tag<Item> BIRCH_LOGS;
    public static final Tag<Item> ACACIA_LOGS;
    public static final Tag<Item> JUNGLE_LOGS;
    public static final Tag<Item> SPRUCE_LOGS;
    public static final Tag<Item> CRIMSON_STEMS;
    public static final Tag<Item> WARPED_STEMS;
    public static final Tag<Item> BANNERS;
    public static final Tag<Item> SAND;
    public static final Tag<Item> STAIRS;
    public static final Tag<Item> SLABS;
    public static final Tag<Item> WALLS;
    public static final Tag<Item> ANVIL;
    public static final Tag<Item> RAILS;
    public static final Tag<Item> LEAVES;
    public static final Tag<Item> TRAPDOORS;
    public static final Tag<Item> SMALL_FLOWERS;
    public static final Tag<Item> BEDS;
    public static final Tag<Item> FENCES;
    public static final Tag<Item> TALL_FLOWERS;
    public static final Tag<Item> FLOWERS;
    public static final Tag<Item> BOATS;
    public static final Tag<Item> FISHES;
    public static final Tag<Item> SIGNS;
    public static final Tag<Item> MUSIC_DISCS;
    public static final Tag<Item> COALS;
    public static final Tag<Item> ARROWS;
    public static final Tag<Item> LECTERN_BOOKS;

    public static void reset(TagCollection<Item> tagCollection) {
        source = tagCollection;
        ++resetCount;
    }

    public static TagCollection<Item> getAllTags() {
        return source;
    }

    private static Tag<Item> bind(String string) {
        return new Wrapper(new ResourceLocation(string));
    }

    static {
        WOOL = ItemTags.bind("wool");
        PLANKS = ItemTags.bind("planks");
        STONE_BRICKS = ItemTags.bind("stone_bricks");
        WOODEN_BUTTONS = ItemTags.bind("wooden_buttons");
        BUTTONS = ItemTags.bind("buttons");
        CARPETS = ItemTags.bind("carpets");
        WOODEN_DOORS = ItemTags.bind("wooden_doors");
        WOODEN_STAIRS = ItemTags.bind("wooden_stairs");
        WOODEN_SLABS = ItemTags.bind("wooden_slabs");
        WOODEN_FENCES = ItemTags.bind("wooden_fences");
        WOODEN_PRESSURE_PLATES = ItemTags.bind("wooden_pressure_plates");
        WOODEN_TRAPDOORS = ItemTags.bind("wooden_trapdoors");
        DOORS = ItemTags.bind("doors");
        SAPLINGS = ItemTags.bind("saplings");
        LOGS = ItemTags.bind("logs");
        DARK_OAK_LOGS = ItemTags.bind("dark_oak_logs");
        OAK_LOGS = ItemTags.bind("oak_logs");
        BIRCH_LOGS = ItemTags.bind("birch_logs");
        ACACIA_LOGS = ItemTags.bind("acacia_logs");
        JUNGLE_LOGS = ItemTags.bind("jungle_logs");
        SPRUCE_LOGS = ItemTags.bind("spruce_logs");
        CRIMSON_STEMS = ItemTags.bind("crimson_stems");
        WARPED_STEMS = ItemTags.bind("warped_stems");
        BANNERS = ItemTags.bind("banners");
        SAND = ItemTags.bind("sand");
        STAIRS = ItemTags.bind("stairs");
        SLABS = ItemTags.bind("slabs");
        WALLS = ItemTags.bind("walls");
        ANVIL = ItemTags.bind("anvil");
        RAILS = ItemTags.bind("rails");
        LEAVES = ItemTags.bind("leaves");
        TRAPDOORS = ItemTags.bind("trapdoors");
        SMALL_FLOWERS = ItemTags.bind("small_flowers");
        BEDS = ItemTags.bind("beds");
        FENCES = ItemTags.bind("fences");
        TALL_FLOWERS = ItemTags.bind("tall_flowers");
        FLOWERS = ItemTags.bind("flowers");
        BOATS = ItemTags.bind("boats");
        FISHES = ItemTags.bind("fishes");
        SIGNS = ItemTags.bind("signs");
        MUSIC_DISCS = ItemTags.bind("music_discs");
        COALS = ItemTags.bind("coals");
        ARROWS = ItemTags.bind("arrows");
        LECTERN_BOOKS = ItemTags.bind("lectern_books");
    }

    public static class Wrapper
    extends Tag<Item> {
        private int check = -1;
        private Tag<Item> actual;

        public Wrapper(ResourceLocation resourceLocation) {
            super(resourceLocation);
        }

        @Override
        public boolean contains(Item item) {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.contains(item);
        }

        @Override
        public Collection<Item> getValues() {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.getValues();
        }

        @Override
        public Collection<Tag.Entry<Item>> getSource() {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.getSource();
        }
    }
}

