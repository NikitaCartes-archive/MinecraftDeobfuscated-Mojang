/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public class ChangeDimensionTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("changed_dimension");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        DimensionType dimensionType = jsonObject.has("from") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(jsonObject, "from"))) : null;
        DimensionType dimensionType2 = jsonObject.has("to") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(jsonObject, "to"))) : null;
        return new TriggerInstance(composite, dimensionType, dimensionType2);
    }

    public void trigger(ServerPlayer serverPlayer, DimensionType dimensionType, DimensionType dimensionType2) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(dimensionType, dimensionType2));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        @Nullable
        private final DimensionType from;
        @Nullable
        private final DimensionType to;

        public TriggerInstance(EntityPredicate.Composite composite, @Nullable DimensionType dimensionType, @Nullable DimensionType dimensionType2) {
            super(ID, composite);
            this.from = dimensionType;
            this.to = dimensionType2;
        }

        public static TriggerInstance changedDimensionTo(DimensionType dimensionType) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, null, dimensionType);
        }

        public boolean matches(DimensionType dimensionType, DimensionType dimensionType2) {
            if (this.from != null && this.from != dimensionType) {
                return false;
            }
            return this.to == null || this.to == dimensionType2;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            if (this.from != null) {
                jsonObject.addProperty("from", DimensionType.getName(this.from).toString());
            }
            if (this.to != null) {
                jsonObject.addProperty("to", DimensionType.getName(this.to).toString());
            }
            return jsonObject;
        }
    }
}

