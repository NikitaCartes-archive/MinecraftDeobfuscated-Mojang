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
import net.minecraft.tags.TagLoader;
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

	public static CompletableFuture<ReloadableServerRegistries.LoadResult> reload(
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, List<Registry.PendingTags<?>> list, ResourceManager resourceManager, Executor executor
	) {
		List<HolderLookup.RegistryLookup<?>> list2 = TagLoader.buildUpdatedLookups(layeredRegistryAccess.getAccessForLoading(RegistryLayer.RELOADABLE), list);
		HolderLookup.Provider provider = HolderLookup.Provider.create(list2.stream());
		RegistryOps<JsonElement> registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
		List<CompletableFuture<WritableRegistry<?>>> list3 = LootDataType.values()
			.map(lootDataType -> scheduleRegistryLoad(lootDataType, registryOps, resourceManager, executor))
			.toList();
		CompletableFuture<List<WritableRegistry<?>>> completableFuture = Util.sequence(list3);
		return completableFuture.thenApplyAsync(listx -> createAndValidateFullContext(layeredRegistryAccess, provider, listx), executor);
	}

	private static <T> CompletableFuture<WritableRegistry<?>> scheduleRegistryLoad(
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
				TagLoader.loadTagsForRegistry(resourceManager, writableRegistry);
				return writableRegistry;
			},
			executor
		);
	}

	private static ReloadableServerRegistries.LoadResult createAndValidateFullContext(
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, HolderLookup.Provider provider, List<WritableRegistry<?>> list
	) {
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess2 = createUpdatedRegistries(layeredRegistryAccess, list);
		HolderLookup.Provider provider2 = concatenateLookups(provider, layeredRegistryAccess2.getLayer(RegistryLayer.RELOADABLE));
		validateLootRegistries(provider2);
		return new ReloadableServerRegistries.LoadResult(layeredRegistryAccess2, provider2);
	}

	private static HolderLookup.Provider concatenateLookups(HolderLookup.Provider provider, HolderLookup.Provider provider2) {
		return HolderLookup.Provider.create(Stream.concat(provider.listRegistries(), provider2.listRegistries()));
	}

	private static void validateLootRegistries(HolderLookup.Provider provider) {
		ProblemReporter.Collector collector = new ProblemReporter.Collector();
		ValidationContext validationContext = new ValidationContext(collector, LootContextParamSets.ALL_PARAMS, provider.asGetterLookup());
		LootDataType.values().forEach(lootDataType -> validateRegistry(validationContext, lootDataType, provider));
		collector.get().forEach((string, string2) -> LOGGER.warn("Found loot table element validation problem in {}: {}", string, string2));
	}

	private static LayeredRegistryAccess<RegistryLayer> createUpdatedRegistries(
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, List<WritableRegistry<?>> list
	) {
		RegistryAccess registryAccess = new RegistryAccess.ImmutableRegistryAccess(list);
		((WritableRegistry)registryAccess.<LootTable>registryOrThrow(Registries.LOOT_TABLE))
			.register(BuiltInLootTables.EMPTY, LootTable.EMPTY, DEFAULT_REGISTRATION_INFO);
		return layeredRegistryAccess.replaceFrom(RegistryLayer.RELOADABLE, registryAccess.freeze());
	}

	private static <T> void validateRegistry(ValidationContext validationContext, LootDataType<T> lootDataType, HolderLookup.Provider provider) {
		HolderLookup<T> holderLookup = provider.lookupOrThrow(lootDataType.registryKey());
		holderLookup.listElements().forEach(reference -> lootDataType.runValidation(validationContext, reference.key(), (T)reference.value()));
	}

	public static class Holder {
		private final HolderLookup.Provider registries;

		public Holder(HolderLookup.Provider provider) {
			this.registries = provider;
		}

		public HolderGetter.Provider lookup() {
			return this.registries.asGetterLookup();
		}

		public Collection<ResourceLocation> getKeys(ResourceKey<? extends Registry<?>> resourceKey) {
			return this.registries.lookupOrThrow(resourceKey).listElementIds().map(ResourceKey::location).toList();
		}

		public LootTable getLootTable(ResourceKey<LootTable> resourceKey) {
			return (LootTable)this.registries
				.lookup(Registries.LOOT_TABLE)
				.flatMap(registryLookup -> registryLookup.get(resourceKey))
				.map(net.minecraft.core.Holder::value)
				.orElse(LootTable.EMPTY);
		}
	}

	public static record LoadResult(LayeredRegistryAccess<RegistryLayer> layers, HolderLookup.Provider lookupWithUpdatedTags) {
	}
}
