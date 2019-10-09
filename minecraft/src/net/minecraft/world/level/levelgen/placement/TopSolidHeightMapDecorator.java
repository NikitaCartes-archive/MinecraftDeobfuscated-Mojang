package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class TopSolidHeightMapDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
	public TopSolidHeightMapDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		NoneDecoratorConfiguration noneDecoratorConfiguration,
		BlockPos blockPos
	) {
		int i = random.nextInt(16) + blockPos.getX();
		int j = random.nextInt(16) + blockPos.getZ();
		int k = levelAccessor.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, i, j);
		return Stream.of(new BlockPos(i, k, j));
	}
}
