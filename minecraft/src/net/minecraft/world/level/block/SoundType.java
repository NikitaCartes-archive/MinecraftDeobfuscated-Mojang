package net.minecraft.world.level.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class SoundType {
	public static final SoundType WOOD = new SoundType(
		1.0F, 1.0F, SoundEvents.WOOD_BREAK, SoundEvents.WOOD_STEP, SoundEvents.WOOD_PLACE, SoundEvents.WOOD_HIT, SoundEvents.WOOD_FALL
	);
	public static final SoundType GRAVEL = new SoundType(
		1.0F, 1.0F, SoundEvents.GRAVEL_BREAK, SoundEvents.GRAVEL_STEP, SoundEvents.GRAVEL_PLACE, SoundEvents.GRAVEL_HIT, SoundEvents.GRAVEL_FALL
	);
	public static final SoundType GRASS = new SoundType(
		1.0F, 1.0F, SoundEvents.GRASS_BREAK, SoundEvents.GRASS_STEP, SoundEvents.GRASS_PLACE, SoundEvents.GRASS_HIT, SoundEvents.GRASS_FALL
	);
	public static final SoundType STONE = new SoundType(
		1.0F, 1.0F, SoundEvents.STONE_BREAK, SoundEvents.STONE_STEP, SoundEvents.STONE_PLACE, SoundEvents.STONE_HIT, SoundEvents.STONE_FALL
	);
	public static final SoundType METAL = new SoundType(
		1.0F, 1.5F, SoundEvents.METAL_BREAK, SoundEvents.METAL_STEP, SoundEvents.METAL_PLACE, SoundEvents.METAL_HIT, SoundEvents.METAL_FALL
	);
	public static final SoundType GLASS = new SoundType(
		1.0F, 1.0F, SoundEvents.GLASS_BREAK, SoundEvents.GLASS_STEP, SoundEvents.GLASS_PLACE, SoundEvents.GLASS_HIT, SoundEvents.GLASS_FALL
	);
	public static final SoundType WOOL = new SoundType(
		1.0F, 1.0F, SoundEvents.WOOL_BREAK, SoundEvents.WOOL_STEP, SoundEvents.WOOL_PLACE, SoundEvents.WOOL_HIT, SoundEvents.WOOL_FALL
	);
	public static final SoundType SAND = new SoundType(
		1.0F, 1.0F, SoundEvents.SAND_BREAK, SoundEvents.SAND_STEP, SoundEvents.SAND_PLACE, SoundEvents.SAND_HIT, SoundEvents.SAND_FALL
	);
	public static final SoundType SNOW = new SoundType(
		1.0F, 1.0F, SoundEvents.SNOW_BREAK, SoundEvents.SNOW_STEP, SoundEvents.SNOW_PLACE, SoundEvents.SNOW_HIT, SoundEvents.SNOW_FALL
	);
	public static final SoundType LADDER = new SoundType(
		1.0F, 1.0F, SoundEvents.LADDER_BREAK, SoundEvents.LADDER_STEP, SoundEvents.LADDER_PLACE, SoundEvents.LADDER_HIT, SoundEvents.LADDER_FALL
	);
	public static final SoundType ANVIL = new SoundType(
		0.3F, 1.0F, SoundEvents.ANVIL_BREAK, SoundEvents.ANVIL_STEP, SoundEvents.ANVIL_PLACE, SoundEvents.ANVIL_HIT, SoundEvents.ANVIL_FALL
	);
	public static final SoundType SLIME_BLOCK = new SoundType(
		1.0F,
		1.0F,
		SoundEvents.SLIME_BLOCK_BREAK,
		SoundEvents.SLIME_BLOCK_STEP,
		SoundEvents.SLIME_BLOCK_PLACE,
		SoundEvents.SLIME_BLOCK_HIT,
		SoundEvents.SLIME_BLOCK_FALL
	);
	public static final SoundType WET_GRASS = new SoundType(
		1.0F, 1.0F, SoundEvents.WET_GRASS_BREAK, SoundEvents.WET_GRASS_STEP, SoundEvents.WET_GRASS_PLACE, SoundEvents.WET_GRASS_HIT, SoundEvents.WET_GRASS_FALL
	);
	public static final SoundType CORAL_BLOCK = new SoundType(
		1.0F,
		1.0F,
		SoundEvents.CORAL_BLOCK_BREAK,
		SoundEvents.CORAL_BLOCK_STEP,
		SoundEvents.CORAL_BLOCK_PLACE,
		SoundEvents.CORAL_BLOCK_HIT,
		SoundEvents.CORAL_BLOCK_FALL
	);
	public static final SoundType BAMBOO = new SoundType(
		1.0F, 1.0F, SoundEvents.BAMBOO_BREAK, SoundEvents.BAMBOO_STEP, SoundEvents.BAMBOO_PLACE, SoundEvents.BAMBOO_HIT, SoundEvents.BAMBOO_FALL
	);
	public static final SoundType BAMBOO_SAPLING = new SoundType(
		1.0F,
		1.0F,
		SoundEvents.BAMBOO_SAPLING_BREAK,
		SoundEvents.BAMBOO_STEP,
		SoundEvents.BAMBOO_SAPLING_PLACE,
		SoundEvents.BAMBOO_SAPLING_HIT,
		SoundEvents.BAMBOO_FALL
	);
	public static final SoundType SCAFFOLDING = new SoundType(
		1.0F,
		1.0F,
		SoundEvents.SCAFFOLDING_BREAK,
		SoundEvents.SCAFFOLDING_STEP,
		SoundEvents.SCAFFOLDING_PLACE,
		SoundEvents.SCAFFOLDING_HIT,
		SoundEvents.SCAFFOLDING_FALL
	);
	public static final SoundType SWEET_BERRY_BUSH = new SoundType(
		1.0F, 1.0F, SoundEvents.SWEET_BERRY_BUSH_BREAK, SoundEvents.GRASS_STEP, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundEvents.GRASS_HIT, SoundEvents.GRASS_FALL
	);
	public static final SoundType CROP = new SoundType(
		1.0F, 1.0F, SoundEvents.CROP_BREAK, SoundEvents.GRASS_STEP, SoundEvents.CROP_PLANTED, SoundEvents.GRASS_HIT, SoundEvents.GRASS_FALL
	);
	public static final SoundType HARD_CROP = new SoundType(
		1.0F, 1.0F, SoundEvents.WOOD_BREAK, SoundEvents.WOOD_STEP, SoundEvents.CROP_PLANTED, SoundEvents.WOOD_HIT, SoundEvents.WOOD_FALL
	);
	public static final SoundType NETHER_WART = new SoundType(
		1.0F, 1.0F, SoundEvents.NETHER_WART_BREAK, SoundEvents.STONE_STEP, SoundEvents.NETHER_WART_PLANTED, SoundEvents.STONE_HIT, SoundEvents.STONE_FALL
	);
	public static final SoundType LANTERN = new SoundType(
		1.0F, 1.0F, SoundEvents.LANTERN_BREAK, SoundEvents.LANTERN_STEP, SoundEvents.LANTERN_PLACE, SoundEvents.LANTERN_HIT, SoundEvents.LANTERN_FALL
	);
	public final float volume;
	public final float pitch;
	private final SoundEvent breakSound;
	private final SoundEvent stepSound;
	private final SoundEvent placeSound;
	private final SoundEvent hitSound;
	private final SoundEvent fallSound;

	public SoundType(float f, float g, SoundEvent soundEvent, SoundEvent soundEvent2, SoundEvent soundEvent3, SoundEvent soundEvent4, SoundEvent soundEvent5) {
		this.volume = f;
		this.pitch = g;
		this.breakSound = soundEvent;
		this.stepSound = soundEvent2;
		this.placeSound = soundEvent3;
		this.hitSound = soundEvent4;
		this.fallSound = soundEvent5;
	}

	public float getVolume() {
		return this.volume;
	}

	public float getPitch() {
		return this.pitch;
	}

	@Environment(EnvType.CLIENT)
	public SoundEvent getBreakSound() {
		return this.breakSound;
	}

	public SoundEvent getStepSound() {
		return this.stepSound;
	}

	public SoundEvent getPlaceSound() {
		return this.placeSound;
	}

	@Environment(EnvType.CLIENT)
	public SoundEvent getHitSound() {
		return this.hitSound;
	}

	public SoundEvent getFallSound() {
		return this.fallSound;
	}
}
