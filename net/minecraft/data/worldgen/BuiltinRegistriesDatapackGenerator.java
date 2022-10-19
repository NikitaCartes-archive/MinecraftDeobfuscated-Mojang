/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public class BuiltinRegistriesDatapackGenerator
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput output;

    public BuiltinRegistriesDatapackGenerator(PackOutput packOutput) {
        this.output = packOutput;
    }

    @Override
    public void run(CachedOutput cachedOutput) {
        RegistryAccess.Frozen registryAccess = BuiltinRegistries.createAccess();
        RegistryOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        RegistryDataLoader.WORLDGEN_REGISTRIES.forEach(registryData -> this.dumpRegistryCap(cachedOutput, registryAccess, (DynamicOps<JsonElement>)dynamicOps, (RegistryDataLoader.RegistryData)registryData));
    }

    private <T> void dumpRegistryCap(CachedOutput cachedOutput, RegistryAccess registryAccess, DynamicOps<JsonElement> dynamicOps, RegistryDataLoader.RegistryData<T> registryData) {
        ResourceKey<Registry<T>> resourceKey = registryData.key();
        Registry<T> registry = registryAccess.registryOrThrow(resourceKey);
        PackOutput.PathProvider pathProvider = this.output.createPathProvider(PackOutput.Target.DATA_PACK, resourceKey.location().getPath());
        for (Map.Entry<ResourceKey<T>, T> entry : registry.entrySet()) {
            BuiltinRegistriesDatapackGenerator.dumpValue(pathProvider.json(entry.getKey().location()), cachedOutput, dynamicOps, registryData.elementCodec(), entry.getValue());
        }
    }

    private static <E> void dumpValue(Path path, CachedOutput cachedOutput, DynamicOps<JsonElement> dynamicOps, Encoder<E> encoder, E object) {
        try {
            Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object).resultOrPartial(string -> LOGGER.error("Couldn't serialize element {}: {}", (Object)path, string));
            if (optional.isPresent()) {
                DataProvider.saveStable(cachedOutput, optional.get(), path);
            }
        } catch (IOException iOException) {
            LOGGER.error("Couldn't save element {}", (Object)path, (Object)iOException);
        }
    }

    @Override
    public String getName() {
        return "Worldgen";
    }
}

