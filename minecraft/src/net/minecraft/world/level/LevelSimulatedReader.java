package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelSimulatedReader {
	boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate);
}
