package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;

public class HeightmapDoubleDecorator extends FeatureDecorator<HeightmapConfiguration> {
	public HeightmapDoubleDecorator(Codec<HeightmapConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, HeightmapConfiguration heightmapConfiguration, BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getZ();
		int k = decorationContext.getHeight(heightmapConfiguration.heightmap, i, j);
		return k == decorationContext.getMinBuildHeight()
			? Stream.of()
			: Stream.of(new BlockPos(i, decorationContext.getMinBuildHeight() + random.nextInt((k - decorationContext.getMinBuildHeight()) * 2), j));
	}
}
