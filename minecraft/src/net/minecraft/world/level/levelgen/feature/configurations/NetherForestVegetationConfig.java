package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class NetherForestVegetationConfig extends BlockPileConfiguration {
	public static final Codec<NetherForestVegetationConfig> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(netherForestVegetationConfig -> netherForestVegetationConfig.stateProvider),
					ExtraCodecs.POSITIVE_INT.fieldOf("spread_width").forGetter(netherForestVegetationConfig -> netherForestVegetationConfig.spreadWidth),
					ExtraCodecs.POSITIVE_INT.fieldOf("spread_height").forGetter(netherForestVegetationConfig -> netherForestVegetationConfig.spreadHeight)
				)
				.apply(instance, NetherForestVegetationConfig::new)
	);
	public final int spreadWidth;
	public final int spreadHeight;

	public NetherForestVegetationConfig(BlockStateProvider blockStateProvider, int i, int j) {
		super(blockStateProvider);
		this.spreadWidth = i;
		this.spreadHeight = j;
	}
}
