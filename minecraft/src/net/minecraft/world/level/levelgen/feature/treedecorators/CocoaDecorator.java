package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;

public class CocoaDecorator extends TreeDecorator {
	public static final MapCodec<CocoaDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
		.fieldOf("probability")
		.xmap(CocoaDecorator::new, cocoaDecorator -> cocoaDecorator.probability);
	private final float probability;

	public CocoaDecorator(float f) {
		this.probability = f;
	}

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.COCOA;
	}

	@Override
	public void place(TreeDecorator.Context context) {
		RandomSource randomSource = context.random();
		if (!(randomSource.nextFloat() >= this.probability)) {
			List<BlockPos> list = context.logs();
			int i = ((BlockPos)list.get(0)).getY();
			list.stream()
				.filter(blockPos -> blockPos.getY() - i <= 2)
				.forEach(
					blockPos -> {
						for (Direction direction : Direction.Plane.HORIZONTAL) {
							if (randomSource.nextFloat() <= 0.25F) {
								Direction direction2 = direction.getOpposite();
								BlockPos blockPos2 = blockPos.offset(direction2.getStepX(), 0, direction2.getStepZ());
								if (context.isAir(blockPos2)) {
									context.setBlock(
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
