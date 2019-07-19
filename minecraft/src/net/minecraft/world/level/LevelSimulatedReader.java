package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public interface LevelSimulatedReader {
	boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate);

	BlockPos getHeightmapPos(Heightmap.Types types, BlockPos blockPos);
}
