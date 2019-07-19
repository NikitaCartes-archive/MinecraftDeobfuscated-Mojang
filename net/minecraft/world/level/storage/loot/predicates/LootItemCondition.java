/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;

@FunctionalInterface
public interface LootItemCondition
extends LootContextUser,
Predicate<LootContext> {

    public static abstract class Serializer<T extends LootItemCondition> {
        private final ResourceLocation name;
        private final Class<T> clazz;

        protected Serializer(ResourceLocation resourceLocation, Class<T> class_) {
            this.name = resourceLocation;
            this.clazz = class_;
        }

        public ResourceLocation getName() {
            return this.name;
        }

        public Class<T> getPredicateClass() {
            return this.clazz;
        }

        public abstract void serialize(JsonObject var1, T var2, JsonSerializationContext var3);

        public abstract T deserialize(JsonObject var1, JsonDeserializationContext var2);
    }

    @FunctionalInterface
    public static interface Builder {
        public LootItemCondition build();

        default public Builder invert() {
            return InvertedLootItemCondition.invert(this);
        }

        default public AlternativeLootItemCondition.Builder or(Builder builder) {
            return AlternativeLootItemCondition.alternative(this, builder);
        }
    }
}

