package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.Pack;
import net.minecraft.util.Unit;

public interface ReloadableResourceManager extends ResourceManager {
	CompletableFuture<Unit> reload(Executor executor, Executor executor2, List<Pack> list, CompletableFuture<Unit> completableFuture);

	@Environment(EnvType.CLIENT)
	ReloadInstance createQueuedReload(Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture);

	@Environment(EnvType.CLIENT)
	ReloadInstance createFullReload(Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture, List<Pack> list);

	void registerReloadListener(PreparableReloadListener preparableReloadListener);
}
