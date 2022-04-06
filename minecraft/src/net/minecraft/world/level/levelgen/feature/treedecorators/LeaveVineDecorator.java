package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;

public class LeaveVineDecorator extends TreeDecorator {
	public static final Codec<LeaveVineDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
		.fieldOf("probability")
		.<LeaveVineDecorator>xmap(LeaveVineDecorator::new, leaveVineDecorator -> leaveVineDecorator.probability)
		.codec();
	private final float probability;

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.LEAVE_VINE;
	}

	public LeaveVineDecorator(float f) {
		this.probability = f;
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
		list2.forEach(blockPos -> {
			if (randomSource.nextFloat() < this.probability) {
				BlockPos blockPos2 = blockPos.west();
				if (Feature.isAir(levelSimulatedReader, blockPos2)) {
					addHangingVine(levelSimulatedReader, blockPos2, VineBlock.EAST, biConsumer);
				}
			}

			if (randomSource.nextFloat() < this.probability) {
				BlockPos blockPos2 = blockPos.east();
				if (Feature.isAir(levelSimulatedReader, blockPos2)) {
					addHangingVine(levelSimulatedReader, blockPos2, VineBlock.WEST, biConsumer);
				}
			}

			if (randomSource.nextFloat() < this.probability) {
				BlockPos blockPos2 = blockPos.north();
				if (Feature.isAir(levelSimulatedReader, blockPos2)) {
					addHangingVine(levelSimulatedReader, blockPos2, VineBlock.SOUTH, biConsumer);
				}
			}

			if (randomSource.nextFloat() < this.probability) {
				BlockPos blockPos2 = blockPos.south();
				if (Feature.isAir(levelSimulatedReader, blockPos2)) {
					addHangingVine(levelSimulatedReader, blockPos2, VineBlock.NORTH, biConsumer);
				}
			}
		});
	}

	private static void addHangingVine(
		LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, BooleanProperty booleanProperty, BiConsumer<BlockPos, BlockState> biConsumer
	) {
		placeVine(biConsumer, blockPos, booleanProperty);
		int i = 4;

		for (BlockPos var5 = blockPos.below(); Feature.isAir(levelSimulatedReader, var5) && i > 0; i--) {
			placeVine(biConsumer, var5, booleanProperty);
			var5 = var5.below();
		}
	}
}
