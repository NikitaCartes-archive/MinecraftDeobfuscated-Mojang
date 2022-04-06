package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
	public static final Codec<SoundEvent> CODEC = ResourceLocation.CODEC.xmap(SoundEvent::new, soundEvent -> soundEvent.location);
	private final ResourceLocation location;
	private final float range;
	private final boolean newSystem;

	public SoundEvent(ResourceLocation resourceLocation) {
		this(resourceLocation, 16.0F, false);
	}

	public SoundEvent(ResourceLocation resourceLocation, float f) {
		this(resourceLocation, f, true);
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
		if (this.newSystem) {
			return this.range;
		} else {
			return f > 1.0F ? 16.0F * f : 16.0F;
		}
	}
}
