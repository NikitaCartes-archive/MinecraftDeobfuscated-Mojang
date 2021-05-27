/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

public class EffectsChangedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("effects_changed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        MobEffectsPredicate mobEffectsPredicate = MobEffectsPredicate.fromJson(jsonObject.get("effects"));
        EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "source", deserializationContext);
        return new TriggerInstance(composite, mobEffectsPredicate, composite2);
    }

    public void trigger(ServerPlayer serverPlayer, @Nullable Entity entity) {
        LootContext lootContext = entity != null ? EntityPredicate.createContext(serverPlayer, entity) : null;
        this.trigger(serverPlayer, (T triggerInstance) -> triggerInstance.matches(serverPlayer, lootContext));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final MobEffectsPredicate effects;
        private final EntityPredicate.Composite source;

        public TriggerInstance(EntityPredicate.Composite composite, MobEffectsPredicate mobEffectsPredicate, EntityPredicate.Composite composite2) {
            super(ID, composite);
            this.effects = mobEffectsPredicate;
            this.source = composite2;
        }

        public static TriggerInstance hasEffects(MobEffectsPredicate mobEffectsPredicate) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, mobEffectsPredicate, EntityPredicate.Composite.ANY);
        }

        public static TriggerInstance gotEffectsFrom(EntityPredicate entityPredicate) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, MobEffectsPredicate.ANY, EntityPredicate.Composite.wrap(entityPredicate));
        }

        public boolean matches(ServerPlayer serverPlayer, @Nullable LootContext lootContext) {
            if (!this.effects.matches(serverPlayer)) {
                return false;
            }
            return this.source == EntityPredicate.Composite.ANY || lootContext != null && this.source.matches(lootContext);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("effects", this.effects.serializeToJson());
            jsonObject.add("source", this.source.toJson(serializationContext));
            return jsonObject;
        }
    }
}

