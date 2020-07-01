package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class LeaveVineDecorator extends TreeDecorator {
	public static final Codec<LeaveVineDecorator> CODEC = Codec.unit((Supplier<LeaveVineDecorator>)(() -> LeaveVineDecorator.INSTANCE));
	public static final LeaveVineDecorator INSTANCE = new LeaveVineDecorator();

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.LEAVE_VINE;
	}

	@Override
	public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
		list2.forEach(blockPos -> {
			if (random.nextInt(4) == 0) {
				BlockPos blockPos2 = blockPos.west();
				if (Feature.isAir(worldGenLevel, blockPos2)) {
					this.addHangingVine(worldGenLevel, blockPos2, VineBlock.EAST, set, boundingBox);
				}
			}

			if (random.nextInt(4) == 0) {
				BlockPos blockPos2 = blockPos.east();
				if (Feature.isAir(worldGenLevel, blockPos2)) {
					this.addHangingVine(worldGenLevel, blockPos2, VineBlock.WEST, set, boundingBox);
				}
			}

			if (random.nextInt(4) == 0) {
				BlockPos blockPos2 = blockPos.north();
				if (Feature.isAir(worldGenLevel, blockPos2)) {
					this.addHangingVine(worldGenLevel, blockPos2, VineBlock.SOUTH, set, boundingBox);
				}
			}

			if (random.nextInt(4) == 0) {
				BlockPos blockPos2 = blockPos.south();
				if (Feature.isAir(worldGenLevel, blockPos2)) {
					this.addHangingVine(worldGenLevel, blockPos2, VineBlock.NORTH, set, boundingBox);
				}
			}
		});
	}

	private void addHangingVine(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BooleanProperty booleanProperty, Set<BlockPos> set, BoundingBox boundingBox) {
		this.placeVine(levelSimulatedRW, blockPos, booleanProperty, set, boundingBox);
		int i = 4;

		for (BlockPos var7 = blockPos.below(); Feature.isAir(levelSimulatedRW, var7) && i > 0; i--) {
			this.placeVine(levelSimulatedRW, var7, booleanProperty, set, boundingBox);
			var7 = var7.below();
		}
	}
}
