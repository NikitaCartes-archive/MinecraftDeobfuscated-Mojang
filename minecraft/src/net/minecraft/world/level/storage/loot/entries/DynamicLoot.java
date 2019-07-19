package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DynamicLoot extends LootPoolSingletonContainer {
	public static final ResourceLocation TYPE = new ResourceLocation("dynamic");
	private final ResourceLocation name;

	private DynamicLoot(ResourceLocation resourceLocation, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
		super(i, j, lootItemConditions, lootItemFunctions);
		this.name = resourceLocation;
	}

	@Override
	public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
		lootContext.addDynamicDrops(this.name, consumer);
	}

	public static LootPoolSingletonContainer.Builder<?> dynamicEntry(ResourceLocation resourceLocation) {
		return simpleBuilder((i, j, lootItemConditions, lootItemFunctions) -> new DynamicLoot(resourceLocation, i, j, lootItemConditions, lootItemFunctions));
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<DynamicLoot> {
		public Serializer() {
			super(new ResourceLocation("dynamic"), DynamicLoot.class);
		}

		public void serialize(JsonObject jsonObject, DynamicLoot dynamicLoot, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, dynamicLoot, jsonSerializationContext);
			jsonObject.addProperty("name", dynamicLoot.name.toString());
		}

		protected DynamicLoot deserialize(
			JsonObject jsonObject,
			JsonDeserializationContext jsonDeserializationContext,
			int i,
			int j,
			LootItemCondition[] lootItemConditions,
			LootItemFunction[] lootItemFunctions
		) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
			return new DynamicLoot(resourceLocation, i, j, lootItemConditions, lootItemFunctions);
		}
	}
}
