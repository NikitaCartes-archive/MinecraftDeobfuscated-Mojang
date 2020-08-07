package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CarvingMaskDecorator extends FeatureDecorator<CarvingMaskDecoratorConfiguration> {
	public CarvingMaskDecorator(Codec<CarvingMaskDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		DecorationContext decorationContext, Random random, CarvingMaskDecoratorConfiguration carvingMaskDecoratorConfiguration, BlockPos blockPos
	) {
		ChunkPos chunkPos = new ChunkPos(blockPos);
		BitSet bitSet = decorationContext.getCarvingMask(chunkPos, carvingMaskDecoratorConfiguration.step);
		return IntStream.range(0, bitSet.length()).filter(i -> bitSet.get(i) && random.nextFloat() < carvingMaskDecoratorConfiguration.probability).mapToObj(i -> {
			int j = i & 15;
			int k = i >> 4 & 15;
			int l = i >> 8;
			return new BlockPos(chunkPos.getMinBlockX() + j, l, chunkPos.getMinBlockZ() + k);
		});
	}
}
