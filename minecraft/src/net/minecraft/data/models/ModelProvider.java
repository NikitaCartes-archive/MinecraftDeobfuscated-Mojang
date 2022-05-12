package net.minecraft.data.models;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

public class ModelProvider implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final DataGenerator.PathProvider blockStatePathProvider;
	private final DataGenerator.PathProvider modelPathProvider;

	public ModelProvider(DataGenerator dataGenerator) {
		this.blockStatePathProvider = dataGenerator.createPathProvider(DataGenerator.Target.RESOURCE_PACK, "blockstates");
		this.modelPathProvider = dataGenerator.createPathProvider(DataGenerator.Target.RESOURCE_PACK, "models");
	}

	@Override
	public void run(CachedOutput cachedOutput) {
		Map<Block, BlockStateGenerator> map = Maps.<Block, BlockStateGenerator>newHashMap();
		Consumer<BlockStateGenerator> consumer = blockStateGenerator -> {
			Block block = blockStateGenerator.getBlock();
			BlockStateGenerator blockStateGenerator2 = (BlockStateGenerator)map.put(block, blockStateGenerator);
			if (blockStateGenerator2 != null) {
				throw new IllegalStateException("Duplicate blockstate definition for " + block);
			}
		};
		Map<ResourceLocation, Supplier<JsonElement>> map2 = Maps.<ResourceLocation, Supplier<JsonElement>>newHashMap();
		Set<Item> set = Sets.<Item>newHashSet();
		BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer = (resourceLocation, supplier) -> {
			Supplier<JsonElement> supplier2 = (Supplier<JsonElement>)map2.put(resourceLocation, supplier);
			if (supplier2 != null) {
				throw new IllegalStateException("Duplicate model definition for " + resourceLocation);
			}
		};
		Consumer<Item> consumer2 = set::add;
		new BlockModelGenerators(consumer, biConsumer, consumer2).run();
		new ItemModelGenerators(biConsumer).run();
		List<Block> list = Registry.BLOCK.stream().filter(block -> !map.containsKey(block)).toList();
		if (!list.isEmpty()) {
			throw new IllegalStateException("Missing blockstate definitions for: " + list);
		} else {
			Registry.BLOCK.forEach(block -> {
				Item item = (Item)Item.BY_BLOCK.get(block);
				if (item != null) {
					if (set.contains(item)) {
						return;
					}

					ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(item);
					if (!map2.containsKey(resourceLocation)) {
						map2.put(resourceLocation, new DelegatedModel(ModelLocationUtils.getModelLocation(block)));
					}
				}
			});
			this.saveCollection(cachedOutput, map, block -> this.blockStatePathProvider.json(block.builtInRegistryHolder().key().location()));
			this.saveCollection(cachedOutput, map2, this.modelPathProvider::json);
		}
	}

	private <T> void saveCollection(CachedOutput cachedOutput, Map<T, ? extends Supplier<JsonElement>> map, Function<T, Path> function) {
		map.forEach((object, supplier) -> {
			Path path = (Path)function.apply(object);

			try {
				DataProvider.saveStable(cachedOutput, (JsonElement)supplier.get(), path);
			} catch (Exception var6) {
				LOGGER.error("Couldn't save {}", path, var6);
			}
		});
	}

	@Override
	public String getName() {
		return "Block State Definitions";
	}
}
