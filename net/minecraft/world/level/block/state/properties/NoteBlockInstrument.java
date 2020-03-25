/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.properties;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public enum NoteBlockInstrument implements StringRepresentable
{
    HARP("harp", SoundEvents.NOTE_BLOCK_HARP),
    BASEDRUM("basedrum", SoundEvents.NOTE_BLOCK_BASEDRUM),
    SNARE("snare", SoundEvents.NOTE_BLOCK_SNARE),
    HAT("hat", SoundEvents.NOTE_BLOCK_HAT),
    BASS("bass", SoundEvents.NOTE_BLOCK_BASS),
    FLUTE("flute", SoundEvents.NOTE_BLOCK_FLUTE),
    BELL("bell", SoundEvents.NOTE_BLOCK_BELL),
    GUITAR("guitar", SoundEvents.NOTE_BLOCK_GUITAR),
    CHIME("chime", SoundEvents.NOTE_BLOCK_CHIME),
    XYLOPHONE("xylophone", SoundEvents.NOTE_BLOCK_XYLOPHONE),
    IRON_XYLOPHONE("iron_xylophone", SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE),
    COW_BELL("cow_bell", SoundEvents.NOTE_BLOCK_COW_BELL),
    DIDGERIDOO("didgeridoo", SoundEvents.NOTE_BLOCK_DIDGERIDOO),
    BIT("bit", SoundEvents.NOTE_BLOCK_BIT),
    BANJO("banjo", SoundEvents.NOTE_BLOCK_BANJO),
    PLING("pling", SoundEvents.NOTE_BLOCK_PLING);

    private final String name;
    private final SoundEvent soundEvent;

    private NoteBlockInstrument(String string2, SoundEvent soundEvent) {
        this.name = string2;
        this.soundEvent = soundEvent;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    public static NoteBlockInstrument byState(BlockState blockState) {
        Block block = blockState.getBlock();
        if (block == Blocks.CLAY) {
            return FLUTE;
        }
        if (block == Blocks.GOLD_BLOCK) {
            return BELL;
        }
        if (block.is(BlockTags.WOOL)) {
            return GUITAR;
        }
        if (block == Blocks.PACKED_ICE) {
            return CHIME;
        }
        if (block == Blocks.BONE_BLOCK) {
            return XYLOPHONE;
        }
        if (block == Blocks.IRON_BLOCK) {
            return IRON_XYLOPHONE;
        }
        if (block == Blocks.SOUL_SAND) {
            return COW_BELL;
        }
        if (block == Blocks.PUMPKIN) {
            return DIDGERIDOO;
        }
        if (block == Blocks.EMERALD_BLOCK) {
            return BIT;
        }
        if (block == Blocks.HAY_BLOCK) {
            return BANJO;
        }
        if (block == Blocks.GLOWSTONE) {
            return PLING;
        }
        Material material = blockState.getMaterial();
        if (material == Material.STONE) {
            return BASEDRUM;
        }
        if (material == Material.SAND) {
            return SNARE;
        }
        if (material == Material.GLASS) {
            return HAT;
        }
        if (material == Material.WOOD || material == Material.NETHER_WOOD) {
            return BASS;
        }
        return HARP;
    }
}

