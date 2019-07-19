/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import net.minecraft.resources.ResourceLocation;

public interface CriterionTriggerInstance {
    public ResourceLocation getCriterion();

    default public JsonElement serializeToJson() {
        return JsonNull.INSTANCE;
    }
}

