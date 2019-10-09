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
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;

public class NoiseHeightmapDoubleDecorator extends FeatureDecorator<NoiseDependantDecoratorConfiguration> {
	public NoiseHeightmapDoubleDecorator(Function<Dynamic<?>, ? extends NoiseDependantDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		NoiseDependantDecoratorConfiguration noiseDependantDecoratorConfiguration,
		BlockPos blockPos
	) {
		double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 200.0, (double)blockPos.getZ() / 200.0, false);
		int i = d < noiseDependantDecoratorConfiguration.noiseLevel
			? noiseDependantDecoratorConfiguration.belowNoise
			: noiseDependantDecoratorConfiguration.aboveNoise;
		return IntStream.range(0, i).mapToObj(ix -> {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			int l = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, j, k) * 2;
			return l <= 0 ? null : new BlockPos(j, random.nextInt(l), k);
		}).filter(Objects::nonNull);
	}
}
