package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public record SuspiciousStewEffects(List<SuspiciousStewEffects.Entry> effects) {
	public static final SuspiciousStewEffects EMPTY = new SuspiciousStewEffects(List.of());
	public static final Codec<SuspiciousStewEffects> CODEC = SuspiciousStewEffects.Entry.CODEC
		.listOf()
		.xmap(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);
	public static final StreamCodec<RegistryFriendlyByteBuf, SuspiciousStewEffects> STREAM_CODEC = SuspiciousStewEffects.Entry.STREAM_CODEC
		.apply(ByteBufCodecs.list())
		.map(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);

	public SuspiciousStewEffects withEffectAdded(SuspiciousStewEffects.Entry entry) {
		return new SuspiciousStewEffects(Util.copyAndAdd(this.effects, entry));
	}

	public static record Entry(Holder<MobEffect> effect, int duration) {
		public static final Codec<SuspiciousStewEffects.Entry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("id").forGetter(SuspiciousStewEffects.Entry::effect),
						Codec.INT.optionalFieldOf("duration", Integer.valueOf(160)).forGetter(SuspiciousStewEffects.Entry::duration)
					)
					.apply(instance, SuspiciousStewEffects.Entry::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, SuspiciousStewEffects.Entry> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT),
			SuspiciousStewEffects.Entry::effect,
			ByteBufCodecs.VAR_INT,
			SuspiciousStewEffects.Entry::duration,
			SuspiciousStewEffects.Entry::new
		);

		public MobEffectInstance createEffectInstance() {
			return new MobEffectInstance(this.effect, this.duration);
		}
	}
}
