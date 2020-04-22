package net.minecraft.server.level;

import java.util.concurrent.Executor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;

public class DerivedServerLevel extends ServerLevel {
	public DerivedServerLevel(
		ServerLevel serverLevel,
		MinecraftServer minecraftServer,
		Executor executor,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		DimensionType dimensionType,
		ChunkProgressListener chunkProgressListener
	) {
		super(
			minecraftServer,
			executor,
			levelStorageAccess,
			new DerivedLevelData(dimensionType, minecraftServer.getWorldData(), serverLevel.getLevelData()),
			dimensionType,
			chunkProgressListener
		);
		serverLevel.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(this.getWorldBorder()));
	}

	@Override
	protected void tickTime() {
	}
}
