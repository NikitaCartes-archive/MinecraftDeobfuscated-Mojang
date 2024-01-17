package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
	public static final Codec<SoundEvent> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("sound_id").forGetter(SoundEvent::getLocation), Codec.FLOAT.optionalFieldOf("range").forGetter(SoundEvent::fixedRange)
				)
				.apply(instance, SoundEvent::create)
	);
	public static final Codec<Holder<SoundEvent>> CODEC = RegistryFileCodec.create(Registries.SOUND_EVENT, DIRECT_CODEC);
	public static final StreamCodec<ByteBuf, SoundEvent> DIRECT_STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, SoundEvent::getLocation, ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional), SoundEvent::fixedRange, SoundEvent::create
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SoundEvent>> STREAM_CODEC = ByteBufCodecs.holder(Registries.SOUND_EVENT, DIRECT_STREAM_CODEC);
	private static final float DEFAULT_RANGE = 16.0F;
	private final ResourceLocation location;
	private final float range;
	private final boolean newSystem;

	private static SoundEvent create(ResourceLocation resourceLocation, Optional<Float> optional) {
		return (SoundEvent)optional.map(float_ -> createFixedRangeEvent(resourceLocation, float_)).orElseGet(() -> createVariableRangeEvent(resourceLocation));
	}

	public static SoundEvent createVariableRangeEvent(ResourceLocation resourceLocation) {
		return new SoundEvent(resourceLocation, 16.0F, false);
	}

	public static SoundEvent createFixedRangeEvent(ResourceLocation resourceLocation, float f) {
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
		if (this.newSystem) {
			return this.range;
		} else {
			return f > 1.0F ? 16.0F * f : 16.0F;
		}
	}

	private Optional<Float> fixedRange() {
		return this.newSystem ? Optional.of(this.range) : Optional.empty();
	}
}
