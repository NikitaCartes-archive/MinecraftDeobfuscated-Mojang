package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;

public record DiskConfiguration(RuleBasedBlockStateProvider stateProvider, BlockPredicate target, IntProvider radius, int halfHeight)
	implements FeatureConfiguration {
	public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					RuleBasedBlockStateProvider.CODEC.fieldOf("state_provider").forGetter(DiskConfiguration::stateProvider),
					BlockPredicate.CODEC.fieldOf("target").forGetter(DiskConfiguration::target),
					IntProvider.codec(0, 8).fieldOf("radius").forGetter(DiskConfiguration::radius),
					Codec.intRange(0, 4).fieldOf("half_height").forGetter(DiskConfiguration::halfHeight)
				)
				.apply(instance, DiskConfiguration::new)
	);
}
