package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.UniformInt;
import net.minecraft.world.entity.ai.behavior.WeightedList;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class GrowingPlantConfiguration implements FeatureConfiguration {
	public static final Codec<GrowingPlantConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					WeightedList.codec(UniformInt.CODEC).fieldOf("height_distribution").forGetter(growingPlantConfiguration -> growingPlantConfiguration.heightDistribution),
					Direction.CODEC.fieldOf("direction").forGetter(growingPlantConfiguration -> growingPlantConfiguration.direction),
					BlockStateProvider.CODEC.fieldOf("body_provider").forGetter(growingPlantConfiguration -> growingPlantConfiguration.bodyProvider),
					BlockStateProvider.CODEC.fieldOf("head_provider").forGetter(growingPlantConfiguration -> growingPlantConfiguration.headProvider),
					Codec.BOOL.fieldOf("allow_water").forGetter(growingPlantConfiguration -> growingPlantConfiguration.allowWater)
				)
				.apply(instance, GrowingPlantConfiguration::new)
	);
	public final WeightedList<UniformInt> heightDistribution;
	public final Direction direction;
	public final BlockStateProvider bodyProvider;
	public final BlockStateProvider headProvider;
	public final boolean allowWater;

	public GrowingPlantConfiguration(
		WeightedList<UniformInt> weightedList, Direction direction, BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, boolean bl
	) {
		this.heightDistribution = weightedList;
		this.direction = direction;
		this.bodyProvider = blockStateProvider;
		this.headProvider = blockStateProvider2;
		this.allowWater = bl;
	}
}
