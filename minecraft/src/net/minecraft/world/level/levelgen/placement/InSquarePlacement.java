package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class InSquarePlacement extends PlacementModifier {
	private static final InSquarePlacement INSTANCE = new InSquarePlacement();
	public static final Codec<InSquarePlacement> CODEC = Codec.unit((Supplier<InSquarePlacement>)(() -> INSTANCE));

	public static InSquarePlacement spread() {
		return INSTANCE;
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext placementContext, Random random, BlockPos blockPos) {
		int i = random.nextInt(16) + blockPos.getX();
		int j = random.nextInt(16) + blockPos.getZ();
		return Stream.of(new BlockPos(i, blockPos.getY(), j));
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.IN_SQUARE;
	}
}
