package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.configurations.ScatterDecoratorConfiguration;

public class ScatterDecorator extends FeatureDecorator<ScatterDecoratorConfiguration> {
	public ScatterDecorator(Codec<ScatterDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		DecorationContext decorationContext, Random random, ScatterDecoratorConfiguration scatterDecoratorConfiguration, BlockPos blockPos
	) {
		int i = blockPos.getX() + scatterDecoratorConfiguration.xzSpread.sample(random);
		int j = blockPos.getY() + scatterDecoratorConfiguration.ySpread.sample(random);
		int k = blockPos.getZ() + scatterDecoratorConfiguration.xzSpread.sample(random);
		BlockPos blockPos2 = new BlockPos(i, j, k);
		ChunkPos chunkPos = new ChunkPos(blockPos2);
		ChunkPos chunkPos2 = new ChunkPos(blockPos);
		int l = Mth.abs(chunkPos.x - chunkPos2.x);
		int m = Mth.abs(chunkPos.z - chunkPos2.z);
		return l <= 1 && m <= 1 ? Stream.of(new BlockPos(i, j, k)) : Stream.empty();
	}
}
