/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.phys.Vec3;

public class LootItemEntityPropertyCondition
implements LootItemCondition {
    final EntityPredicate predicate;
    final LootContext.EntityTarget entityTarget;

    LootItemEntityPropertyCondition(EntityPredicate entityPredicate, LootContext.EntityTarget entityTarget) {
        this.predicate = entityPredicate;
        this.entityTarget = entityTarget;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_PROPERTIES;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN, this.entityTarget.getParam());
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(this.entityTarget.getParam());
        Vec3 vec3 = lootContext.getParamOrNull(LootContextParams.ORIGIN);
        return this.predicate.matches(lootContext.getLevel(), vec3, entity);
    }

    public static LootItemCondition.Builder entityPresent(LootContext.EntityTarget entityTarget) {
        return LootItemEntityPropertyCondition.hasProperties(entityTarget, EntityPredicate.Builder.entity());
    }

    public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget entityTarget, EntityPredicate.Builder builder) {
        return () -> new LootItemEntityPropertyCondition(builder.build(), entityTarget);
    }

    public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget entityTarget, EntityPredicate entityPredicate) {
        return () -> new LootItemEntityPropertyCondition(entityPredicate, entityTarget);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<LootItemEntityPropertyCondition> {
        @Override
        public void serialize(JsonObject jsonObject, LootItemEntityPropertyCondition lootItemEntityPropertyCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("predicate", lootItemEntityPropertyCondition.predicate.serializeToJson());
            jsonObject.add("entity", jsonSerializationContext.serialize((Object)lootItemEntityPropertyCondition.entityTarget));
        }

        @Override
        public LootItemEntityPropertyCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("predicate"));
            return new LootItemEntityPropertyCondition(entityPredicate, GsonHelper.getAsObject(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class));
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

