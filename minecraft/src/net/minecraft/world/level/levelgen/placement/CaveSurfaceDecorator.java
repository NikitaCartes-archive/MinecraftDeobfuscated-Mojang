package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Column;

public class CaveSurfaceDecorator extends FeatureDecorator<CaveDecoratorConfiguration> {
	public CaveSurfaceDecorator(Codec<CaveDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		DecorationContext decorationContext, Random random, CaveDecoratorConfiguration caveDecoratorConfiguration, BlockPos blockPos
	) {
		Optional<Column> optional = Column.scan(
			decorationContext.getLevel(),
			blockPos,
			caveDecoratorConfiguration.floorToCeilingSearchRange,
			BlockBehaviour.BlockStateBase::isAir,
			blockState -> blockState.getMaterial().isSolid()
		);
		if (!optional.isPresent()) {
			return Stream.of();
		} else {
			OptionalInt optionalInt = caveDecoratorConfiguration.surface == CaveSurface.CEILING
				? ((Column)optional.get()).getCeiling()
				: ((Column)optional.get()).getFloor();
			return !optionalInt.isPresent() ? Stream.of() : Stream.of(blockPos.atY(optionalInt.getAsInt() - caveDecoratorConfiguration.surface.getY()));
		}
	}
}
