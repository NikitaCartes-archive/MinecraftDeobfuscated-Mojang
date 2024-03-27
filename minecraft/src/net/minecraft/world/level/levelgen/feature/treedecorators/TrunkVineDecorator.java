package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;

public class TrunkVineDecorator extends TreeDecorator {
	public static final MapCodec<TrunkVineDecorator> CODEC = MapCodec.unit((Supplier<TrunkVineDecorator>)(() -> TrunkVineDecorator.INSTANCE));
	public static final TrunkVineDecorator INSTANCE = new TrunkVineDecorator();

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.TRUNK_VINE;
	}

	@Override
	public void place(TreeDecorator.Context context) {
		RandomSource randomSource = context.random();
		context.logs().forEach(blockPos -> {
			if (randomSource.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.west();
				if (context.isAir(blockPos2)) {
					context.placeVine(blockPos2, VineBlock.EAST);
				}
			}

			if (randomSource.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.east();
				if (context.isAir(blockPos2)) {
					context.placeVine(blockPos2, VineBlock.WEST);
				}
			}

			if (randomSource.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.north();
				if (context.isAir(blockPos2)) {
					context.placeVine(blockPos2, VineBlock.SOUTH);
				}
			}

			if (randomSource.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.south();
				if (context.isAir(blockPos2)) {
					context.placeVine(blockPos2, VineBlock.NORTH);
				}
			}
		});
	}
}
