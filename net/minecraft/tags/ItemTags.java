/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.StaticTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.item.Item;

public final class ItemTags {
    protected static final StaticTagHelper<Item> HELPER = StaticTags.create(new ResourceLocation("item"), TagContainer::getItems);
    public static final Tag.Named<Item> WOOL = ItemTags.bind("wool");
    public static final Tag.Named<Item> PLANKS = ItemTags.bind("planks");
    public static final Tag.Named<Item> STONE_BRICKS = ItemTags.bind("stone_bricks");
    public static final Tag.Named<Item> WOODEN_BUTTONS = ItemTags.bind("wooden_buttons");
    public static final Tag.Named<Item> BUTTONS = ItemTags.bind("buttons");
    public static final Tag.Named<Item> CARPETS = ItemTags.bind("carpets");
    public static final Tag.Named<Item> WOODEN_DOORS = ItemTags.bind("wooden_doors");
    public static final Tag.Named<Item> WOODEN_STAIRS = ItemTags.bind("wooden_stairs");
    public static final Tag.Named<Item> WOODEN_SLABS = ItemTags.bind("wooden_slabs");
    public static final Tag.Named<Item> WOODEN_FENCES = ItemTags.bind("wooden_fences");
    public static final Tag.Named<Item> WOODEN_PRESSURE_PLATES = ItemTags.bind("wooden_pressure_plates");
    public static final Tag.Named<Item> WOODEN_TRAPDOORS = ItemTags.bind("wooden_trapdoors");
    public static final Tag.Named<Item> DOORS = ItemTags.bind("doors");
    public static final Tag.Named<Item> SAPLINGS = ItemTags.bind("saplings");
    public static final Tag.Named<Item> LOGS_THAT_BURN = ItemTags.bind("logs_that_burn");
    public static final Tag.Named<Item> LOGS = ItemTags.bind("logs");
    public static final Tag.Named<Item> DARK_OAK_LOGS = ItemTags.bind("dark_oak_logs");
    public static final Tag.Named<Item> OAK_LOGS = ItemTags.bind("oak_logs");
    public static final Tag.Named<Item> BIRCH_LOGS = ItemTags.bind("birch_logs");
    public static final Tag.Named<Item> ACACIA_LOGS = ItemTags.bind("acacia_logs");
    public static final Tag.Named<Item> JUNGLE_LOGS = ItemTags.bind("jungle_logs");
    public static final Tag.Named<Item> SPRUCE_LOGS = ItemTags.bind("spruce_logs");
    public static final Tag.Named<Item> CRIMSON_STEMS = ItemTags.bind("crimson_stems");
    public static final Tag.Named<Item> WARPED_STEMS = ItemTags.bind("warped_stems");
    public static final Tag.Named<Item> BANNERS = ItemTags.bind("banners");
    public static final Tag.Named<Item> SAND = ItemTags.bind("sand");
    public static final Tag.Named<Item> STAIRS = ItemTags.bind("stairs");
    public static final Tag.Named<Item> SLABS = ItemTags.bind("slabs");
    public static final Tag.Named<Item> WALLS = ItemTags.bind("walls");
    public static final Tag.Named<Item> ANVIL = ItemTags.bind("anvil");
    public static final Tag.Named<Item> RAILS = ItemTags.bind("rails");
    public static final Tag.Named<Item> LEAVES = ItemTags.bind("leaves");
    public static final Tag.Named<Item> TRAPDOORS = ItemTags.bind("trapdoors");
    public static final Tag.Named<Item> SMALL_FLOWERS = ItemTags.bind("small_flowers");
    public static final Tag.Named<Item> BEDS = ItemTags.bind("beds");
    public static final Tag.Named<Item> FENCES = ItemTags.bind("fences");
    public static final Tag.Named<Item> TALL_FLOWERS = ItemTags.bind("tall_flowers");
    public static final Tag.Named<Item> FLOWERS = ItemTags.bind("flowers");
    public static final Tag.Named<Item> PIGLIN_REPELLENTS = ItemTags.bind("piglin_repellents");
    public static final Tag.Named<Item> PIGLIN_LOVED = ItemTags.bind("piglin_loved");
    public static final Tag.Named<Item> GOLD_ORES = ItemTags.bind("gold_ores");
    public static final Tag.Named<Item> NON_FLAMMABLE_WOOD = ItemTags.bind("non_flammable_wood");
    public static final Tag.Named<Item> SOUL_FIRE_BASE_BLOCKS = ItemTags.bind("soul_fire_base_blocks");
    public static final Tag.Named<Item> BOATS = ItemTags.bind("boats");
    public static final Tag.Named<Item> FISHES = ItemTags.bind("fishes");
    public static final Tag.Named<Item> SIGNS = ItemTags.bind("signs");
    public static final Tag.Named<Item> MUSIC_DISCS = ItemTags.bind("music_discs");
    public static final Tag.Named<Item> CREEPER_DROP_MUSIC_DISCS = ItemTags.bind("creeper_drop_music_discs");
    public static final Tag.Named<Item> COALS = ItemTags.bind("coals");
    public static final Tag.Named<Item> ARROWS = ItemTags.bind("arrows");
    public static final Tag.Named<Item> LECTERN_BOOKS = ItemTags.bind("lectern_books");
    public static final Tag.Named<Item> BEACON_PAYMENT_ITEMS = ItemTags.bind("beacon_payment_items");
    public static final Tag.Named<Item> STONE_TOOL_MATERIALS = ItemTags.bind("stone_tool_materials");
    public static final Tag.Named<Item> STONE_CRAFTING_MATERIALS = ItemTags.bind("stone_crafting_materials");

    private static Tag.Named<Item> bind(String string) {
        return HELPER.bind(string);
    }

    public static TagCollection<Item> getAllTags() {
        return HELPER.getAllTags();
    }
}

