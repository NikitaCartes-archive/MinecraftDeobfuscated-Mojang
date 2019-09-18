package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

@FunctionalInterface
public interface LootItemCondition extends LootContextUser, Predicate<LootContext> {
	LootItemCondition FALSE = lootContext -> false;

	@FunctionalInterface
	public interface Builder {
		LootItemCondition build();

		default LootItemCondition.Builder invert() {
			return InvertedLootItemCondition.invert(this);
		}

		default AlternativeLootItemCondition.Builder or(LootItemCondition.Builder builder) {
			return AlternativeLootItemCondition.alternative(this, builder);
		}
	}

	public abstract static class Serializer<T extends LootItemCondition> {
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

		public abstract void serialize(JsonObject jsonObject, T lootItemCondition, JsonSerializationContext jsonSerializationContext);

		public abstract T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext);
	}
}
