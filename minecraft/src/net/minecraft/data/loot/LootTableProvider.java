package net.minecraft.data.loot;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final PackOutput.PathProvider pathProvider;
	private final Set<ResourceKey<LootTable>> requiredTables;
	private final List<LootTableProvider.SubProviderEntry> subProviders;
	private final CompletableFuture<HolderLookup.Provider> registries;

	public LootTableProvider(
		PackOutput packOutput,
		Set<ResourceKey<LootTable>> set,
		List<LootTableProvider.SubProviderEntry> list,
		CompletableFuture<HolderLookup.Provider> completableFuture
	) {
		this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "loot_tables");
		this.subProviders = list;
		this.requiredTables = set;
		this.registries = completableFuture;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		return this.registries.thenCompose(provider -> this.run(cachedOutput, provider));
	}

	private CompletableFuture<?> run(CachedOutput cachedOutput, HolderLookup.Provider provider) {
		WritableRegistry<LootTable> writableRegistry = new MappedRegistry<>(Registries.LOOT_TABLE, Lifecycle.experimental());
		Map<RandomSupport.Seed128bit, ResourceLocation> map = new Object2ObjectOpenHashMap<>();
		this.subProviders.forEach(subProviderEntry -> ((LootTableSubProvider)subProviderEntry.provider().get()).generate(provider, (resourceKeyx, builder) -> {
				ResourceLocation resourceLocation = sequenceIdForLootTable(resourceKeyx);
				ResourceLocation resourceLocation2 = (ResourceLocation)map.put(RandomSequence.seedForKey(resourceLocation), resourceLocation);
				if (resourceLocation2 != null) {
					Util.logAndPauseIfInIde("Loot table random sequence seed collision on " + resourceLocation2 + " and " + resourceKeyx.location());
				}

				builder.setRandomSequence(resourceLocation);
				LootTable lootTable = builder.setParamSet(subProviderEntry.paramSet).build();
				writableRegistry.register(resourceKeyx, lootTable, RegistrationInfo.BUILT_IN);
			}));
		writableRegistry.freeze();
		ProblemReporter.Collector collector = new ProblemReporter.Collector();
		HolderGetter.Provider provider2 = new RegistryAccess.ImmutableRegistryAccess(List.of(writableRegistry)).freeze().asGetterLookup();
		ValidationContext validationContext = new ValidationContext(collector, LootContextParamSets.ALL_PARAMS, provider2);

		for (ResourceKey<LootTable> resourceKey : Sets.difference(this.requiredTables, writableRegistry.registryKeySet())) {
			collector.report("Missing built-in table: " + resourceKey.location());
		}

		writableRegistry.holders()
			.forEach(
				reference -> ((LootTable)reference.value())
						.validate(validationContext.setParams(((LootTable)reference.value()).getParamSet()).enterElement("{" + reference.key().location() + "}", reference.key()))
			);
		Multimap<String, String> multimap = collector.get();
		if (!multimap.isEmpty()) {
			multimap.forEach((string, string2) -> LOGGER.warn("Found validation problem in {}: {}", string, string2));
			throw new IllegalStateException("Failed to validate loot tables, see logs");
		} else {
			return CompletableFuture.allOf((CompletableFuture[])writableRegistry.entrySet().stream().map(entry -> {
				ResourceKey<LootTable> resourceKeyx = (ResourceKey<LootTable>)entry.getKey();
				LootTable lootTable = (LootTable)entry.getValue();
				Path path = this.pathProvider.json(resourceKeyx.location());
				return DataProvider.saveStable(cachedOutput, provider, LootTable.DIRECT_CODEC, lootTable, path);
			}).toArray(CompletableFuture[]::new));
		}
	}

	private static ResourceLocation sequenceIdForLootTable(ResourceKey<LootTable> resourceKey) {
		return resourceKey.location();
	}

	@Override
	public final String getName() {
		return "Loot Tables";
	}

	public static record SubProviderEntry(Supplier<LootTableSubProvider> provider, LootContextParamSet paramSet) {
	}
}
