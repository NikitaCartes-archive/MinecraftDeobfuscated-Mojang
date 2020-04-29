/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public interface CriterionTriggerInstance {
    public ResourceLocation getCriterion();

    public JsonObject serializeToJson(SerializationContext var1);
}

