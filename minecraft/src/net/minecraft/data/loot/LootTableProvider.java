package net.minecraft.data.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final DataGenerator.PathProvider pathProvider;
	private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> subProviders = ImmutableList.of(
		Pair.of(FishingLoot::new, LootContextParamSets.FISHING),
		Pair.of(ChestLoot::new, LootContextParamSets.CHEST),
		Pair.of(EntityLoot::new, LootContextParamSets.ENTITY),
		Pair.of(BlockLoot::new, LootContextParamSets.BLOCK),
		Pair.of(PiglinBarterLoot::new, LootContextParamSets.PIGLIN_BARTER),
		Pair.of(GiftLoot::new, LootContextParamSets.GIFT)
	);

	public LootTableProvider(DataGenerator dataGenerator) {
		this.pathProvider = dataGenerator.createPathProvider(DataGenerator.Target.DATA_PACK, "loot_tables");
	}

	@Override
	public void run(CachedOutput cachedOutput) {
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
			multimap.forEach((string, string2) -> LOGGER.warn("Found validation problem in {}: {}", string, string2));
			throw new IllegalStateException("Failed to validate loot tables, see logs");
		} else {
			map.forEach((resourceLocationx, lootTable) -> {
				Path path = this.pathProvider.json(resourceLocationx);

				try {
					DataProvider.saveStable(cachedOutput, LootTables.serialize(lootTable), path);
				} catch (IOException var6x) {
					LOGGER.error("Couldn't save loot table {}", path, var6x);
				}
			});
		}
	}

	@Override
	public String getName() {
		return "LootTables";
	}
}
