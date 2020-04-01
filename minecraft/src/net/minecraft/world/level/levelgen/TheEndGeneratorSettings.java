package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.core.BlockPos;

public class TheEndGeneratorSettings extends ChunkGeneratorSettings {
	private BlockPos spawnPosition;

	public TheEndGeneratorSettings setSpawnPosition(BlockPos blockPos) {
		this.spawnPosition = blockPos;
		return this;
	}

	public BlockPos getSpawnPosition() {
		return this.spawnPosition;
	}

	public TheEndGeneratorSettings() {
	}

	public TheEndGeneratorSettings(Random random) {
		this.defaultBlock = this.randomGroundBlock(random);
		this.defaultFluid = this.randomLiquidBlock(random);
	}
}
