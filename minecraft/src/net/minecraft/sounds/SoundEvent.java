package net.minecraft.sounds;

import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
	private static final float DEFAULT_RANGE = 16.0F;
	private final ResourceLocation location;
	private final float range;
	private final boolean newSystem;

	static SoundEvent createVariableRangeEvent(ResourceLocation resourceLocation) {
		return new SoundEvent(resourceLocation, 16.0F, false);
	}

	static SoundEvent createFixedRangeEvent(ResourceLocation resourceLocation, float f) {
		return new SoundEvent(resourceLocation, f, true);
	}

	private SoundEvent(ResourceLocation resourceLocation, float f, boolean bl) {
		this.location = resourceLocation;
		this.range = f;
		this.newSystem = bl;
	}

	public ResourceLocation getLocation() {
		return this.location;
	}

	public float getRange(float f) {
		return this.newSystem ? this.range : legacySoundRange(f);
	}

	public static float legacySoundRange(float f) {
		return f > 1.0F ? 16.0F * f : 16.0F;
	}
}
