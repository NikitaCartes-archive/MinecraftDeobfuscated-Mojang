/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.properties;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public enum NoteBlockInstrument implements StringRepresentable
{
    HARP("harp", SoundEvents.NOTE_BLOCK_HARP, Type.BASE_BLOCK),
    BASEDRUM("basedrum", SoundEvents.NOTE_BLOCK_BASEDRUM, Type.BASE_BLOCK),
    SNARE("snare", SoundEvents.NOTE_BLOCK_SNARE, Type.BASE_BLOCK),
    HAT("hat", SoundEvents.NOTE_BLOCK_HAT, Type.BASE_BLOCK),
    BASS("bass", SoundEvents.NOTE_BLOCK_BASS, Type.BASE_BLOCK),
    FLUTE("flute", SoundEvents.NOTE_BLOCK_FLUTE, Type.BASE_BLOCK),
    BELL("bell", SoundEvents.NOTE_BLOCK_BELL, Type.BASE_BLOCK),
    GUITAR("guitar", SoundEvents.NOTE_BLOCK_GUITAR, Type.BASE_BLOCK),
    CHIME("chime", SoundEvents.NOTE_BLOCK_CHIME, Type.BASE_BLOCK),
    XYLOPHONE("xylophone", SoundEvents.NOTE_BLOCK_XYLOPHONE, Type.BASE_BLOCK),
    IRON_XYLOPHONE("iron_xylophone", SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE, Type.BASE_BLOCK),
    COW_BELL("cow_bell", SoundEvents.NOTE_BLOCK_COW_BELL, Type.BASE_BLOCK),
    DIDGERIDOO("didgeridoo", SoundEvents.NOTE_BLOCK_DIDGERIDOO, Type.BASE_BLOCK),
    BIT("bit", SoundEvents.NOTE_BLOCK_BIT, Type.BASE_BLOCK),
    BANJO("banjo", SoundEvents.NOTE_BLOCK_BANJO, Type.BASE_BLOCK),
    PLING("pling", SoundEvents.NOTE_BLOCK_PLING, Type.BASE_BLOCK),
    ZOMBIE("zombie", SoundEvents.NOTE_BLOCK_IMITATE_ZOMBIE, Type.MOB_HEAD),
    SKELETON("skeleton", SoundEvents.NOTE_BLOCK_IMITATE_SKELETON, Type.MOB_HEAD),
    CREEPER("creeper", SoundEvents.NOTE_BLOCK_IMITATE_CREEPER, Type.MOB_HEAD),
    DRAGON("dragon", SoundEvents.NOTE_BLOCK_IMITATE_ENDER_DRAGON, Type.MOB_HEAD),
    WITHER_SKELETON("wither_skeleton", SoundEvents.NOTE_BLOCK_IMITATE_WITHER_SKELETON, Type.MOB_HEAD),
    PIGLIN("piglin", SoundEvents.NOTE_BLOCK_IMITATE_PIGLIN, Type.MOB_HEAD),
    CUSTOM_HEAD("custom_head", SoundEvents.UI_BUTTON_CLICK, Type.CUSTOM);

    private final String name;
    private final Holder<SoundEvent> soundEvent;
    private final Type type;

    private NoteBlockInstrument(String string2, Holder<SoundEvent> holder, Type type) {
        this.name = string2;
        this.soundEvent = holder;
        this.type = type;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Holder<SoundEvent> getSoundEvent() {
        return this.soundEvent;
    }

    public boolean isTunable() {
        return this.type == Type.BASE_BLOCK;
    }

    public boolean hasCustomSound() {
        return this.type == Type.CUSTOM;
    }

    public boolean requiresAirAbove() {
        return this.type == Type.BASE_BLOCK;
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
        if (blockState.is(Blocks.PLAYER_HEAD)) {
            return Optional.of(CUSTOM_HEAD);
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

    static enum Type {
        BASE_BLOCK,
        MOB_HEAD,
        CUSTOM;

    }
}

