package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;

public class HeightmapDecorator extends FeatureDecorator<HeightmapConfiguration> {
	public HeightmapDecorator(Codec<HeightmapConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, HeightmapConfiguration heightmapConfiguration, BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getZ();
		int k = decorationContext.getHeight(heightmapConfiguration.heightmap, i, j);
		return k > decorationContext.getMinBuildHeight() ? Stream.of(new BlockPos(i, k, j)) : Stream.of();
	}
}
