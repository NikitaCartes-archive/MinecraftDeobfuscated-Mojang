package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.WorldDataConfiguration;
import org.slf4j.Logger;

public class WorldLoader {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static <D, R> CompletableFuture<R> load(
		WorldLoader.InitConfig initConfig,
		WorldLoader.WorldDataSupplier<D> worldDataSupplier,
		WorldLoader.ResultFactory<D, R> resultFactory,
		Executor executor,
		Executor executor2
	) {
		try {
			Pair<WorldDataConfiguration, CloseableResourceManager> pair = initConfig.packConfig.createResourceManager();
			CloseableResourceManager closeableResourceManager = pair.getSecond();
			LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = RegistryLayer.createRegistryAccess();
			LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess2 = loadAndReplaceLayer(
				closeableResourceManager, layeredRegistryAccess, RegistryLayer.WORLDGEN, RegistryDataLoader.WORLDGEN_REGISTRIES
			);
			RegistryAccess.Frozen frozen = layeredRegistryAccess2.getAccessForLoading(RegistryLayer.DIMENSIONS);
			RegistryAccess.Frozen frozen2 = RegistryDataLoader.load(closeableResourceManager, frozen, RegistryDataLoader.DIMENSION_REGISTRIES);
			WorldDataConfiguration worldDataConfiguration = pair.getFirst();
			WorldLoader.DataLoadOutput<D> dataLoadOutput = worldDataSupplier.get(
				new WorldLoader.DataLoadContext(closeableResourceManager, worldDataConfiguration, frozen, frozen2)
			);
			LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess3 = layeredRegistryAccess2.replaceFrom(RegistryLayer.DIMENSIONS, dataLoadOutput.finalDimensions);
			return ReloadableServerResources.loadResources(
					closeableResourceManager,
					layeredRegistryAccess3,
					worldDataConfiguration.enabledFeatures(),
					initConfig.commandSelection(),
					initConfig.functionCompilationLevel(),
					executor,
					executor2
				)
				.whenComplete((reloadableServerResources, throwable) -> {
					if (throwable != null) {
						closeableResourceManager.close();
					}
				})
				.thenApplyAsync(reloadableServerResources -> {
					reloadableServerResources.updateRegistryTags();
					return resultFactory.create(closeableResourceManager, reloadableServerResources, layeredRegistryAccess3, dataLoadOutput.cookie);
				}, executor2);
		} catch (Exception var14) {
			return CompletableFuture.failedFuture(var14);
		}
	}

	private static RegistryAccess.Frozen loadLayer(
		ResourceManager resourceManager,
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
		RegistryLayer registryLayer,
		List<RegistryDataLoader.RegistryData<?>> list
	) {
		RegistryAccess.Frozen frozen = layeredRegistryAccess.getAccessForLoading(registryLayer);
		return RegistryDataLoader.load(resourceManager, frozen, list);
	}

	private static LayeredRegistryAccess<RegistryLayer> loadAndReplaceLayer(
		ResourceManager resourceManager,
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
		RegistryLayer registryLayer,
		List<RegistryDataLoader.RegistryData<?>> list
	) {
		RegistryAccess.Frozen frozen = loadLayer(resourceManager, layeredRegistryAccess, registryLayer, list);
		return layeredRegistryAccess.replaceFrom(registryLayer, frozen);
	}

	public static record DataLoadContext(
		ResourceManager resources, WorldDataConfiguration dataConfiguration, RegistryAccess.Frozen datapackWorldgen, RegistryAccess.Frozen datapackDimensions
	) {
	}

	public static record DataLoadOutput<D>(D cookie, RegistryAccess.Frozen finalDimensions) {
	}

	public static record InitConfig(WorldLoader.PackConfig packConfig, Commands.CommandSelection commandSelection, int functionCompilationLevel) {
	}

	public static record PackConfig(PackRepository packRepository, WorldDataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
		public Pair<WorldDataConfiguration, CloseableResourceManager> createResourceManager() {
			FeatureFlagSet featureFlagSet = this.initMode ? FeatureFlags.REGISTRY.allFlags() : this.initialDataConfig.enabledFeatures();
			WorldDataConfiguration worldDataConfiguration = MinecraftServer.configurePackRepository(
				this.packRepository, this.initialDataConfig.dataPacks(), this.safeMode, featureFlagSet
			);
			if (!this.initMode) {
				worldDataConfiguration = worldDataConfiguration.expandFeatures(this.initialDataConfig.enabledFeatures());
			}

			List<PackResources> list = this.packRepository.openAllSelected();
			CloseableResourceManager closeableResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, list);
			return Pair.of(worldDataConfiguration, closeableResourceManager);
		}
	}

	@FunctionalInterface
	public interface ResultFactory<D, R> {
		R create(
			CloseableResourceManager closeableResourceManager,
			ReloadableServerResources reloadableServerResources,
			LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
			D object
		);
	}

	@FunctionalInterface
	public interface WorldDataSupplier<D> {
		WorldLoader.DataLoadOutput<D> get(WorldLoader.DataLoadContext dataLoadContext);
	}
}
