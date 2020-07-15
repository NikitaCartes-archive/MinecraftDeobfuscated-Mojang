package net.minecraft.world.level.storage;

import net.minecraft.core.BlockPos;

public interface WritableLevelData extends LevelData {
	void setXSpawn(int i);

	void setYSpawn(int i);

	void setZSpawn(int i);

	void setSpawnAngle(float f);

	default void setSpawn(BlockPos blockPos, float f) {
		this.setXSpawn(blockPos.getX());
		this.setYSpawn(blockPos.getY());
		this.setZSpawn(blockPos.getZ());
		this.setSpawnAngle(f);
	}
}
