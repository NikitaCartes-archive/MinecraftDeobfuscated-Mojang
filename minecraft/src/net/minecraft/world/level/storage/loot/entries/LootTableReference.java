package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootTableReference extends LootPoolSingletonContainer {
	private final ResourceLocation name;

	private LootTableReference(ResourceLocation resourceLocation, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
		super(i, j, lootItemConditions, lootItemFunctions);
		this.name = resourceLocation;
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.REFERENCE;
	}

	@Override
	public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
		LootTable lootTable = lootContext.getLootTable(this.name);
		lootTable.getRandomItemsRaw(lootContext, consumer);
	}

	@Override
	public void validate(ValidationContext validationContext) {
		if (validationContext.hasVisitedTable(this.name)) {
			validationContext.reportProblem("Table " + this.name + " is recursively called");
		} else {
			super.validate(validationContext);
			LootTable lootTable = validationContext.resolveLootTable(this.name);
			if (lootTable == null) {
				validationContext.reportProblem("Unknown loot table called " + this.name);
			} else {
				lootTable.validate(validationContext.enterTable("->{" + this.name + "}", this.name));
			}
		}
	}

	public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation resourceLocation) {
		return simpleBuilder((i, j, lootItemConditions, lootItemFunctions) -> new LootTableReference(resourceLocation, i, j, lootItemConditions, lootItemFunctions));
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<LootTableReference> {
		public void serializeCustom(JsonObject jsonObject, LootTableReference lootTableReference, JsonSerializationContext jsonSerializationContext) {
			super.serializeCustom(jsonObject, lootTableReference, jsonSerializationContext);
			jsonObject.addProperty("name", lootTableReference.name.toString());
		}

		protected LootTableReference deserialize(
			JsonObject jsonObject,
			JsonDeserializationContext jsonDeserializationContext,
			int i,
			int j,
			LootItemCondition[] lootItemConditions,
			LootItemFunction[] lootItemFunctions
		) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
			return new LootTableReference(resourceLocation, i, j, lootItemConditions, lootItemFunctions);
		}
	}
}
