/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    final ResourceLocation id;

    public KilledTrigger(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return new TriggerInstance(this.id, composite, EntityPredicate.Composite.fromJson(jsonObject, "entity", deserializationContext), DamageSourcePredicate.fromJson(jsonObject.get("killing_blow")));
    }

    public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
        LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext, damageSource));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final EntityPredicate.Composite entityPredicate;
        private final DamageSourcePredicate killingBlow;

        public TriggerInstance(ResourceLocation resourceLocation, EntityPredicate.Composite composite, EntityPredicate.Composite composite2, DamageSourcePredicate damageSourcePredicate) {
            super(resourceLocation, composite);
            this.entityPredicate = composite2;
            this.killingBlow = damageSourcePredicate;
        }

        public static TriggerInstance playerKilledEntity(EntityPredicate entityPredicate) {
            return new TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(entityPredicate), DamageSourcePredicate.ANY);
        }

        public static TriggerInstance playerKilledEntity(EntityPredicate.Builder builder) {
            return new TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(builder.build()), DamageSourcePredicate.ANY);
        }

        public static TriggerInstance playerKilledEntity() {
            return new TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, DamageSourcePredicate.ANY);
        }

        public static TriggerInstance playerKilledEntity(EntityPredicate entityPredicate, DamageSourcePredicate damageSourcePredicate) {
            return new TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(entityPredicate), damageSourcePredicate);
        }

        public static TriggerInstance playerKilledEntity(EntityPredicate.Builder builder, DamageSourcePredicate damageSourcePredicate) {
            return new TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(builder.build()), damageSourcePredicate);
        }

        public static TriggerInstance playerKilledEntity(EntityPredicate entityPredicate, DamageSourcePredicate.Builder builder) {
            return new TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(entityPredicate), builder.build());
        }

        public static TriggerInstance playerKilledEntity(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
            return new TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(builder.build()), builder2.build());
        }

        public static TriggerInstance playerKilledEntityNearSculkCatalyst() {
            return new TriggerInstance(CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, DamageSourcePredicate.ANY);
        }

        public static TriggerInstance entityKilledPlayer(EntityPredicate entityPredicate) {
            return new TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(entityPredicate), DamageSourcePredicate.ANY);
        }

        public static TriggerInstance entityKilledPlayer(EntityPredicate.Builder builder) {
            return new TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(builder.build()), DamageSourcePredicate.ANY);
        }

        public static TriggerInstance entityKilledPlayer() {
            return new TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, DamageSourcePredicate.ANY);
        }

        public static TriggerInstance entityKilledPlayer(EntityPredicate entityPredicate, DamageSourcePredicate damageSourcePredicate) {
            return new TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(entityPredicate), damageSourcePredicate);
        }

        public static TriggerInstance entityKilledPlayer(EntityPredicate.Builder builder, DamageSourcePredicate damageSourcePredicate) {
            return new TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(builder.build()), damageSourcePredicate);
        }

        public static TriggerInstance entityKilledPlayer(EntityPredicate entityPredicate, DamageSourcePredicate.Builder builder) {
            return new TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(entityPredicate), builder.build());
        }

        public static TriggerInstance entityKilledPlayer(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
            return new TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(builder.build()), builder2.build());
        }

        public boolean matches(ServerPlayer serverPlayer, LootContext lootContext, DamageSource damageSource) {
            if (!this.killingBlow.matches(serverPlayer, damageSource)) {
                return false;
            }
            return this.entityPredicate.matches(lootContext);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("entity", this.entityPredicate.toJson(serializationContext));
            jsonObject.add("killing_blow", this.killingBlow.serializeToJson());
            return jsonObject;
        }
    }
}

