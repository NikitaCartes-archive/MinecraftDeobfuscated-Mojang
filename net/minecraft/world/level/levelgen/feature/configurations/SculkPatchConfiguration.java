/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record SculkPatchConfiguration(int chargeCount, int amountPerCharge, int spreadAttempts, int growthRounds, int spreadRounds, IntProvider extraRareGrowths, float catalystChance) implements FeatureConfiguration
{
    public static final Codec<SculkPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(1, 32).fieldOf("charge_count")).forGetter(SculkPatchConfiguration::chargeCount), ((MapCodec)Codec.intRange(1, 500).fieldOf("amount_per_charge")).forGetter(SculkPatchConfiguration::amountPerCharge), ((MapCodec)Codec.intRange(1, 64).fieldOf("spread_attempts")).forGetter(SculkPatchConfiguration::spreadAttempts), ((MapCodec)Codec.intRange(0, 8).fieldOf("growth_rounds")).forGetter(SculkPatchConfiguration::growthRounds), ((MapCodec)Codec.intRange(0, 8).fieldOf("spread_rounds")).forGetter(SculkPatchConfiguration::spreadRounds), ((MapCodec)IntProvider.CODEC.fieldOf("extra_rare_growths")).forGetter(SculkPatchConfiguration::extraRareGrowths), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("catalyst_chance")).forGetter(SculkPatchConfiguration::catalystChance)).apply((Applicative<SculkPatchConfiguration, ?>)instance, SculkPatchConfiguration::new));
}

