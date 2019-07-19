package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface LootItemFunction extends LootContextUser, BiFunction<ItemStack, LootContext, ItemStack> {
	static Consumer<ItemStack> decorate(BiFunction<ItemStack, LootContext, ItemStack> biFunction, Consumer<ItemStack> consumer, LootContext lootContext) {
		return itemStack -> consumer.accept(biFunction.apply(itemStack, lootContext));
	}

	public interface Builder {
		LootItemFunction build();
	}

	public abstract static class Serializer<T extends LootItemFunction> {
		private final ResourceLocation name;
		private final Class<T> clazz;

		protected Serializer(ResourceLocation resourceLocation, Class<T> class_) {
			this.name = resourceLocation;
			this.clazz = class_;
		}

		public ResourceLocation getName() {
			return this.name;
		}

		public Class<T> getFunctionClass() {
			return this.clazz;
		}

		public abstract void serialize(JsonObject jsonObject, T lootItemFunction, JsonSerializationContext jsonSerializationContext);

		public abstract T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext);
	}
}
