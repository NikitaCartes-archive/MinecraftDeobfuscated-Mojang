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
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.storage.loot.LootContext;

public class CuredZombieVillagerTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "zombie", deserializationContext);
        EntityPredicate.Composite composite3 = EntityPredicate.Composite.fromJson(jsonObject, "villager", deserializationContext);
        return new TriggerInstance(composite, composite2, composite3);
    }

    public void trigger(ServerPlayer serverPlayer, Zombie zombie, Villager villager) {
        LootContext lootContext = EntityPredicate.createContext(serverPlayer, zombie);
        LootContext lootContext2 = EntityPredicate.createContext(serverPlayer, villager);
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, lootContext2));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final EntityPredicate.Composite zombie;
        private final EntityPredicate.Composite villager;

        public TriggerInstance(EntityPredicate.Composite composite, EntityPredicate.Composite composite2, EntityPredicate.Composite composite3) {
            super(ID, composite);
            this.zombie = composite2;
            this.villager = composite3;
        }

        public static TriggerInstance curedZombieVillager() {
            return new TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY);
        }

        public boolean matches(LootContext lootContext, LootContext lootContext2) {
            if (!this.zombie.matches(lootContext)) {
                return false;
            }
            return this.villager.matches(lootContext2);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("zombie", this.zombie.toJson(serializationContext));
            jsonObject.add("villager", this.villager.toJson(serializationContext));
            return jsonObject;
        }
    }
}

