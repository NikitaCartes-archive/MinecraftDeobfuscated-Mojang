package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

@Deprecated
public class CountOnEveryLayerPlacement extends PlacementModifier {
	public static final MapCodec<CountOnEveryLayerPlacement> CODEC = IntProvider.codec(0, 256)
		.fieldOf("count")
		.xmap(CountOnEveryLayerPlacement::new, countOnEveryLayerPlacement -> countOnEveryLayerPlacement.count);
	private final IntProvider count;

	private CountOnEveryLayerPlacement(IntProvider intProvider) {
		this.count = intProvider;
	}

	public static CountOnEveryLayerPlacement of(IntProvider intProvider) {
		return new CountOnEveryLayerPlacement(intProvider);
	}

	public static CountOnEveryLayerPlacement of(int i) {
		return of(ConstantInt.of(i));
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		Builder<BlockPos> builder = Stream.builder();
		int i = 0;

		boolean bl;
		do {
			bl = false;

			for (int j = 0; j < this.count.sample(randomSource); j++) {
				int k = randomSource.nextInt(16) + blockPos.getX();
				int l = randomSource.nextInt(16) + blockPos.getZ();
				int m = placementContext.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
				int n = findOnGroundYPosition(placementContext, k, m, l, i);
				if (n != Integer.MAX_VALUE) {
					builder.add(new BlockPos(k, n, l));
					bl = true;
				}
			}

			i++;
		} while (bl);

		return builder.build();
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.COUNT_ON_EVERY_LAYER;
	}

	private static int findOnGroundYPosition(PlacementContext placementContext, int i, int j, int k, int l) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, j, k);
		int m = 0;
		BlockState blockState = placementContext.getBlockState(mutableBlockPos);

		for (int n = j; n >= placementContext.getMinY() + 1; n--) {
			mutableBlockPos.setY(n - 1);
			BlockState blockState2 = placementContext.getBlockState(mutableBlockPos);
			if (!isEmpty(blockState2) && isEmpty(blockState) && !blockState2.is(Blocks.BEDROCK)) {
				if (m == l) {
					return mutableBlockPos.getY() + 1;
				}

				m++;
			}

			blockState = blockState2;
		}

		return Integer.MAX_VALUE;
	}

	private static boolean isEmpty(BlockState blockState) {
		return blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.LAVA);
	}
}
