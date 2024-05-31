package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;

public class FixedPlacement extends PlacementModifier {
	public static final MapCodec<FixedPlacement> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(BlockPos.CODEC.listOf().fieldOf("positions").forGetter(fixedPlacement -> fixedPlacement.positions))
				.apply(instance, FixedPlacement::new)
	);
	private final List<BlockPos> positions;

	public static FixedPlacement of(BlockPos... blockPoss) {
		return new FixedPlacement(List.of(blockPoss));
	}

	private FixedPlacement(List<BlockPos> list) {
		this.positions = list;
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		int i = SectionPos.blockToSectionCoord(blockPos.getX());
		int j = SectionPos.blockToSectionCoord(blockPos.getZ());
		boolean bl = false;

		for (BlockPos blockPos2 : this.positions) {
			if (isSameChunk(i, j, blockPos2)) {
				bl = true;
				break;
			}
		}

		return !bl ? Stream.empty() : this.positions.stream().filter(blockPosx -> isSameChunk(i, j, blockPosx));
	}

	private static boolean isSameChunk(int i, int j, BlockPos blockPos) {
		return i == SectionPos.blockToSectionCoord(blockPos.getX()) && j == SectionPos.blockToSectionCoord(blockPos.getZ());
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.FIXED_PLACEMENT;
	}
}
