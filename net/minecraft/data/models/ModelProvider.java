/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.models;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

public class ModelProvider
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public ModelProvider(DataGenerator dataGenerator) {
        this.generator = dataGenerator;
    }

    @Override
    public void run(CachedOutput cachedOutput) {
        Path path = this.generator.getOutputFolder();
        HashMap map = Maps.newHashMap();
        Consumer<BlockStateGenerator> consumer = blockStateGenerator -> {
            Block block = blockStateGenerator.getBlock();
            BlockStateGenerator blockStateGenerator2 = map.put(block, blockStateGenerator);
            if (blockStateGenerator2 != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + block);
            }
        };
        HashMap map2 = Maps.newHashMap();
        HashSet set = Sets.newHashSet();
        BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer = (resourceLocation, supplier) -> {
            Supplier supplier2 = map2.put(resourceLocation, supplier);
            if (supplier2 != null) {
                throw new IllegalStateException("Duplicate model definition for " + resourceLocation);
            }
        };
        Consumer<Item> consumer2 = set::add;
        new BlockModelGenerators(consumer, biConsumer, consumer2).run();
        new ItemModelGenerators(biConsumer).run();
        List list = Registry.BLOCK.stream().filter(block -> !map.containsKey(block)).collect(Collectors.toList());
        if (!list.isEmpty()) {
            throw new IllegalStateException("Missing blockstate definitions for: " + list);
        }
        Registry.BLOCK.forEach(block -> {
            Item item = Item.BY_BLOCK.get(block);
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
        this.saveCollection(cachedOutput, path, map, ModelProvider::createBlockStatePath);
        this.saveCollection(cachedOutput, path, map2, ModelProvider::createModelPath);
    }

    private <T> void saveCollection(CachedOutput cachedOutput, Path path, Map<T, ? extends Supplier<JsonElement>> map, BiFunction<Path, T, Path> biFunction) {
        map.forEach((object, supplier) -> {
            Path path2 = (Path)biFunction.apply(path, object);
            try {
                DataProvider.save(GSON, cachedOutput, (JsonElement)supplier.get(), path2);
            } catch (Exception exception) {
                LOGGER.error("Couldn't save {}", (Object)path2, (Object)exception);
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

