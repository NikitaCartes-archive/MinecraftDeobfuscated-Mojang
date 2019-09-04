/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.block.Block;

public class BlockTags {
    private static TagCollection<Block> source = new TagCollection(resourceLocation -> Optional.empty(), "", false, "");
    private static int resetCount;
    public static final Tag<Block> WOOL;
    public static final Tag<Block> PLANKS;
    public static final Tag<Block> STONE_BRICKS;
    public static final Tag<Block> WOODEN_BUTTONS;
    public static final Tag<Block> BUTTONS;
    public static final Tag<Block> CARPETS;
    public static final Tag<Block> WOODEN_DOORS;
    public static final Tag<Block> WOODEN_STAIRS;
    public static final Tag<Block> WOODEN_SLABS;
    public static final Tag<Block> WOODEN_FENCES;
    public static final Tag<Block> WOODEN_PRESSURE_PLATES;
    public static final Tag<Block> WOODEN_TRAPDOORS;
    public static final Tag<Block> DOORS;
    public static final Tag<Block> SAPLINGS;
    public static final Tag<Block> LOGS;
    public static final Tag<Block> DARK_OAK_LOGS;
    public static final Tag<Block> OAK_LOGS;
    public static final Tag<Block> BIRCH_LOGS;
    public static final Tag<Block> ACACIA_LOGS;
    public static final Tag<Block> JUNGLE_LOGS;
    public static final Tag<Block> SPRUCE_LOGS;
    public static final Tag<Block> BANNERS;
    public static final Tag<Block> SAND;
    public static final Tag<Block> STAIRS;
    public static final Tag<Block> SLABS;
    public static final Tag<Block> WALLS;
    public static final Tag<Block> ANVIL;
    public static final Tag<Block> RAILS;
    public static final Tag<Block> LEAVES;
    public static final Tag<Block> TRAPDOORS;
    public static final Tag<Block> SMALL_FLOWERS;
    public static final Tag<Block> BEDS;
    public static final Tag<Block> FENCES;
    public static final Tag<Block> TALL_FLOWERS;
    public static final Tag<Block> FLOWERS;
    public static final Tag<Block> FLOWER_POTS;
    public static final Tag<Block> ENDERMAN_HOLDABLE;
    public static final Tag<Block> ICE;
    public static final Tag<Block> VALID_SPAWN;
    public static final Tag<Block> IMPERMEABLE;
    public static final Tag<Block> UNDERWATER_BONEMEALS;
    public static final Tag<Block> CORAL_BLOCKS;
    public static final Tag<Block> WALL_CORALS;
    public static final Tag<Block> CORAL_PLANTS;
    public static final Tag<Block> CORALS;
    public static final Tag<Block> BAMBOO_PLANTABLE_ON;
    public static final Tag<Block> DIRT_LIKE;
    public static final Tag<Block> STANDING_SIGNS;
    public static final Tag<Block> WALL_SIGNS;
    public static final Tag<Block> SIGNS;
    public static final Tag<Block> DRAGON_IMMUNE;
    public static final Tag<Block> WITHER_IMMUNE;
    public static final Tag<Block> BEEHIVES;
    public static final Tag<Block> CROPS;
    public static final Tag<Block> BEE_GROWABLES;

    public static void reset(TagCollection<Block> tagCollection) {
        source = tagCollection;
        ++resetCount;
    }

    public static TagCollection<Block> getAllTags() {
        return source;
    }

    private static Tag<Block> bind(String string) {
        return new Wrapper(new ResourceLocation(string));
    }

    static {
        WOOL = BlockTags.bind("wool");
        PLANKS = BlockTags.bind("planks");
        STONE_BRICKS = BlockTags.bind("stone_bricks");
        WOODEN_BUTTONS = BlockTags.bind("wooden_buttons");
        BUTTONS = BlockTags.bind("buttons");
        CARPETS = BlockTags.bind("carpets");
        WOODEN_DOORS = BlockTags.bind("wooden_doors");
        WOODEN_STAIRS = BlockTags.bind("wooden_stairs");
        WOODEN_SLABS = BlockTags.bind("wooden_slabs");
        WOODEN_FENCES = BlockTags.bind("wooden_fences");
        WOODEN_PRESSURE_PLATES = BlockTags.bind("wooden_pressure_plates");
        WOODEN_TRAPDOORS = BlockTags.bind("wooden_trapdoors");
        DOORS = BlockTags.bind("doors");
        SAPLINGS = BlockTags.bind("saplings");
        LOGS = BlockTags.bind("logs");
        DARK_OAK_LOGS = BlockTags.bind("dark_oak_logs");
        OAK_LOGS = BlockTags.bind("oak_logs");
        BIRCH_LOGS = BlockTags.bind("birch_logs");
        ACACIA_LOGS = BlockTags.bind("acacia_logs");
        JUNGLE_LOGS = BlockTags.bind("jungle_logs");
        SPRUCE_LOGS = BlockTags.bind("spruce_logs");
        BANNERS = BlockTags.bind("banners");
        SAND = BlockTags.bind("sand");
        STAIRS = BlockTags.bind("stairs");
        SLABS = BlockTags.bind("slabs");
        WALLS = BlockTags.bind("walls");
        ANVIL = BlockTags.bind("anvil");
        RAILS = BlockTags.bind("rails");
        LEAVES = BlockTags.bind("leaves");
        TRAPDOORS = BlockTags.bind("trapdoors");
        SMALL_FLOWERS = BlockTags.bind("small_flowers");
        BEDS = BlockTags.bind("beds");
        FENCES = BlockTags.bind("fences");
        TALL_FLOWERS = BlockTags.bind("tall_flowers");
        FLOWERS = BlockTags.bind("flowers");
        FLOWER_POTS = BlockTags.bind("flower_pots");
        ENDERMAN_HOLDABLE = BlockTags.bind("enderman_holdable");
        ICE = BlockTags.bind("ice");
        VALID_SPAWN = BlockTags.bind("valid_spawn");
        IMPERMEABLE = BlockTags.bind("impermeable");
        UNDERWATER_BONEMEALS = BlockTags.bind("underwater_bonemeals");
        CORAL_BLOCKS = BlockTags.bind("coral_blocks");
        WALL_CORALS = BlockTags.bind("wall_corals");
        CORAL_PLANTS = BlockTags.bind("coral_plants");
        CORALS = BlockTags.bind("corals");
        BAMBOO_PLANTABLE_ON = BlockTags.bind("bamboo_plantable_on");
        DIRT_LIKE = BlockTags.bind("dirt_like");
        STANDING_SIGNS = BlockTags.bind("standing_signs");
        WALL_SIGNS = BlockTags.bind("wall_signs");
        SIGNS = BlockTags.bind("signs");
        DRAGON_IMMUNE = BlockTags.bind("dragon_immune");
        WITHER_IMMUNE = BlockTags.bind("wither_immune");
        BEEHIVES = BlockTags.bind("beehives");
        CROPS = BlockTags.bind("crops");
        BEE_GROWABLES = BlockTags.bind("bee_growables");
    }

    static class Wrapper
    extends Tag<Block> {
        private int check = -1;
        private Tag<Block> actual;

        public Wrapper(ResourceLocation resourceLocation) {
            super(resourceLocation);
        }

        @Override
        public boolean contains(Block block) {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.contains(block);
        }

        @Override
        public Collection<Block> getValues() {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.getValues();
        }

        @Override
        public Collection<Tag.Entry<Block>> getSource() {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.getSource();
        }
    }
}

