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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.PredicateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ServerAdvancementManager
extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private AdvancementList advancements = new AdvancementList();
    private final PredicateManager predicateManager;

    public ServerAdvancementManager(PredicateManager predicateManager) {
        super(GSON, "advancements");
        this.predicateManager = predicateManager;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        HashMap<ResourceLocation, Advancement.Builder> map2 = Maps.newHashMap();
        map.forEach((resourceLocation, jsonElement) -> {
            try {
                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "advancement");
                Advancement.Builder builder = Advancement.Builder.fromJson(jsonObject, new DeserializationContext((ResourceLocation)resourceLocation, this.predicateManager));
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

