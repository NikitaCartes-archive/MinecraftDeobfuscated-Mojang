package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.DataPackConfig;

public class WorldLoader {
	public static <D, R> CompletableFuture<R> load(
		WorldLoader.InitConfig initConfig,
		WorldLoader.WorldDataSupplier<D> worldDataSupplier,
		WorldLoader.ResultFactory<D, R> resultFactory,
		Executor executor,
		Executor executor2
	) {
		try {
			Pair<DataPackConfig, CloseableResourceManager> pair = initConfig.packConfig.createResourceManager();
			CloseableResourceManager closeableResourceManager = pair.getSecond();
			Pair<D, RegistryAccess.Frozen> pair2 = worldDataSupplier.get(closeableResourceManager, pair.getFirst());
			D object = pair2.getFirst();
			RegistryAccess.Frozen frozen = pair2.getSecond();
			return ReloadableServerResources.loadResources(
					closeableResourceManager, frozen, initConfig.commandSelection(), initConfig.functionCompilationLevel(), executor, executor2
				)
				.whenComplete((reloadableServerResources, throwable) -> {
					if (throwable != null) {
						closeableResourceManager.close();
					}
				})
				.thenApplyAsync(reloadableServerResources -> {
					reloadableServerResources.updateRegistryTags(frozen);
					return resultFactory.create(closeableResourceManager, reloadableServerResources, frozen, object);
				}, executor2);
		} catch (Exception var10) {
			return CompletableFuture.failedFuture(var10);
		}
	}

	public static record InitConfig(WorldLoader.PackConfig packConfig, Commands.CommandSelection commandSelection, int functionCompilationLevel) {
	}

	public static record PackConfig(PackRepository packRepository, DataPackConfig initialDataPacks, boolean safeMode) {
		public Pair<DataPackConfig, CloseableResourceManager> createResourceManager() {
			DataPackConfig dataPackConfig = MinecraftServer.configurePackRepository(this.packRepository, this.initialDataPacks, this.safeMode);
			List<PackResources> list = this.packRepository.openAllSelected();
			CloseableResourceManager closeableResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, list);
			return Pair.of(dataPackConfig, closeableResourceManager);
		}
	}

	@FunctionalInterface
	public interface ResultFactory<D, R> {
		R create(CloseableResourceManager closeableResourceManager, ReloadableServerResources reloadableServerResources, RegistryAccess.Frozen frozen, D object);
	}

	@FunctionalInterface
	public interface WorldDataSupplier<D> {
		Pair<D, RegistryAccess.Frozen> get(ResourceManager resourceManager, DataPackConfig dataPackConfig);
	}
}
