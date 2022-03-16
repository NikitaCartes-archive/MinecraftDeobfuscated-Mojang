/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.slf4j.Logger;

public class BiomeParametersDumpReport
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;

    public BiomeParametersDumpReport(DataGenerator dataGenerator) {
        this.generator = dataGenerator;
    }

    @Override
    public void run(HashCache hashCache) {
        Path path = this.generator.getOutputFolder();
        RegistryAccess.Frozen frozen = RegistryAccess.BUILTIN.get();
        RegistryOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, frozen);
        Registry<Biome> registry = frozen.registryOrThrow(Registry.BIOME_REGISTRY);
        MultiNoiseBiomeSource.Preset.getPresets().forEach(pair -> {
            MultiNoiseBiomeSource multiNoiseBiomeSource = ((MultiNoiseBiomeSource.Preset)pair.getSecond()).biomeSource(registry, false);
            BiomeParametersDumpReport.dumpValue(BiomeParametersDumpReport.createPath(path, (ResourceLocation)pair.getFirst()), hashCache, dynamicOps, MultiNoiseBiomeSource.CODEC, multiNoiseBiomeSource);
        });
    }

    private static <E> void dumpValue(Path path, HashCache hashCache, DynamicOps<JsonElement> dynamicOps, Encoder<E> encoder, E object) {
        try {
            Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object).resultOrPartial(string -> LOGGER.error("Couldn't serialize element {}: {}", (Object)path, string));
            if (optional.isPresent()) {
                DataProvider.save(GSON, hashCache, optional.get(), path);
            }
        } catch (IOException iOException) {
            LOGGER.error("Couldn't save element {}", (Object)path, (Object)iOException);
        }
    }

    private static Path createPath(Path path, ResourceLocation resourceLocation) {
        return BiomeParametersDumpReport.resolveTopPath(path).resolve(resourceLocation.getNamespace()).resolve(resourceLocation.getPath() + ".json");
    }

    private static Path resolveTopPath(Path path) {
        return path.resolve("reports").resolve("biome_parameters");
    }

    @Override
    public String getName() {
        return "Biome Parameters";
    }
}

