package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;

public record DamageType(String msgId, DamageScaling scaling, float exhaustion, DamageEffects effects, DeathMessageType deathMessageType) {
	public static final Codec<DamageType> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.STRING.fieldOf("message_id").forGetter(DamageType::msgId),
					DamageScaling.CODEC.fieldOf("scaling").forGetter(DamageType::scaling),
					Codec.FLOAT.fieldOf("exhaustion").forGetter(DamageType::exhaustion),
					DamageEffects.CODEC.optionalFieldOf("effects", DamageEffects.HURT).forGetter(DamageType::effects),
					DeathMessageType.CODEC.optionalFieldOf("death_message_type", DeathMessageType.DEFAULT).forGetter(DamageType::deathMessageType)
				)
				.apply(instance, DamageType::new)
	);
	public static final Codec<Holder<DamageType>> CODEC = RegistryFixedCodec.create(Registries.DAMAGE_TYPE);

	public DamageType(String string, DamageScaling damageScaling, float f) {
		this(string, damageScaling, f, DamageEffects.HURT, DeathMessageType.DEFAULT);
	}

	public DamageType(String string, DamageScaling damageScaling, float f, DamageEffects damageEffects) {
		this(string, damageScaling, f, damageEffects, DeathMessageType.DEFAULT);
	}

	public DamageType(String string, float f, DamageEffects damageEffects) {
		this(string, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, f, damageEffects);
	}

	public DamageType(String string, float f) {
		this(string, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, f);
	}
}
