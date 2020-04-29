package net.minecraft.world.level.storage;

import net.minecraft.core.BlockPos;

public interface WritableLevelData extends LevelData {
	void setXSpawn(int i);

	void setYSpawn(int i);

	void setZSpawn(int i);

	default void setSpawn(BlockPos blockPos) {
		this.setXSpawn(blockPos.getX());
		this.setYSpawn(blockPos.getY());
		this.setZSpawn(blockPos.getZ());
	}

	void setGameTime(long l);

	void setDayTime(long l);
}
