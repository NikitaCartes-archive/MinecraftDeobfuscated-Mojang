package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;

public record SculkPatchConfiguration(
	int chargeCount, int amountPerCharge, int spreadAttempts, int growthRounds, int spreadRounds, IntProvider extraRareGrowths, float catalystChance
) implements FeatureConfiguration {
	public static final Codec<SculkPatchConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.intRange(1, 32).fieldOf("charge_count").forGetter(SculkPatchConfiguration::chargeCount),
					Codec.intRange(1, 500).fieldOf("amount_per_charge").forGetter(SculkPatchConfiguration::amountPerCharge),
					Codec.intRange(1, 64).fieldOf("spread_attempts").forGetter(SculkPatchConfiguration::spreadAttempts),
					Codec.intRange(0, 8).fieldOf("growth_rounds").forGetter(SculkPatchConfiguration::growthRounds),
					Codec.intRange(0, 8).fieldOf("spread_rounds").forGetter(SculkPatchConfiguration::spreadRounds),
					IntProvider.CODEC.fieldOf("extra_rare_growths").forGetter(SculkPatchConfiguration::extraRareGrowths),
					Codec.floatRange(0.0F, 1.0F).fieldOf("catalyst_chance").forGetter(SculkPatchConfiguration::catalystChance)
				)
				.apply(instance, SculkPatchConfiguration::new)
	);
}
