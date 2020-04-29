/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractCriterionTriggerInstance
implements CriterionTriggerInstance {
    private final ResourceLocation criterion;
    private final EntityPredicate.Composite player;

    public AbstractCriterionTriggerInstance(ResourceLocation resourceLocation, EntityPredicate.Composite composite) {
        this.criterion = resourceLocation;
        this.player = composite;
    }

    @Override
    public ResourceLocation getCriterion() {
        return this.criterion;
    }

    protected EntityPredicate.Composite getPlayerPredicate() {
        return this.player;
    }

    @Override
    public JsonObject serializeToJson(SerializationContext serializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("player", this.player.toJson(serializationContext));
        return jsonObject;
    }

    public String toString() {
        return "AbstractCriterionInstance{criterion=" + this.criterion + '}';
    }
}

