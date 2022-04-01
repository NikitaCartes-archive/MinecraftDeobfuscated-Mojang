package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;

public record WorldStem(
	CloseableResourceManager resourceManager, ReloadableServerResources dataPackResources, RegistryAccess.Frozen registryAccess, WorldData worldData
) implements AutoCloseable {
	public static CompletableFuture<WorldStem> load(
		WorldStem.InitConfig initConfig,
		WorldStem.DataPackConfigSupplier dataPackConfigSupplier,
		WorldStem.WorldDataSupplier worldDataSupplier,
		Executor executor,
		Executor executor2
	) {
		try {
			DataPackConfig dataPackConfig = (DataPackConfig)dataPackConfigSupplier.get();
			DataPackConfig dataPackConfig2 = MinecraftServer.configurePackRepository(initConfig.packRepository(), dataPackConfig, initConfig.safeMode());
			List<PackResources> list = initConfig.packRepository().openAllSelected();
			CloseableResourceManager closeableResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, list);
			Pair<WorldData, RegistryAccess.Frozen> pair = worldDataSupplier.get(closeableResourceManager, dataPackConfig2);
			WorldData worldData = pair.getFirst();
			RegistryAccess.Frozen frozen = pair.getSecond();
			return ReloadableServerResources.loadResources(
					closeableResourceManager, frozen, initConfig.commandSelection(), initConfig.functionCompilationLevel(), executor, executor2
				)
				.whenComplete((reloadableServerResources, throwable) -> {
					if (throwable != null) {
						closeableResourceManager.close();
					}
				})
				.thenApply(reloadableServerResources -> new WorldStem(closeableResourceManager, reloadableServerResources, frozen, worldData));
		} catch (Exception var12) {
			return CompletableFuture.failedFuture(var12);
		}
	}

	public void close() {
		this.resourceManager.close();
	}

	public void updateGlobals() {
		this.dataPackResources.updateRegistryTags(this.registryAccess);
	}

	@FunctionalInterface
	public interface DataPackConfigSupplier extends Supplier<DataPackConfig> {
		static WorldStem.DataPackConfigSupplier loadFromWorld(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
			return () -> {
				DataPackConfig dataPackConfig = levelStorageAccess.getDataPacks();
				if (dataPackConfig == null) {
					throw new IllegalStateException("Failed to load data pack config");
				} else {
					return dataPackConfig;
				}
			};
		}
	}

	public static record InitConfig(PackRepository packRepository, Commands.CommandSelection commandSelection, int functionCompilationLevel, boolean safeMode) {
	}

	@FunctionalInterface
	public interface WorldDataSupplier {
		Pair<WorldData, RegistryAccess.Frozen> get(ResourceManager resourceManager, DataPackConfig dataPackConfig);

		static WorldStem.WorldDataSupplier loadFromWorld(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
			return (resourceManager, dataPackConfig) -> {
				RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
				DynamicOps<Tag> dynamicOps = RegistryOps.createAndLoad(NbtOps.INSTANCE, writable, resourceManager);
				WorldData worldData = levelStorageAccess.getDataTag(dynamicOps, dataPackConfig, writable.allElementsLifecycle());
				if (worldData == null) {
					throw new IllegalStateException("Failed to load world");
				} else {
					return Pair.of(worldData, writable.freeze());
				}
			};
		}
	}
}
