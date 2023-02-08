/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.damagesource;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DeathMessageType;

public record DamageType(String msgId, DamageScaling scaling, float exhaustion, DamageEffects effects, DeathMessageType deathMessageType) {
    public static final Codec<DamageType> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("message_id")).forGetter(DamageType::msgId), ((MapCodec)DamageScaling.CODEC.fieldOf("scaling")).forGetter(DamageType::scaling), ((MapCodec)Codec.FLOAT.fieldOf("exhaustion")).forGetter(DamageType::exhaustion), DamageEffects.CODEC.optionalFieldOf("effects", DamageEffects.HURT).forGetter(DamageType::effects), DeathMessageType.CODEC.optionalFieldOf("death_message_type", DeathMessageType.DEFAULT).forGetter(DamageType::deathMessageType)).apply((Applicative<DamageType, ?>)instance, DamageType::new));

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

