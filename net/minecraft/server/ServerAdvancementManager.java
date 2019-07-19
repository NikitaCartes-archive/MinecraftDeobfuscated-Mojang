/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ServerAdvancementManager
extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(Advancement.Builder.class, (jsonElement, type, jsonDeserializationContext) -> {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "advancement");
        return Advancement.Builder.fromJson(jsonObject, jsonDeserializationContext);
    }).registerTypeAdapter((Type)((Object)AdvancementRewards.class), new AdvancementRewards.Deserializer()).registerTypeHierarchyAdapter(Component.class, new Component.Serializer()).registerTypeHierarchyAdapter(Style.class, new Style.Serializer()).registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory()).create();
    private AdvancementList advancements = new AdvancementList();

    public ServerAdvancementManager() {
        super(GSON, "advancements");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        HashMap<ResourceLocation, Advancement.Builder> map2 = Maps.newHashMap();
        map.forEach((resourceLocation, jsonObject) -> {
            try {
                Advancement.Builder builder = GSON.fromJson((JsonElement)jsonObject, Advancement.Builder.class);
                map2.put((ResourceLocation)resourceLocation, builder);
            } catch (JsonParseException | IllegalArgumentException runtimeException) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", resourceLocation, (Object)runtimeException.getMessage());
            }
        });
        AdvancementList advancementList = new AdvancementList();
        advancementList.add(map2);
        for (Advancement advancement : advancementList.getRoots()) {
            if (advancement.getDisplay() == null) continue;
            TreeNodePosition.run(advancement);
        }
        this.advancements = advancementList;
    }

    @Nullable
    public Advancement getAdvancement(ResourceLocation resourceLocation) {
        return this.advancements.get(resourceLocation);
    }

    public Collection<Advancement> getAllAdvancements() {
        return this.advancements.getAllAdvancements();
    }
}

