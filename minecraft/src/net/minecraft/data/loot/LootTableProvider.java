package net.minecraft.data.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final PackOutput.PathProvider pathProvider;
	private final Set<ResourceLocation> requiredTables;
	private final List<LootTableProvider.SubProviderEntry> subProviders;

	public LootTableProvider(PackOutput packOutput, Set<ResourceLocation> set, List<LootTableProvider.SubProviderEntry> list) {
		this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "loot_tables");
		this.subProviders = list;
		this.requiredTables = set;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		Map<ResourceLocation, LootTable> map = Maps.<ResourceLocation, LootTable>newHashMap();
		this.subProviders.forEach(subProviderEntry -> ((LootTableSubProvider)subProviderEntry.provider().get()).generate((resourceLocationx, builder) -> {
				if (map.put(resourceLocationx, builder.setParamSet(subProviderEntry.paramSet).build()) != null) {
					throw new IllegalStateException("Duplicate loot table " + resourceLocationx);
				}
			}));
		ValidationContext validationContext = new ValidationContext(LootContextParamSets.ALL_PARAMS, resourceLocationx -> null, map::get);

		for (ResourceLocation resourceLocation : Sets.difference(this.requiredTables, map.keySet())) {
			validationContext.reportProblem("Missing built-in table: " + resourceLocation);
		}

		map.forEach((resourceLocationx, lootTable) -> LootTables.validate(validationContext, resourceLocationx, lootTable));
		Multimap<String, String> multimap = validationContext.getProblems();
		if (!multimap.isEmpty()) {
			multimap.forEach((string, string2) -> LOGGER.warn("Found validation problem in {}: {}", string, string2));
			throw new IllegalStateException("Failed to validate loot tables, see logs");
		} else {
			return CompletableFuture.allOf((CompletableFuture[])map.entrySet().stream().map(entry -> {
				ResourceLocation resourceLocationx = (ResourceLocation)entry.getKey();
				LootTable lootTable = (LootTable)entry.getValue();
				Path path = this.pathProvider.json(resourceLocationx);
				return DataProvider.saveStable(cachedOutput, LootTables.serialize(lootTable), path);
			}).toArray(CompletableFuture[]::new));
		}
	}

	@Override
	public final String getName() {
		return "Loot Tables";
	}

	public static record SubProviderEntry(Supplier<LootTableSubProvider> provider, LootContextParamSet paramSet) {
	}
}
