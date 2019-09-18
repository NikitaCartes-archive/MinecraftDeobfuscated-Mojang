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
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.animal.Animal;
import org.jetbrains.annotations.Nullable;

public class BredAnimalsTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("bred_animals");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("parent"));
        EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("partner"));
        EntityPredicate entityPredicate3 = EntityPredicate.fromJson(jsonObject.get("child"));
        return new TriggerInstance(entityPredicate, entityPredicate2, entityPredicate3);
    }

    public void trigger(ServerPlayer serverPlayer, Animal animal, @Nullable Animal animal2, @Nullable AgableMob agableMob) {
        this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, animal, animal2, agableMob));
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final EntityPredicate parent;
        private final EntityPredicate partner;
        private final EntityPredicate child;

        public TriggerInstance(EntityPredicate entityPredicate, EntityPredicate entityPredicate2, EntityPredicate entityPredicate3) {
            super(ID);
            this.parent = entityPredicate;
            this.partner = entityPredicate2;
            this.child = entityPredicate3;
        }

        public static TriggerInstance bredAnimals() {
            return new TriggerInstance(EntityPredicate.ANY, EntityPredicate.ANY, EntityPredicate.ANY);
        }

        public static TriggerInstance bredAnimals(EntityPredicate.Builder builder) {
            return new TriggerInstance(builder.build(), EntityPredicate.ANY, EntityPredicate.ANY);
        }

        public boolean matches(ServerPlayer serverPlayer, Animal animal, @Nullable Animal animal2, @Nullable AgableMob agableMob) {
            if (!this.child.matches(serverPlayer, agableMob)) {
                return false;
            }
            return this.parent.matches(serverPlayer, animal) && this.partner.matches(serverPlayer, animal2) || this.parent.matches(serverPlayer, animal2) && this.partner.matches(serverPlayer, animal);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("parent", this.parent.serializeToJson());
            jsonObject.add("partner", this.partner.serializeToJson());
            jsonObject.add("child", this.child.serializeToJson());
            return jsonObject;
        }
    }
}

