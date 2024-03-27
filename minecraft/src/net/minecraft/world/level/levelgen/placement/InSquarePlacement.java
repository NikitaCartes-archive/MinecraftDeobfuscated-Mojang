package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class InSquarePlacement extends PlacementModifier {
	private static final InSquarePlacement INSTANCE = new InSquarePlacement();
	public static final MapCodec<InSquarePlacement> CODEC = MapCodec.unit((Supplier<InSquarePlacement>)(() -> INSTANCE));

	public static InSquarePlacement spread() {
		return INSTANCE;
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		int i = randomSource.nextInt(16) + blockPos.getX();
		int j = randomSource.nextInt(16) + blockPos.getZ();
		return Stream.of(new BlockPos(i, blockPos.getY(), j));
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.IN_SQUARE;
	}
}
