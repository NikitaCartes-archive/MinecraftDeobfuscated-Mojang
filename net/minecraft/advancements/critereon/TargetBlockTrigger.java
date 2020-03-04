/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("target_hit");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("signal_strength"));
        EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("projectile"));
        EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("shooter"));
        return new TriggerInstance(ints, entityPredicate, entityPredicate2);
    }

    public void trigger(ServerPlayer serverPlayer, Entity entity, Vec3 vec3, int i) {
        this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, entity, vec3, i));
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints signalStrength;
        private final EntityPredicate projectile;
        private final EntityPredicate shooter;

        public TriggerInstance(MinMaxBounds.Ints ints, EntityPredicate entityPredicate, EntityPredicate entityPredicate2) {
            super(ID);
            this.signalStrength = ints;
            this.projectile = entityPredicate;
            this.shooter = entityPredicate2;
        }

        public static TriggerInstance targetHit(MinMaxBounds.Ints ints) {
            return new TriggerInstance(ints, EntityPredicate.ANY, EntityPredicate.ANY);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("signal_strength", this.signalStrength.serializeToJson());
            jsonObject.add("projectile", this.projectile.serializeToJson());
            jsonObject.add("shooter", this.shooter.serializeToJson());
            return jsonObject;
        }

        public boolean matches(ServerPlayer serverPlayer, Entity entity, Vec3 vec3, int i) {
            if (!this.signalStrength.matches(i)) {
                return false;
            }
            if (!this.projectile.matches(serverPlayer, entity)) {
                return false;
            }
            return this.shooter.matches(serverPlayer.getLevel(), vec3, serverPlayer);
        }
    }
}

