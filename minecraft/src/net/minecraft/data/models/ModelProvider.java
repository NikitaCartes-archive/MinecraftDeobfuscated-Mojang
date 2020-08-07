package net.minecraft.data.models;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModelProvider implements DataProvider {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private final DataGenerator generator;

	public ModelProvider(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(HashCache hashCache) {
		Path path = this.generator.getOutputFolder();
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
		List<Block> list = (List<Block>)Registry.BLOCK.stream().filter(block -> !map.containsKey(block)).collect(Collectors.toList());
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
			this.saveCollection(hashCache, path, map, ModelProvider::createBlockStatePath);
			this.saveCollection(hashCache, path, map2, ModelProvider::createModelPath);
		}
	}

	private <T> void saveCollection(HashCache hashCache, Path path, Map<T, ? extends Supplier<JsonElement>> map, BiFunction<Path, T, Path> biFunction) {
		map.forEach((object, supplier) -> {
			Path path2 = (Path)biFunction.apply(path, object);

			try {
				DataProvider.save(GSON, hashCache, (JsonElement)supplier.get(), path2);
			} catch (Exception var7) {
				LOGGER.error("Couldn't save {}", path2, var7);
			}
		});
	}

	private static Path createBlockStatePath(Path path, Block block) {
		ResourceLocation resourceLocation = Registry.BLOCK.getKey(block);
		return path.resolve("assets/" + resourceLocation.getNamespace() + "/blockstates/" + resourceLocation.getPath() + ".json");
	}

	private static Path createModelPath(Path path, ResourceLocation resourceLocation) {
		return path.resolve("assets/" + resourceLocation.getNamespace() + "/models/" + resourceLocation.getPath() + ".json");
	}

	@Override
	public String getName() {
		return "Block State Definitions";
	}
}
