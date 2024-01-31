package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootDataManager implements PreparableReloadListener, LootDataResolver {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().create();
	public static final LootDataId<LootTable> EMPTY_LOOT_TABLE_KEY = new LootDataId<>(LootDataType.TABLE, BuiltInLootTables.EMPTY);
	private final HolderLookup.Provider registries;
	private Map<LootDataId<?>, ?> elements = Map.of();
	private Multimap<LootDataType<?>, ResourceLocation> typeKeys = ImmutableMultimap.of();

	public LootDataManager(HolderLookup.Provider provider) {
		this.registries = provider;
	}

	@Override
	public final CompletableFuture<Void> reload(
		PreparableReloadListener.PreparationBarrier preparationBarrier,
		ResourceManager resourceManager,
		ProfilerFiller profilerFiller,
		ProfilerFiller profilerFiller2,
		Executor executor,
		Executor executor2
	) {
		Map<LootDataType<?>, Map<ResourceLocation, ?>> map = new HashMap();
		CompletableFuture<?>[] completableFutures = (CompletableFuture<?>[])LootDataType.values()
			.map(lootDataType -> scheduleElementParse(lootDataType, this.registries, resourceManager, executor, map))
			.toArray(CompletableFuture[]::new);
		return CompletableFuture.allOf(completableFutures).thenCompose(preparationBarrier::wait).thenAcceptAsync(void_ -> this.apply(map), executor2);
	}

	private static <T> CompletableFuture<?> scheduleElementParse(
		LootDataType<T> lootDataType,
		HolderLookup.Provider provider,
		ResourceManager resourceManager,
		Executor executor,
		Map<LootDataType<?>, Map<ResourceLocation, ?>> map
	) {
		RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, provider);
		Map<ResourceLocation, T> map2 = new HashMap();
		map.put(lootDataType, map2);
		return CompletableFuture.runAsync(
			() -> {
				Map<ResourceLocation, JsonElement> map2x = new HashMap();
				SimpleJsonResourceReloadListener.scanDirectory(resourceManager, lootDataType.directory(), GSON, map2x);
				map2x.forEach(
					(resourceLocation, jsonElement) -> lootDataType.deserialize(resourceLocation, registryOps, jsonElement)
							.ifPresent(object -> map2.put(resourceLocation, object))
				);
			},
			executor
		);
	}

	private void apply(Map<LootDataType<?>, Map<ResourceLocation, ?>> map) {
		Object object = ((Map)map.get(LootDataType.TABLE)).remove(BuiltInLootTables.EMPTY);
		if (object != null) {
			LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", BuiltInLootTables.EMPTY);
		}

		Builder<LootDataId<?>, Object> builder = ImmutableMap.builder();
		com.google.common.collect.ImmutableMultimap.Builder<LootDataType<?>, ResourceLocation> builder2 = ImmutableMultimap.builder();
		map.forEach((lootDataType, mapx) -> mapx.forEach((resourceLocation, objectx) -> {
				builder.put(new LootDataId(lootDataType, resourceLocation), objectx);
				builder2.put(lootDataType, resourceLocation);
			}));
		builder.put(EMPTY_LOOT_TABLE_KEY, LootTable.EMPTY);
		ProblemReporter.Collector collector = new ProblemReporter.Collector();
		final Map<LootDataId<?>, ?> map2 = builder.build();
		ValidationContext validationContext = new ValidationContext(collector, LootContextParamSets.ALL_PARAMS, new LootDataResolver() {
			@Nullable
			@Override
			public <T> T getElement(LootDataId<T> lootDataId) {
				return (T)map2.get(lootDataId);
			}
		});
		map2.forEach((lootDataId, objectx) -> castAndValidate(validationContext, lootDataId, objectx));
		collector.get().forEach((string, string2) -> LOGGER.warn("Found loot table element validation problem in {}: {}", string, string2));
		this.elements = map2;
		this.typeKeys = builder2.build();
	}

	private static <T> void castAndValidate(ValidationContext validationContext, LootDataId<T> lootDataId, Object object) {
		lootDataId.type().runValidation(validationContext, lootDataId, (T)object);
	}

	@Nullable
	@Override
	public <T> T getElement(LootDataId<T> lootDataId) {
		return (T)this.elements.get(lootDataId);
	}

	public Collection<ResourceLocation> getKeys(LootDataType<?> lootDataType) {
		return this.typeKeys.get(lootDataType);
	}
}
