package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class CarvingMaskDecorator extends FeatureDecorator<DecoratorCarvingMaskConfig> {
	public CarvingMaskDecorator(Function<Dynamic<?>, ? extends DecoratorCarvingMaskConfig> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		DecoratorCarvingMaskConfig decoratorCarvingMaskConfig,
		BlockPos blockPos
	) {
		ChunkAccess chunkAccess = levelAccessor.getChunk(blockPos);
		ChunkPos chunkPos = chunkAccess.getPos();
		BitSet bitSet = chunkAccess.getCarvingMask(decoratorCarvingMaskConfig.step);
		return IntStream.range(0, bitSet.length()).filter(i -> bitSet.get(i) && random.nextFloat() < decoratorCarvingMaskConfig.probability).mapToObj(i -> {
			int j = i & 15;
			int k = i >> 4 & 15;
			int l = i >> 8;
			return new BlockPos(chunkPos.getMinBlockX() + j, l, chunkPos.getMinBlockZ() + k);
		});
	}
}
