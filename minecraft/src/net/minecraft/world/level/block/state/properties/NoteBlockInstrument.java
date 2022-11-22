package net.minecraft.world.level.block.state.properties;

import java.util.Optional;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public enum NoteBlockInstrument implements StringRepresentable {
	HARP("harp", SoundEvents.NOTE_BLOCK_HARP, NoteBlockInstrument.Type.BASE_BLOCK),
	BASEDRUM("basedrum", SoundEvents.NOTE_BLOCK_BASEDRUM, NoteBlockInstrument.Type.BASE_BLOCK),
	SNARE("snare", SoundEvents.NOTE_BLOCK_SNARE, NoteBlockInstrument.Type.BASE_BLOCK),
	HAT("hat", SoundEvents.NOTE_BLOCK_HAT, NoteBlockInstrument.Type.BASE_BLOCK),
	BASS("bass", SoundEvents.NOTE_BLOCK_BASS, NoteBlockInstrument.Type.BASE_BLOCK),
	FLUTE("flute", SoundEvents.NOTE_BLOCK_FLUTE, NoteBlockInstrument.Type.BASE_BLOCK),
	BELL("bell", SoundEvents.NOTE_BLOCK_BELL, NoteBlockInstrument.Type.BASE_BLOCK),
	GUITAR("guitar", SoundEvents.NOTE_BLOCK_GUITAR, NoteBlockInstrument.Type.BASE_BLOCK),
	CHIME("chime", SoundEvents.NOTE_BLOCK_CHIME, NoteBlockInstrument.Type.BASE_BLOCK),
	XYLOPHONE("xylophone", SoundEvents.NOTE_BLOCK_XYLOPHONE, NoteBlockInstrument.Type.BASE_BLOCK),
	IRON_XYLOPHONE("iron_xylophone", SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE, NoteBlockInstrument.Type.BASE_BLOCK),
	COW_BELL("cow_bell", SoundEvents.NOTE_BLOCK_COW_BELL, NoteBlockInstrument.Type.BASE_BLOCK),
	DIDGERIDOO("didgeridoo", SoundEvents.NOTE_BLOCK_DIDGERIDOO, NoteBlockInstrument.Type.BASE_BLOCK),
	BIT("bit", SoundEvents.NOTE_BLOCK_BIT, NoteBlockInstrument.Type.BASE_BLOCK),
	BANJO("banjo", SoundEvents.NOTE_BLOCK_BANJO, NoteBlockInstrument.Type.BASE_BLOCK),
	PLING("pling", SoundEvents.NOTE_BLOCK_PLING, NoteBlockInstrument.Type.BASE_BLOCK),
	ZOMBIE("zombie", SoundEvents.ZOMBIE_AMBIENT, NoteBlockInstrument.Type.MOB_HEAD),
	SKELETON("skeleton", SoundEvents.SKELETON_AMBIENT, NoteBlockInstrument.Type.MOB_HEAD),
	CREEPER("creeper", SoundEvents.CREEPER_PRIMED, NoteBlockInstrument.Type.MOB_HEAD),
	DRAGON("dragon", SoundEvents.ENDER_DRAGON_AMBIENT, NoteBlockInstrument.Type.MOB_HEAD),
	WITHER_SKELETON("wither_skeleton", SoundEvents.WITHER_SKELETON_AMBIENT, NoteBlockInstrument.Type.MOB_HEAD),
	PIGLIN("piglin", SoundEvents.PIGLIN_AMBIENT, NoteBlockInstrument.Type.MOB_HEAD),
	CUSTOM_HEAD("custom_head", SoundEvents.UI_BUTTON_CLICK, NoteBlockInstrument.Type.CUSTOM);

	private final String name;
	private final SoundEvent soundEvent;
	private final NoteBlockInstrument.Type type;

	private NoteBlockInstrument(String string2, SoundEvent soundEvent, NoteBlockInstrument.Type type) {
		this.name = string2;
		this.soundEvent = soundEvent;
		this.type = type;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public SoundEvent getSoundEvent() {
		return this.soundEvent;
	}

	public boolean isTunable() {
		return this.type == NoteBlockInstrument.Type.BASE_BLOCK;
	}

	public boolean hasCustomSound() {
		return this.type == NoteBlockInstrument.Type.CUSTOM;
	}

	public boolean requiresAirAbove() {
		return this.type == NoteBlockInstrument.Type.BASE_BLOCK;
	}

	public static Optional<NoteBlockInstrument> byStateAbove(BlockState blockState) {
		if (blockState.is(Blocks.ZOMBIE_HEAD)) {
			return Optional.of(ZOMBIE);
		} else if (blockState.is(Blocks.SKELETON_SKULL)) {
			return Optional.of(SKELETON);
		} else if (blockState.is(Blocks.CREEPER_HEAD)) {
			return Optional.of(CREEPER);
		} else if (blockState.is(Blocks.DRAGON_HEAD)) {
			return Optional.of(DRAGON);
		} else if (blockState.is(Blocks.WITHER_SKELETON_SKULL)) {
			return Optional.of(WITHER_SKELETON);
		} else if (blockState.is(Blocks.PIGLIN_HEAD)) {
			return Optional.of(PIGLIN);
		} else {
			return blockState.is(Blocks.PLAYER_HEAD) ? Optional.of(CUSTOM_HEAD) : Optional.empty();
		}
	}

	public static NoteBlockInstrument byStateBelow(BlockState blockState) {
		if (blockState.is(Blocks.CLAY)) {
			return FLUTE;
		} else if (blockState.is(Blocks.GOLD_BLOCK)) {
			return BELL;
		} else if (blockState.is(BlockTags.WOOL)) {
			return GUITAR;
		} else if (blockState.is(Blocks.PACKED_ICE)) {
			return CHIME;
		} else if (blockState.is(Blocks.BONE_BLOCK)) {
			return XYLOPHONE;
		} else if (blockState.is(Blocks.IRON_BLOCK)) {
			return IRON_XYLOPHONE;
		} else if (blockState.is(Blocks.SOUL_SAND)) {
			return COW_BELL;
		} else if (blockState.is(Blocks.PUMPKIN)) {
			return DIDGERIDOO;
		} else if (blockState.is(Blocks.EMERALD_BLOCK)) {
			return BIT;
		} else if (blockState.is(Blocks.HAY_BLOCK)) {
			return BANJO;
		} else if (blockState.is(Blocks.GLOWSTONE)) {
			return PLING;
		} else {
			Material material = blockState.getMaterial();
			if (material == Material.STONE) {
				return BASEDRUM;
			} else if (material == Material.SAND) {
				return SNARE;
			} else if (material == Material.GLASS) {
				return HAT;
			} else {
				return material != Material.WOOD && material != Material.NETHER_WOOD ? HARP : BASS;
			}
		}
	}

	static enum Type {
		BASE_BLOCK,
		MOB_HEAD,
		CUSTOM;
	}
}
