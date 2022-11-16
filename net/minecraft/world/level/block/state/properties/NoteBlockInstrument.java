/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.properties;

import java.util.Optional;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
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
    PLING("pling", SoundEvents.NOTE_BLOCK_PLING),
    ZOMBIE("zombie", SoundEvents.ZOMBIE_AMBIENT, true),
    SKELETON("skeleton", SoundEvents.SKELETON_AMBIENT, true),
    CREEPER("creeper", SoundEvents.CREEPER_PRIMED, true),
    DRAGON("dragon", SoundEvents.ENDER_DRAGON_AMBIENT, true),
    WITHER_SKELETON("wither_skeleton", SoundEvents.WITHER_SKELETON_AMBIENT, true),
    PIGLIN("piglin", SoundEvents.PIGLIN_ANGRY, true);

    private final String name;
    private final SoundEvent soundEvent;
    private final boolean isMobHead;

    private NoteBlockInstrument(String string2, SoundEvent soundEvent, boolean bl) {
        this.name = string2;
        this.soundEvent = soundEvent;
        this.isMobHead = bl;
    }

    private NoteBlockInstrument(String string2, SoundEvent soundEvent) {
        this(string2, soundEvent, false);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    public boolean isMobHeadInstrument() {
        return this.isMobHead;
    }

    public static Optional<NoteBlockInstrument> byStateAbove(BlockState blockState) {
        if (blockState.is(Blocks.ZOMBIE_HEAD)) {
            return Optional.of(ZOMBIE);
        }
        if (blockState.is(Blocks.SKELETON_SKULL)) {
            return Optional.of(SKELETON);
        }
        if (blockState.is(Blocks.CREEPER_HEAD)) {
            return Optional.of(CREEPER);
        }
        if (blockState.is(Blocks.DRAGON_HEAD)) {
            return Optional.of(DRAGON);
        }
        if (blockState.is(Blocks.WITHER_SKELETON_SKULL)) {
            return Optional.of(WITHER_SKELETON);
        }
        if (blockState.is(Blocks.PIGLIN_HEAD)) {
            return Optional.of(PIGLIN);
        }
        return Optional.empty();
    }

    public static NoteBlockInstrument byStateBelow(BlockState blockState) {
        if (blockState.is(Blocks.CLAY)) {
            return FLUTE;
        }
        if (blockState.is(Blocks.GOLD_BLOCK)) {
            return BELL;
        }
        if (blockState.is(BlockTags.WOOL)) {
            return GUITAR;
        }
        if (blockState.is(Blocks.PACKED_ICE)) {
            return CHIME;
        }
        if (blockState.is(Blocks.BONE_BLOCK)) {
            return XYLOPHONE;
        }
        if (blockState.is(Blocks.IRON_BLOCK)) {
            return IRON_XYLOPHONE;
        }
        if (blockState.is(Blocks.SOUL_SAND)) {
            return COW_BELL;
        }
        if (blockState.is(Blocks.PUMPKIN)) {
            return DIDGERIDOO;
        }
        if (blockState.is(Blocks.EMERALD_BLOCK)) {
            return BIT;
        }
        if (blockState.is(Blocks.HAY_BLOCK)) {
            return BANJO;
        }
        if (blockState.is(Blocks.GLOWSTONE)) {
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

