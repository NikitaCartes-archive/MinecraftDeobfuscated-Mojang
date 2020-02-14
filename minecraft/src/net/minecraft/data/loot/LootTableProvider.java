package net.minecraft.data.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootTableProvider implements DataProvider {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private final DataGenerator generator;
	private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> subProviders = ImmutableList.of(
		Pair.of(FishingLoot::new, LootContextParamSets.FISHING),
		Pair.of(ChestLoot::new, LootContextParamSets.CHEST),
		Pair.of(EntityLoot::new, LootContextParamSets.ENTITY),
		Pair.of(BlockLoot::new, LootContextParamSets.BLOCK),
		Pair.of(PiglinBarterLoot::new, LootContextParamSets.PIGLIN_BARTER),
		Pair.of(GiftLoot::new, LootContextParamSets.GIFT)
	);

	public LootTableProvider(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(HashCache hashCache) {
		Path path = this.generator.getOutputFolder();
		Map<ResourceLocation, LootTable> map = Maps.<ResourceLocation, LootTable>newHashMap();
		this.subProviders.forEach(pair -> ((Consumer)((Supplier)pair.getFirst()).get()).accept((BiConsumer)(resourceLocationx, builder) -> {
				if (map.put(resourceLocationx, builder.setParamSet((LootContextParamSet)pair.getSecond()).build()) != null) {
					throw new IllegalStateException("Duplicate loot table " + resourceLocationx);
				}
			}));
		ValidationContext validationContext = new ValidationContext(LootContextParamSets.ALL_PARAMS, resourceLocationx -> null, map::get);

		for (ResourceLocation resourceLocation : Sets.difference(BuiltInLootTables.all(), map.keySet())) {
			validationContext.reportProblem("Missing built-in table: " + resourceLocation);
		}

		map.forEach((resourceLocationx, lootTable) -> LootTables.validate(validationContext, resourceLocationx, lootTable));
		Multimap<String, String> multimap = validationContext.getProblems();
		if (!multimap.isEmpty()) {
			multimap.forEach((string, string2) -> LOGGER.warn("Found validation problem in " + string + ": " + string2));
			throw new IllegalStateException("Failed to validate loot tables, see logs");
		} else {
			map.forEach((resourceLocationx, lootTable) -> {
				Path path2 = createPath(path, resourceLocationx);

				try {
					DataProvider.save(GSON, hashCache, LootTables.serialize(lootTable), path2);
				} catch (IOException var6) {
					LOGGER.error("Couldn't save loot table {}", path2, var6);
				}
			});
		}
	}

	private static Path createPath(Path path, ResourceLocation resourceLocation) {
		return path.resolve("data/" + resourceLocation.getNamespace() + "/loot_tables/" + resourceLocation.getPath() + ".json");
	}

	@Override
	public String getName() {
		return "LootTables";
	}
}
