package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

public class CocoaDecorator extends TreeDecorator {
	public static final Codec<CocoaDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
		.fieldOf("probability")
		.<CocoaDecorator>xmap(CocoaDecorator::new, cocoaDecorator -> cocoaDecorator.probability)
		.codec();
	private final float probability;

	public CocoaDecorator(float f) {
		this.probability = f;
	}

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.COCOA;
	}

	@Override
	public void place(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		List<BlockPos> list,
		List<BlockPos> list2,
		List<BlockPos> list3
	) {
		if (!(randomSource.nextFloat() >= this.probability)) {
			int i = ((BlockPos)list.get(0)).getY();
			list.stream()
				.filter(blockPos -> blockPos.getY() - i <= 2)
				.forEach(
					blockPos -> {
						for (Direction direction : Direction.Plane.HORIZONTAL) {
							if (randomSource.nextFloat() <= 0.25F) {
								Direction direction2 = direction.getOpposite();
								BlockPos blockPos2 = blockPos.offset(direction2.getStepX(), 0, direction2.getStepZ());
								if (Feature.isAir(levelSimulatedReader, blockPos2)) {
									biConsumer.accept(
										blockPos2, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, Integer.valueOf(randomSource.nextInt(3))).setValue(CocoaBlock.FACING, direction)
									);
								}
							}
						}
					}
				);
		}
	}
}
