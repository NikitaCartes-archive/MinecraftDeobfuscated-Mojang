package net.minecraft.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class ReloadableServerRegistries {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().create();
	private static final RegistrationInfo DEFAULT_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());

	public static CompletableFuture<LayeredRegistryAccess<RegistryLayer>> reload(
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, ResourceManager resourceManager, Executor executor
	) {
		RegistryAccess.Frozen frozen = layeredRegistryAccess.getAccessForLoading(RegistryLayer.RELOADABLE);
		RegistryOps<JsonElement> registryOps = new ReloadableServerRegistries.EmptyTagLookupWrapper(frozen).createSerializationContext(JsonOps.INSTANCE);
		List<CompletableFuture<WritableRegistry<?>>> list = LootDataType.values()
			.map(lootDataType -> scheduleElementParse(lootDataType, registryOps, resourceManager, executor))
			.toList();
		CompletableFuture<List<WritableRegistry<?>>> completableFuture = Util.sequence(list);
		return completableFuture.thenApplyAsync(listx -> apply(layeredRegistryAccess, listx), executor);
	}

	private static <T> CompletableFuture<WritableRegistry<?>> scheduleElementParse(
		LootDataType<T> lootDataType, RegistryOps<JsonElement> registryOps, ResourceManager resourceManager, Executor executor
	) {
		return CompletableFuture.supplyAsync(
			() -> {
				WritableRegistry<T> writableRegistry = new MappedRegistry<>(lootDataType.registryKey(), Lifecycle.experimental());
				Map<ResourceLocation, JsonElement> map = new HashMap();
				String string = Registries.elementsDirPath(lootDataType.registryKey());
				SimpleJsonResourceReloadListener.scanDirectory(resourceManager, string, GSON, map);
				map.forEach(
					(resourceLocation, jsonElement) -> lootDataType.deserialize(resourceLocation, registryOps, jsonElement)
							.ifPresent(object -> writableRegistry.register(ResourceKey.create(lootDataType.registryKey(), resourceLocation), (T)object, DEFAULT_REGISTRATION_INFO))
				);
				return writableRegistry;
			},
			executor
		);
	}

	private static LayeredRegistryAccess<RegistryLayer> apply(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, List<WritableRegistry<?>> list) {
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess2 = createUpdatedRegistries(layeredRegistryAccess, list);
		ProblemReporter.Collector collector = new ProblemReporter.Collector();
		RegistryAccess.Frozen frozen = layeredRegistryAccess2.compositeAccess();
		ValidationContext validationContext = new ValidationContext(collector, LootContextParamSets.ALL_PARAMS, frozen.asGetterLookup());
		LootDataType.values().forEach(lootDataType -> validateRegistry(validationContext, lootDataType, frozen));
		collector.get().forEach((string, string2) -> LOGGER.warn("Found loot table element validation problem in {}: {}", string, string2));
		return layeredRegistryAccess2;
	}

	private static LayeredRegistryAccess<RegistryLayer> createUpdatedRegistries(
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, List<WritableRegistry<?>> list
	) {
		RegistryAccess registryAccess = new RegistryAccess.ImmutableRegistryAccess(list);
		((WritableRegistry)registryAccess.<LootTable>registryOrThrow(Registries.LOOT_TABLE))
			.register(BuiltInLootTables.EMPTY, LootTable.EMPTY, DEFAULT_REGISTRATION_INFO);
		return layeredRegistryAccess.replaceFrom(RegistryLayer.RELOADABLE, registryAccess.freeze());
	}

	private static <T> void validateRegistry(ValidationContext validationContext, LootDataType<T> lootDataType, RegistryAccess registryAccess) {
		Registry<T> registry = registryAccess.registryOrThrow(lootDataType.registryKey());
		registry.holders().forEach(reference -> lootDataType.runValidation(validationContext, reference.key(), (T)reference.value()));
	}

	static class EmptyTagLookupWrapper implements HolderLookup.Provider {
		private final RegistryAccess registryAccess;

		EmptyTagLookupWrapper(RegistryAccess registryAccess) {
			this.registryAccess = registryAccess;
		}

		@Override
		public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
			return this.registryAccess.listRegistries();
		}

		@Override
		public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
			return this.registryAccess.registry(resourceKey).map(Registry::asTagAddingLookup);
		}
	}

	public static class Holder {
		private final RegistryAccess.Frozen registries;

		public Holder(RegistryAccess.Frozen frozen) {
			this.registries = frozen;
		}

		public RegistryAccess.Frozen get() {
			return this.registries;
		}

		public HolderGetter.Provider lookup() {
			return this.registries.asGetterLookup();
		}

		public Collection<ResourceLocation> getKeys(ResourceKey<? extends Registry<?>> resourceKey) {
			return this.registries.registry(resourceKey).stream().flatMap(registry -> registry.holders().map(reference -> reference.key().location())).toList();
		}

		public LootTable getLootTable(ResourceKey<LootTable> resourceKey) {
			return (LootTable)this.registries
				.lookup(Registries.LOOT_TABLE)
				.flatMap(registryLookup -> registryLookup.get(resourceKey))
				.map(net.minecraft.core.Holder::value)
				.orElse(LootTable.EMPTY);
		}
	}
}
