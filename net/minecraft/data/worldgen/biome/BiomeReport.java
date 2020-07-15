/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.biome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeReport
implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;

    public BiomeReport(DataGenerator dataGenerator) {
        this.generator = dataGenerator;
    }

    @Override
    public void run(HashCache hashCache) {
        Path path = this.generator.getOutputFolder();
        for (Map.Entry<ResourceKey<Biome>, Biome> entry : BuiltinRegistries.BIOME.entrySet()) {
            Path path2 = BiomeReport.createPath(path, entry.getKey().location());
            Biome biome = entry.getValue();
            Function<Supplier<Biome>, DataResult<Supplier<Biome>>> function = JsonOps.INSTANCE.withEncoder(Biome.CODEC);
            try {
                Optional optional = function.apply(() -> biome).result();
                if (optional.isPresent()) {
                    DataProvider.save(GSON, hashCache, (JsonElement)optional.get(), path2);
                    continue;
                }
                LOGGER.error("Couldn't serialize biome {}", (Object)path2);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't save biome {}", (Object)path2, (Object)iOException);
            }
        }
    }

    private static Path createPath(Path path, ResourceLocation resourceLocation) {
        return path.resolve("reports/biomes/" + resourceLocation.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Biomes";
    }
}

