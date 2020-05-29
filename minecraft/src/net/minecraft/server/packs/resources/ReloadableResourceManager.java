package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.Pack;
import net.minecraft.util.Unit;

public interface ReloadableResourceManager extends ResourceManager {
	default CompletableFuture<Unit> reload(Executor executor, Executor executor2, List<Pack> list, CompletableFuture<Unit> completableFuture) {
		return this.createFullReload(executor, executor2, completableFuture, list).done();
	}

	ReloadInstance createFullReload(Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture, List<Pack> list);

	void registerReloadListener(PreparableReloadListener preparableReloadListener);
}
