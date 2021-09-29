package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
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
		return decorationContext.getCarvingMask(chunkPos, carvingMaskDecoratorConfiguration.step).stream(chunkPos);
	}
}
