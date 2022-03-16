package net.minecraft.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.storage.WorldData;

public record WorldStem(
	CloseableResourceManager resourceManager, ReloadableServerResources dataPackResources, RegistryAccess.Frozen registryAccess, WorldData worldData
) implements AutoCloseable {
	public static CompletableFuture<WorldStem> load(
		WorldLoader.InitConfig initConfig, WorldLoader.WorldDataSupplier<WorldData> worldDataSupplier, Executor executor, Executor executor2
	) {
		return WorldLoader.load(initConfig, worldDataSupplier, WorldStem::new, executor, executor2);
	}

	public void close() {
		this.resourceManager.close();
	}
}
