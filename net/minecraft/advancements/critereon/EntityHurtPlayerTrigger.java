/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class EntityHurtPlayerTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("entity_hurt_player");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        DamagePredicate damagePredicate = DamagePredicate.fromJson(jsonObject.get("damage"));
        return new TriggerInstance(composite, damagePredicate);
    }

    public void trigger(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, damageSource, f, g, bl));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final DamagePredicate damage;

        public TriggerInstance(EntityPredicate.Composite composite, DamagePredicate damagePredicate) {
            super(ID, composite);
            this.damage = damagePredicate;
        }

        public static TriggerInstance entityHurtPlayer() {
            return new TriggerInstance(EntityPredicate.Composite.ANY, DamagePredicate.ANY);
        }

        public static TriggerInstance entityHurtPlayer(DamagePredicate damagePredicate) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, damagePredicate);
        }

        public static TriggerInstance entityHurtPlayer(DamagePredicate.Builder builder) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, builder.build());
        }

        public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
            return this.damage.matches(serverPlayer, damageSource, f, g, bl);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("damage", this.damage.serializeToJson());
            return jsonObject;
        }
    }
}

