package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class LeaveVineDecorator extends TreeDecorator {
	public LeaveVineDecorator() {
		super(TreeDecoratorType.LEAVE_VINE);
	}

	public <T> LeaveVineDecorator(Dynamic<T> dynamic) {
		this();
	}

	@Override
	public void place(LevelAccessor levelAccessor, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
		list2.forEach(blockPos -> {
			if (random.nextInt(4) == 0) {
				BlockPos blockPos2 = blockPos.west();
				if (AbstractTreeFeature.isAir(levelAccessor, blockPos2)) {
					this.addHangingVine(levelAccessor, blockPos2, VineBlock.EAST, set, boundingBox);
				}
			}

			if (random.nextInt(4) == 0) {
				BlockPos blockPos2 = blockPos.east();
				if (AbstractTreeFeature.isAir(levelAccessor, blockPos2)) {
					this.addHangingVine(levelAccessor, blockPos2, VineBlock.WEST, set, boundingBox);
				}
			}

			if (random.nextInt(4) == 0) {
				BlockPos blockPos2 = blockPos.north();
				if (AbstractTreeFeature.isAir(levelAccessor, blockPos2)) {
					this.addHangingVine(levelAccessor, blockPos2, VineBlock.SOUTH, set, boundingBox);
				}
			}

			if (random.nextInt(4) == 0) {
				BlockPos blockPos2 = blockPos.south();
				if (AbstractTreeFeature.isAir(levelAccessor, blockPos2)) {
					this.addHangingVine(levelAccessor, blockPos2, VineBlock.NORTH, set, boundingBox);
				}
			}
		});
	}

	private void addHangingVine(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BooleanProperty booleanProperty, Set<BlockPos> set, BoundingBox boundingBox) {
		this.placeVine(levelSimulatedRW, blockPos, booleanProperty, set, boundingBox);
		int i = 4;

		for (BlockPos var7 = blockPos.below(); AbstractTreeFeature.isAir(levelSimulatedRW, var7) && i > 0; i--) {
			this.placeVine(levelSimulatedRW, var7, booleanProperty, set, boundingBox);
			var7 = var7.below();
		}
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
				dynamicOps,
				dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("type"), dynamicOps.createString(Registry.TREE_DECORATOR_TYPES.getKey(this.type).toString())))
			)
			.getValue();
	}
}
