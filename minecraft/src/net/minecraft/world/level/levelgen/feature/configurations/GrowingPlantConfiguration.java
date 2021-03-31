package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class GrowingPlantConfiguration implements FeatureConfiguration {
	public static final Codec<GrowingPlantConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					SimpleWeightedRandomList.wrappedCodec(IntProvider.CODEC)
						.fieldOf("height_distribution")
						.forGetter(growingPlantConfiguration -> growingPlantConfiguration.heightDistribution),
					Direction.CODEC.fieldOf("direction").forGetter(growingPlantConfiguration -> growingPlantConfiguration.direction),
					BlockStateProvider.CODEC.fieldOf("body_provider").forGetter(growingPlantConfiguration -> growingPlantConfiguration.bodyProvider),
					BlockStateProvider.CODEC.fieldOf("head_provider").forGetter(growingPlantConfiguration -> growingPlantConfiguration.headProvider),
					Codec.BOOL.fieldOf("allow_water").forGetter(growingPlantConfiguration -> growingPlantConfiguration.allowWater)
				)
				.apply(instance, GrowingPlantConfiguration::new)
	);
	public final SimpleWeightedRandomList<IntProvider> heightDistribution;
	public final Direction direction;
	public final BlockStateProvider bodyProvider;
	public final BlockStateProvider headProvider;
	public final boolean allowWater;

	public GrowingPlantConfiguration(
		SimpleWeightedRandomList<IntProvider> simpleWeightedRandomList,
		Direction direction,
		BlockStateProvider blockStateProvider,
		BlockStateProvider blockStateProvider2,
		boolean bl
	) {
		this.heightDistribution = simpleWeightedRandomList;
		this.direction = direction;
		this.bodyProvider = blockStateProvider;
		this.headProvider = blockStateProvider2;
		this.allowWater = bl;
	}
}
