/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener
extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PATH_SUFFIX = ".json";
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private final Gson gson;
    private final String directory;

    public SimpleJsonResourceReloadListener(Gson gson, String string) {
        this.gson = gson;
        this.directory = string;
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        HashMap<ResourceLocation, JsonElement> map = Maps.newHashMap();
        int i = this.directory.length() + 1;
        for (ResourceLocation resourceLocation : resourceManager.listResources(this.directory, string -> string.endsWith(PATH_SUFFIX))) {
            String string2 = resourceLocation.getPath();
            ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), string2.substring(i, string2.length() - PATH_SUFFIX_LENGTH));
            try {
                Resource resource = resourceManager.getResource(resourceLocation);
                try {
                    InputStream inputStream = resource.getInputStream();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
                        JsonElement jsonElement = GsonHelper.fromJson(this.gson, (Reader)reader, JsonElement.class);
                        if (jsonElement != null) {
                            JsonElement jsonElement2 = map.put(resourceLocation2, jsonElement);
                            if (jsonElement2 == null) continue;
                            throw new IllegalStateException("Duplicate data file ignored with ID " + resourceLocation2);
                        }
                        LOGGER.error("Couldn't load data file {} from {} as it's null or empty", (Object)resourceLocation2, (Object)resourceLocation);
                    } finally {
                        if (inputStream == null) continue;
                        inputStream.close();
                    }
                } finally {
                    if (resource == null) continue;
                    resource.close();
                }
            } catch (JsonParseException | IOException | IllegalArgumentException exception) {
                LOGGER.error("Couldn't parse data file {} from {}", resourceLocation2, resourceLocation, exception);
            }
        }
        return map;
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }
}

