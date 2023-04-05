package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootTableReference extends LootPoolSingletonContainer {
	final ResourceLocation name;

	LootTableReference(ResourceLocation resourceLocation, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
		super(i, j, lootItemConditions, lootItemFunctions);
		this.name = resourceLocation;
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.REFERENCE;
	}

	@Override
	public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
		LootTable lootTable = lootContext.getResolver().getLootTable(this.name);
		lootTable.getRandomItemsRaw(lootContext, consumer);
	}

	@Override
	public void validate(ValidationContext validationContext) {
		LootDataId<LootTable> lootDataId = new LootDataId<>(LootDataType.TABLE, this.name);
		if (validationContext.hasVisitedElement(lootDataId)) {
			validationContext.reportProblem("Table " + this.name + " is recursively called");
		} else {
			super.validate(validationContext);
			validationContext.resolver()
				.getElementOptional(lootDataId)
				.ifPresentOrElse(
					lootTable -> lootTable.validate(validationContext.enterElement("->{" + this.name + "}", lootDataId)),
					() -> validationContext.reportProblem("Unknown loot table called " + this.name)
				);
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
