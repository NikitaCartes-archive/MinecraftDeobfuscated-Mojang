/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConditionReference
implements LootItemCondition {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceLocation name;

    private ConditionReference(ResourceLocation resourceLocation) {
        this.name = resourceLocation;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.REFERENCE;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        if (validationContext.hasVisitedCondition(this.name)) {
            validationContext.reportProblem("Condition " + this.name + " is recursively called");
            return;
        }
        LootItemCondition.super.validate(validationContext);
        LootItemCondition lootItemCondition = validationContext.resolveCondition(this.name);
        if (lootItemCondition == null) {
            validationContext.reportProblem("Unknown condition table called " + this.name);
        } else {
            lootItemCondition.validate(validationContext.enterTable(".{" + this.name + "}", this.name));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean test(LootContext lootContext) {
        LootItemCondition lootItemCondition = lootContext.getCondition(this.name);
        if (lootContext.addVisitedCondition(lootItemCondition)) {
            try {
                boolean bl = lootItemCondition.test(lootContext);
                return bl;
            } finally {
                lootContext.removeVisitedCondition(lootItemCondition);
            }
        }
        LOGGER.warn("Detected infinite loop in loot tables");
        return false;
    }

    public static LootItemCondition.Builder conditionReference(ResourceLocation resourceLocation) {
        return () -> new ConditionReference(resourceLocation);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<ConditionReference> {
        @Override
        public void serialize(JsonObject jsonObject, ConditionReference conditionReference, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("name", conditionReference.name.toString());
        }

        @Override
        public ConditionReference deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
            return new ConditionReference(resourceLocation);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

