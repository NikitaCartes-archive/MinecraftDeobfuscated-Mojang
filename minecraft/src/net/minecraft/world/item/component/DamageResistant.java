package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public record DamageResistant(TagKey<DamageType> types) {
	public static final Codec<DamageResistant> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(TagKey.hashedCodec(Registries.DAMAGE_TYPE).fieldOf("types").forGetter(DamageResistant::types))
				.apply(instance, DamageResistant::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, DamageResistant> STREAM_CODEC = StreamCodec.composite(
		TagKey.streamCodec(Registries.DAMAGE_TYPE), DamageResistant::types, DamageResistant::new
	);

	public boolean isResistantTo(DamageSource damageSource) {
		return damageSource.is(this.types);
	}
}
