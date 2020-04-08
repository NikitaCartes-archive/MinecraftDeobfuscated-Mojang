/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.block.Block;

public class BlockTags {
    private static final StaticTagHelper<Block> HELPER = new StaticTagHelper();
    public static final Tag.Named<Block> WOOL = BlockTags.bind("wool");
    public static final Tag.Named<Block> PLANKS = BlockTags.bind("planks");
    public static final Tag.Named<Block> STONE_BRICKS = BlockTags.bind("stone_bricks");
    public static final Tag.Named<Block> WOODEN_BUTTONS = BlockTags.bind("wooden_buttons");
    public static final Tag.Named<Block> BUTTONS = BlockTags.bind("buttons");
    public static final Tag.Named<Block> CARPETS = BlockTags.bind("carpets");
    public static final Tag.Named<Block> WOODEN_DOORS = BlockTags.bind("wooden_doors");
    public static final Tag.Named<Block> WOODEN_STAIRS = BlockTags.bind("wooden_stairs");
    public static final Tag.Named<Block> WOODEN_SLABS = BlockTags.bind("wooden_slabs");
    public static final Tag.Named<Block> WOODEN_FENCES = BlockTags.bind("wooden_fences");
    public static final Tag.Named<Block> WOODEN_PRESSURE_PLATES = BlockTags.bind("wooden_pressure_plates");
    public static final Tag.Named<Block> WOODEN_TRAPDOORS = BlockTags.bind("wooden_trapdoors");
    public static final Tag.Named<Block> DOORS = BlockTags.bind("doors");
    public static final Tag.Named<Block> SAPLINGS = BlockTags.bind("saplings");
    public static final Tag.Named<Block> LOGS_THAT_BURN = BlockTags.bind("logs_that_burn");
    public static final Tag.Named<Block> LOGS = BlockTags.bind("logs");
    public static final Tag.Named<Block> DARK_OAK_LOGS = BlockTags.bind("dark_oak_logs");
    public static final Tag.Named<Block> OAK_LOGS = BlockTags.bind("oak_logs");
    public static final Tag.Named<Block> BIRCH_LOGS = BlockTags.bind("birch_logs");
    public static final Tag.Named<Block> ACACIA_LOGS = BlockTags.bind("acacia_logs");
    public static final Tag.Named<Block> JUNGLE_LOGS = BlockTags.bind("jungle_logs");
    public static final Tag.Named<Block> SPRUCE_LOGS = BlockTags.bind("spruce_logs");
    public static final Tag.Named<Block> CRIMSON_STEMS = BlockTags.bind("crimson_stems");
    public static final Tag.Named<Block> WARPED_STEMS = BlockTags.bind("warped_stems");
    public static final Tag.Named<Block> BANNERS = BlockTags.bind("banners");
    public static final Tag.Named<Block> SAND = BlockTags.bind("sand");
    public static final Tag.Named<Block> STAIRS = BlockTags.bind("stairs");
    public static final Tag.Named<Block> SLABS = BlockTags.bind("slabs");
    public static final Tag.Named<Block> WALLS = BlockTags.bind("walls");
    public static final Tag.Named<Block> ANVIL = BlockTags.bind("anvil");
    public static final Tag.Named<Block> RAILS = BlockTags.bind("rails");
    public static final Tag.Named<Block> LEAVES = BlockTags.bind("leaves");
    public static final Tag.Named<Block> TRAPDOORS = BlockTags.bind("trapdoors");
    public static final Tag.Named<Block> SMALL_FLOWERS = BlockTags.bind("small_flowers");
    public static final Tag.Named<Block> BEDS = BlockTags.bind("beds");
    public static final Tag.Named<Block> FENCES = BlockTags.bind("fences");
    public static final Tag.Named<Block> TALL_FLOWERS = BlockTags.bind("tall_flowers");
    public static final Tag.Named<Block> FLOWERS = BlockTags.bind("flowers");
    public static final Tag.Named<Block> PIGLIN_REPELLENTS = BlockTags.bind("piglin_repellents");
    public static final Tag.Named<Block> GOLD_ORES = BlockTags.bind("gold_ores");
    public static final Tag.Named<Block> NON_FLAMMABLE_WOOD = BlockTags.bind("non_flammable_wood");
    public static final Tag.Named<Block> FLOWER_POTS = BlockTags.bind("flower_pots");
    public static final Tag.Named<Block> ENDERMAN_HOLDABLE = BlockTags.bind("enderman_holdable");
    public static final Tag.Named<Block> ICE = BlockTags.bind("ice");
    public static final Tag.Named<Block> VALID_SPAWN = BlockTags.bind("valid_spawn");
    public static final Tag.Named<Block> IMPERMEABLE = BlockTags.bind("impermeable");
    public static final Tag.Named<Block> UNDERWATER_BONEMEALS = BlockTags.bind("underwater_bonemeals");
    public static final Tag.Named<Block> CORAL_BLOCKS = BlockTags.bind("coral_blocks");
    public static final Tag.Named<Block> WALL_CORALS = BlockTags.bind("wall_corals");
    public static final Tag.Named<Block> CORAL_PLANTS = BlockTags.bind("coral_plants");
    public static final Tag.Named<Block> CORALS = BlockTags.bind("corals");
    public static final Tag.Named<Block> BAMBOO_PLANTABLE_ON = BlockTags.bind("bamboo_plantable_on");
    public static final Tag.Named<Block> STANDING_SIGNS = BlockTags.bind("standing_signs");
    public static final Tag.Named<Block> WALL_SIGNS = BlockTags.bind("wall_signs");
    public static final Tag.Named<Block> SIGNS = BlockTags.bind("signs");
    public static final Tag.Named<Block> DRAGON_IMMUNE = BlockTags.bind("dragon_immune");
    public static final Tag.Named<Block> WITHER_IMMUNE = BlockTags.bind("wither_immune");
    public static final Tag.Named<Block> WITHER_SUMMON_BASE_BLOCKS = BlockTags.bind("wither_summon_base_blocks");
    public static final Tag.Named<Block> BEEHIVES = BlockTags.bind("beehives");
    public static final Tag.Named<Block> CROPS = BlockTags.bind("crops");
    public static final Tag.Named<Block> BEE_GROWABLES = BlockTags.bind("bee_growables");
    public static final Tag.Named<Block> PORTALS = BlockTags.bind("portals");
    public static final Tag.Named<Block> FIRE = BlockTags.bind("fire");
    public static final Tag.Named<Block> NYLIUM = BlockTags.bind("nylium");
    public static final Tag.Named<Block> WART_BLOCKS = BlockTags.bind("wart_blocks");
    public static final Tag.Named<Block> BEACON_BASE_BLOCKS = BlockTags.bind("beacon_base_blocks");
    public static final Tag.Named<Block> SOUL_SPEED_BLOCKS = BlockTags.bind("soul_speed_blocks");
    public static final Tag.Named<Block> WALL_POST_OVERRIDE = BlockTags.bind("wall_post_override");
    public static final Tag.Named<Block> CLIMBABLE = BlockTags.bind("climbable");
    public static final Tag.Named<Block> SHULKER_BOXES = BlockTags.bind("shulker_boxes");
    public static final Tag.Named<Block> HOGLIN_REPELLENTS = BlockTags.bind("hoglin_repellents");
    public static final Tag.Named<Block> SOUL_FIRE_BASE_BLOCKS = BlockTags.bind("soul_fire_base_blocks");
    public static final Tag.Named<Block> STRIDER_WARM_BLOCKS = BlockTags.bind("strider_warm_blocks");
    public static final Tag.Named<Block> CAMPFIRES = BlockTags.bind("campfires");
    public static final Tag.Named<Block> GUARDED_BY_PIGLINS = BlockTags.bind("guarded_by_piglins");

    private static Tag.Named<Block> bind(String string) {
        return HELPER.bind(string);
    }

    public static void reset(TagCollection<Block> tagCollection) {
        HELPER.reset(tagCollection);
    }

    @Environment(value=EnvType.CLIENT)
    public static void resetToEmpty() {
        HELPER.resetToEmpty();
    }

    public static TagCollection<Block> getAllTags() {
        return HELPER.getAllTags();
    }
}

