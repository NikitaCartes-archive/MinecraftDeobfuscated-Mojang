package net.minecraft.world.level.levelgen.placement.nether;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class CountMultiLayerDecorator extends FeatureDecorator<CountConfiguration> {
	public CountMultiLayerDecorator(Codec<CountConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, CountConfiguration countConfiguration, BlockPos blockPos) {
		List<BlockPos> list = Lists.<BlockPos>newArrayList();
		int i = 0;

		boolean bl;
		do {
			bl = false;

			for (int j = 0; j < countConfiguration.count().sample(random); j++) {
				int k = random.nextInt(16) + blockPos.getX();
				int l = random.nextInt(16) + blockPos.getZ();
				int m = decorationContext.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
				int n = findOnGroundYPosition(decorationContext, k, m, l, i);
				if (n != Integer.MAX_VALUE) {
					list.add(new BlockPos(k, n, l));
					bl = true;
				}
			}

			i++;
		} while (bl);

		return list.stream();
	}

	private static int findOnGroundYPosition(DecorationContext decorationContext, int i, int j, int k, int l) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, j, k);
		int m = 0;
		BlockState blockState = decorationContext.getBlockState(mutableBlockPos);

		for (int n = j; n >= 1; n--) {
			mutableBlockPos.setY(n - 1);
			BlockState blockState2 = decorationContext.getBlockState(mutableBlockPos);
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
