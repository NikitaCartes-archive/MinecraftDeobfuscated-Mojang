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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ChangeDimensionTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("changed_dimension");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        ResourceKey<Level> resourceKey = jsonObject.has("from") ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(jsonObject, "from"))) : null;
        ResourceKey<Level> resourceKey2 = jsonObject.has("to") ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(jsonObject, "to"))) : null;
        return new TriggerInstance(composite, resourceKey, resourceKey2);
    }

    public void trigger(ServerPlayer serverPlayer, ResourceKey<Level> resourceKey, ResourceKey<Level> resourceKey2) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceKey, resourceKey2));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        @Nullable
        private final ResourceKey<Level> from;
        @Nullable
        private final ResourceKey<Level> to;

        public TriggerInstance(EntityPredicate.Composite composite, @Nullable ResourceKey<Level> resourceKey, @Nullable ResourceKey<Level> resourceKey2) {
            super(ID, composite);
            this.from = resourceKey;
            this.to = resourceKey2;
        }

        public static TriggerInstance changedDimension() {
            return new TriggerInstance(EntityPredicate.Composite.ANY, null, null);
        }

        public static TriggerInstance changedDimension(ResourceKey<Level> resourceKey, ResourceKey<Level> resourceKey2) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, resourceKey, resourceKey2);
        }

        public static TriggerInstance changedDimensionTo(ResourceKey<Level> resourceKey) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, null, resourceKey);
        }

        public static TriggerInstance changedDimensionFrom(ResourceKey<Level> resourceKey) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, resourceKey, null);
        }

        public boolean matches(ResourceKey<Level> resourceKey, ResourceKey<Level> resourceKey2) {
            if (this.from != null && this.from != resourceKey) {
                return false;
            }
            return this.to == null || this.to == resourceKey2;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            if (this.from != null) {
                jsonObject.addProperty("from", this.from.location().toString());
            }
            if (this.to != null) {
                jsonObject.addProperty("to", this.to.location().toString());
            }
            return jsonObject;
        }
    }
}

