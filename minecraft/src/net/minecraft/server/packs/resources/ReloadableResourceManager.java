package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.PackResources;
import net.minecraft.util.Unit;

public interface ReloadableResourceManager extends ResourceManager, AutoCloseable {
	default CompletableFuture<Unit> reload(Executor executor, Executor executor2, List<PackResources> list, CompletableFuture<Unit> completableFuture) {
		return this.createReload(executor, executor2, completableFuture, list).done();
	}

	ReloadInstance createReload(Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture, List<PackResources> list);

	void registerReloadListener(PreparableReloadListener preparableReloadListener);

	void close();
}
