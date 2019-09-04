package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.DecoratorNoiseDependant;

public class NoiseHeightmapDoubleDecorator extends FeatureDecorator<DecoratorNoiseDependant> {
	public NoiseHeightmapDoubleDecorator(Function<Dynamic<?>, ? extends DecoratorNoiseDependant> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		DecoratorNoiseDependant decoratorNoiseDependant,
		BlockPos blockPos
	) {
		double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 200.0, (double)blockPos.getZ() / 200.0, false);
		int i = d < decoratorNoiseDependant.noiseLevel ? decoratorNoiseDependant.belowNoise : decoratorNoiseDependant.aboveNoise;
		return IntStream.range(0, i).mapToObj(ix -> {
			int j = random.nextInt(16);
			int k = random.nextInt(16);
			int l = levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(j, 0, k)).getY() * 2;
			if (l <= 0) {
				return null;
			} else {
				int m = random.nextInt(l);
				return blockPos.offset(j, m, k);
			}
		}).filter(Objects::nonNull);
	}
}
