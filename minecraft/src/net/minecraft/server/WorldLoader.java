package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
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
			List<Registry.PendingTags<?>> list = TagLoader.loadTagsForExistingRegistries(closeableResourceManager, layeredRegistryAccess.getLayer(RegistryLayer.STATIC));
			RegistryAccess.Frozen frozen = layeredRegistryAccess.getAccessForLoading(RegistryLayer.WORLDGEN);
			List<HolderLookup.RegistryLookup<?>> list2 = TagLoader.buildUpdatedLookups(frozen, list);
			RegistryAccess.Frozen frozen2 = RegistryDataLoader.load(closeableResourceManager, list2, RegistryDataLoader.WORLDGEN_REGISTRIES);
			List<HolderLookup.RegistryLookup<?>> list3 = Stream.concat(list2.stream(), frozen2.listRegistries()).toList();
			RegistryAccess.Frozen frozen3 = RegistryDataLoader.load(closeableResourceManager, list3, RegistryDataLoader.DIMENSION_REGISTRIES);
			WorldDataConfiguration worldDataConfiguration = pair.getFirst();
			HolderLookup.Provider provider = HolderLookup.Provider.create(list3.stream());
			WorldLoader.DataLoadOutput<D> dataLoadOutput = worldDataSupplier.get(
				new WorldLoader.DataLoadContext(closeableResourceManager, worldDataConfiguration, provider, frozen3)
			);
			LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess2 = layeredRegistryAccess.replaceFrom(
				RegistryLayer.WORLDGEN, frozen2, dataLoadOutput.finalDimensions
			);
			return ReloadableServerResources.loadResources(
					closeableResourceManager,
					layeredRegistryAccess2,
					list,
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
					reloadableServerResources.updateStaticRegistryTags();
					return resultFactory.create(closeableResourceManager, reloadableServerResources, layeredRegistryAccess2, dataLoadOutput.cookie);
				}, executor2);
		} catch (Exception var18) {
			return CompletableFuture.failedFuture(var18);
		}
	}

	public static record DataLoadContext(
		ResourceManager resources, WorldDataConfiguration dataConfiguration, HolderLookup.Provider datapackWorldgen, RegistryAccess.Frozen datapackDimensions
	) {
	}

	public static record DataLoadOutput<D>(D cookie, RegistryAccess.Frozen finalDimensions) {
	}

	public static record InitConfig(WorldLoader.PackConfig packConfig, Commands.CommandSelection commandSelection, int functionCompilationLevel) {
	}

	public static record PackConfig(PackRepository packRepository, WorldDataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
		public Pair<WorldDataConfiguration, CloseableResourceManager> createResourceManager() {
			WorldDataConfiguration worldDataConfiguration = MinecraftServer.configurePackRepository(
				this.packRepository, this.initialDataConfig, this.initMode, this.safeMode
			);
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
