package net.minecraft.world.level.redstone;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DefaultRedstoneWireEvaluator extends RedstoneWireEvaluator {
	public DefaultRedstoneWireEvaluator(RedStoneWireBlock redStoneWireBlock) {
		super(redStoneWireBlock);
	}

	@Override
	public void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState, @Nullable Orientation orientation) {
		int i = this.calculateTargetStrength(level, blockPos);
		if ((Integer)blockState.getValue(RedStoneWireBlock.POWER) != i) {
			if (level.getBlockState(blockPos) == blockState) {
				level.setBlock(blockPos, blockState.setValue(RedStoneWireBlock.POWER, Integer.valueOf(i)), 2);
			}

			Set<BlockPos> set = Sets.<BlockPos>newHashSet();
			set.add(blockPos);

			for (Direction direction : Direction.values()) {
				set.add(blockPos.relative(direction));
			}

			for (BlockPos blockPos2 : set) {
				level.updateNeighborsAt(blockPos2, this.wireBlock);
			}
		}
	}

	private int calculateTargetStrength(Level level, BlockPos blockPos) {
		int i = this.getBlockSignal(level, blockPos);
		return i == 15 ? i : Math.max(i, this.getIncomingWireSignal(level, blockPos));
	}
}
