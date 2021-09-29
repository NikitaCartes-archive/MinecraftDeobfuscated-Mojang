/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public class Criterion {
    @Nullable
    private final CriterionTriggerInstance trigger;

    public Criterion(CriterionTriggerInstance criterionTriggerInstance) {
        this.trigger = criterionTriggerInstance;
    }

    public Criterion() {
        this.trigger = null;
    }

    public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
    }

    public static Criterion criterionFromJson(JsonObject jsonObject, DeserializationContext deserializationContext) {
        ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "trigger"));
        CriterionTrigger criterionTrigger = CriteriaTriggers.getCriterion(resourceLocation);
        if (criterionTrigger == null) {
            throw new JsonSyntaxException("Invalid criterion trigger: " + resourceLocation);
        }
        Object criterionTriggerInstance = criterionTrigger.createInstance(GsonHelper.getAsJsonObject(jsonObject, "conditions", new JsonObject()), deserializationContext);
        return new Criterion((CriterionTriggerInstance)criterionTriggerInstance);
    }

    public static Criterion criterionFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return new Criterion();
    }

    public static Map<String, Criterion> criteriaFromJson(JsonObject jsonObject, DeserializationContext deserializationContext) {
        HashMap<String, Criterion> map = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            map.put(entry.getKey(), Criterion.criterionFromJson(GsonHelper.convertToJsonObject(entry.getValue(), "criterion"), deserializationContext));
        }
        return map;
    }

    public static Map<String, Criterion> criteriaFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return friendlyByteBuf.readMap(FriendlyByteBuf::readUtf, Criterion::criterionFromNetwork);
    }

    public static void serializeToNetwork(Map<String, Criterion> map, FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeMap(map, FriendlyByteBuf::writeUtf, (friendlyByteBuf, criterion) -> criterion.serializeToNetwork((FriendlyByteBuf)friendlyByteBuf));
    }

    @Nullable
    public CriterionTriggerInstance getTrigger() {
        return this.trigger;
    }

    public JsonElement serializeToJson() {
        if (this.trigger == null) {
            throw new JsonSyntaxException("Missing trigger");
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("trigger", this.trigger.getCriterion().toString());
        JsonObject jsonObject2 = this.trigger.serializeToJson(SerializationContext.INSTANCE);
        if (jsonObject2.size() != 0) {
            jsonObject.add("conditions", jsonObject2);
        }
        return jsonObject;
    }
}

