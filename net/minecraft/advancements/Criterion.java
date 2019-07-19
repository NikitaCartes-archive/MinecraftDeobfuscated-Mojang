/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public class Criterion {
    private final CriterionTriggerInstance trigger;

    public Criterion(CriterionTriggerInstance criterionTriggerInstance) {
        this.trigger = criterionTriggerInstance;
    }

    public Criterion() {
        this.trigger = null;
    }

    public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
    }

    public static Criterion criterionFromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "trigger"));
        CriterionTrigger criterionTrigger = CriteriaTriggers.getCriterion(resourceLocation);
        if (criterionTrigger == null) {
            throw new JsonSyntaxException("Invalid criterion trigger: " + resourceLocation);
        }
        Object criterionTriggerInstance = criterionTrigger.createInstance(GsonHelper.getAsJsonObject(jsonObject, "conditions", new JsonObject()), jsonDeserializationContext);
        return new Criterion((CriterionTriggerInstance)criterionTriggerInstance);
    }

    public static Criterion criterionFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return new Criterion();
    }

    public static Map<String, Criterion> criteriaFromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        HashMap<String, Criterion> map = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            map.put(entry.getKey(), Criterion.criterionFromJson(GsonHelper.convertToJsonObject(entry.getValue(), "criterion"), jsonDeserializationContext));
        }
        return map;
    }

    public static Map<String, Criterion> criteriaFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        HashMap<String, Criterion> map = Maps.newHashMap();
        int i = friendlyByteBuf.readVarInt();
        for (int j = 0; j < i; ++j) {
            map.put(friendlyByteBuf.readUtf(Short.MAX_VALUE), Criterion.criterionFromNetwork(friendlyByteBuf));
        }
        return map;
    }

    public static void serializeToNetwork(Map<String, Criterion> map, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(map.size());
        for (Map.Entry<String, Criterion> entry : map.entrySet()) {
            friendlyByteBuf.writeUtf(entry.getKey());
            entry.getValue().serializeToNetwork(friendlyByteBuf);
        }
    }

    @Nullable
    public CriterionTriggerInstance getTrigger() {
        return this.trigger;
    }

    public JsonElement serializeToJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("trigger", this.trigger.getCriterion().toString());
        jsonObject.add("conditions", this.trigger.serializeToJson());
        return jsonObject;
    }
}

