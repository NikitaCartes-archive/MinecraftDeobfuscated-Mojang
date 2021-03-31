package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;

public class LeaveVineDecorator extends TreeDecorator {
	public static final Codec<LeaveVineDecorator> CODEC = Codec.unit((Supplier<LeaveVineDecorator>)(() -> LeaveVineDecorator.INSTANCE));
	public static final LeaveVineDecorator INSTANCE = new LeaveVineDecorator();

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.LEAVE_VINE;
	}

	@Override
	public void place(
		LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, List<BlockPos> list, List<BlockPos> list2
	) {
		list2.forEach(blockPos -> {
			if (random.nextInt(4) == 0) {
				BlockPos blockPos2 = blockPos.west();
				if (Feature.isAir(levelSimulatedReader, blockPos2)) {
					addHangingVine(levelSimulatedReader, blockPos2, VineBlock.EAST, biConsumer);
				}
			}

			if (random.nextInt(4) == 0) {
				BlockPos blockPos2 = blockPos.east();
				if (Feature.isAir(levelSimulatedReader, blockPos2)) {
					addHangingVine(levelSimulatedReader, blockPos2, VineBlock.WEST, biConsumer);
				}
			}

			if (random.nextInt(4) == 0) {
				BlockPos blockPos2 = blockPos.north();
				if (Feature.isAir(levelSimulatedReader, blockPos2)) {
					addHangingVine(levelSimulatedReader, blockPos2, VineBlock.SOUTH, biConsumer);
				}
			}

			if (random.nextInt(4) == 0) {
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
