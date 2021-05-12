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
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

public class BredAnimalsTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("bred_animals");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "parent", deserializationContext);
        EntityPredicate.Composite composite3 = EntityPredicate.Composite.fromJson(jsonObject, "partner", deserializationContext);
        EntityPredicate.Composite composite4 = EntityPredicate.Composite.fromJson(jsonObject, "child", deserializationContext);
        return new TriggerInstance(composite, composite2, composite3, composite4);
    }

    public void trigger(ServerPlayer serverPlayer, Animal animal, Animal animal2, @Nullable AgeableMob ageableMob) {
        LootContext lootContext = EntityPredicate.createContext(serverPlayer, animal);
        LootContext lootContext2 = EntityPredicate.createContext(serverPlayer, animal2);
        LootContext lootContext3 = ageableMob != null ? EntityPredicate.createContext(serverPlayer, ageableMob) : null;
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, lootContext2, lootContext3));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final EntityPredicate.Composite parent;
        private final EntityPredicate.Composite partner;
        private final EntityPredicate.Composite child;

        public TriggerInstance(EntityPredicate.Composite composite, EntityPredicate.Composite composite2, EntityPredicate.Composite composite3, EntityPredicate.Composite composite4) {
            super(ID, composite);
            this.parent = composite2;
            this.partner = composite3;
            this.child = composite4;
        }

        public static TriggerInstance bredAnimals() {
            return new TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY);
        }

        public static TriggerInstance bredAnimals(EntityPredicate.Builder builder) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(builder.build()));
        }

        public static TriggerInstance bredAnimals(EntityPredicate entityPredicate, EntityPredicate entityPredicate2, EntityPredicate entityPredicate3) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(entityPredicate), EntityPredicate.Composite.wrap(entityPredicate2), EntityPredicate.Composite.wrap(entityPredicate3));
        }

        public boolean matches(LootContext lootContext, LootContext lootContext2, @Nullable LootContext lootContext3) {
            if (!(this.child == EntityPredicate.Composite.ANY || lootContext3 != null && this.child.matches(lootContext3))) {
                return false;
            }
            return this.parent.matches(lootContext) && this.partner.matches(lootContext2) || this.parent.matches(lootContext2) && this.partner.matches(lootContext);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("parent", this.parent.toJson(serializationContext));
            jsonObject.add("partner", this.partner.toJson(serializationContext));
            jsonObject.add("child", this.child.toJson(serializationContext));
            return jsonObject;
        }
    }
}

