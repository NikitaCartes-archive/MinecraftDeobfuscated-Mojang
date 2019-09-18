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
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;

public class CuredZombieVillagerTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("zombie"));
        EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("villager"));
        return new TriggerInstance(entityPredicate, entityPredicate2);
    }

    public void trigger(ServerPlayer serverPlayer, Zombie zombie, Villager villager) {
        this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, zombie, villager));
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final EntityPredicate zombie;
        private final EntityPredicate villager;

        public TriggerInstance(EntityPredicate entityPredicate, EntityPredicate entityPredicate2) {
            super(ID);
            this.zombie = entityPredicate;
            this.villager = entityPredicate2;
        }

        public static TriggerInstance curedZombieVillager() {
            return new TriggerInstance(EntityPredicate.ANY, EntityPredicate.ANY);
        }

        public boolean matches(ServerPlayer serverPlayer, Zombie zombie, Villager villager) {
            if (!this.zombie.matches(serverPlayer, zombie)) {
                return false;
            }
            return this.villager.matches(serverPlayer, villager);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("zombie", this.zombie.serializeToJson());
            jsonObject.add("villager", this.villager.serializeToJson());
            return jsonObject;
        }
    }
}

