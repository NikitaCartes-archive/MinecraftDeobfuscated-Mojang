package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class HeightmapDoubleDecorator<DC extends DecoratorConfiguration> extends EdgeDecorator<DC> {
	public HeightmapDoubleDecorator(Codec<DC> codec) {
		super(codec);
	}

	@Override
	protected Heightmap.Types type(DC decoratorConfiguration) {
		return Heightmap.Types.MOTION_BLOCKING;
	}

	@Override
	public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, DC decoratorConfiguration, BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getZ();
		int k = decorationContext.getHeight(this.type(decoratorConfiguration), i, j);
		return k == 0 ? Stream.of() : Stream.of(new BlockPos(i, random.nextInt(k * 2), j));
	}
}
