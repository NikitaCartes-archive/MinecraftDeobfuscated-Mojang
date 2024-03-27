package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class LeaveVineDecorator extends TreeDecorator {
	public static final MapCodec<LeaveVineDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
		.fieldOf("probability")
		.xmap(LeaveVineDecorator::new, leaveVineDecorator -> leaveVineDecorator.probability);
	private final float probability;

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.LEAVE_VINE;
	}

	public LeaveVineDecorator(float f) {
		this.probability = f;
	}

	@Override
	public void place(TreeDecorator.Context context) {
		RandomSource randomSource = context.random();
		context.leaves().forEach(blockPos -> {
			if (randomSource.nextFloat() < this.probability) {
				BlockPos blockPos2 = blockPos.west();
				if (context.isAir(blockPos2)) {
					addHangingVine(blockPos2, VineBlock.EAST, context);
				}
			}

			if (randomSource.nextFloat() < this.probability) {
				BlockPos blockPos2 = blockPos.east();
				if (context.isAir(blockPos2)) {
					addHangingVine(blockPos2, VineBlock.WEST, context);
				}
			}

			if (randomSource.nextFloat() < this.probability) {
				BlockPos blockPos2 = blockPos.north();
				if (context.isAir(blockPos2)) {
					addHangingVine(blockPos2, VineBlock.SOUTH, context);
				}
			}

			if (randomSource.nextFloat() < this.probability) {
				BlockPos blockPos2 = blockPos.south();
				if (context.isAir(blockPos2)) {
					addHangingVine(blockPos2, VineBlock.NORTH, context);
				}
			}
		});
	}

	private static void addHangingVine(BlockPos blockPos, BooleanProperty booleanProperty, TreeDecorator.Context context) {
		context.placeVine(blockPos, booleanProperty);
		int i = 4;

		for (BlockPos var4 = blockPos.below(); context.isAir(var4) && i > 0; i--) {
			context.placeVine(var4, booleanProperty);
			var4 = var4.below();
		}
	}
}
