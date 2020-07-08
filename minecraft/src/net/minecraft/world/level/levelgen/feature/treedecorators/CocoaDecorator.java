package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

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
	public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
		if (!(random.nextFloat() >= this.probability)) {
			int i = ((BlockPos)list.get(0)).getY();
			list.stream()
				.filter(blockPos -> blockPos.getY() - i <= 2)
				.forEach(
					blockPos -> {
						for (Direction direction : Direction.Plane.HORIZONTAL) {
							if (random.nextFloat() <= 0.25F) {
								Direction direction2 = direction.getOpposite();
								BlockPos blockPos2 = blockPos.offset(direction2.getStepX(), 0, direction2.getStepZ());
								if (Feature.isAir(worldGenLevel, blockPos2)) {
									BlockState blockState = Blocks.COCOA
										.defaultBlockState()
										.setValue(CocoaBlock.AGE, Integer.valueOf(random.nextInt(3)))
										.setValue(CocoaBlock.FACING, direction);
									this.setBlock(worldGenLevel, blockPos2, blockState, set, boundingBox);
								}
							}
						}
					}
				);
		}
	}
}
