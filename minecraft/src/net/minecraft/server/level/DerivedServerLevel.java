package net.minecraft.server.level;

import java.util.concurrent.Executor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorage;

public class DerivedServerLevel extends ServerLevel {
	public DerivedServerLevel(
		ServerLevel serverLevel,
		MinecraftServer minecraftServer,
		Executor executor,
		LevelStorage levelStorage,
		DimensionType dimensionType,
		ProfilerFiller profilerFiller,
		ChunkProgressListener chunkProgressListener
	) {
		super(minecraftServer, executor, levelStorage, new DerivedLevelData(serverLevel.getLevelData()), dimensionType, profilerFiller, chunkProgressListener);
		serverLevel.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(this.getWorldBorder()));
	}

	@Override
	protected void tickTime() {
	}
}
