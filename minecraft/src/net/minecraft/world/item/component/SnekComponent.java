package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SnekComponent(boolean revealed) {
	public static SnekComponent HIDDEN_SNEK = new SnekComponent(false);
	public static final Codec<SnekComponent> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.BOOL.fieldOf("revealed").forGetter(SnekComponent::revealed)).apply(instance, SnekComponent::new)
	);
	public static final StreamCodec<? super RegistryFriendlyByteBuf, SnekComponent> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL, SnekComponent::revealed, SnekComponent::new
	);
}
