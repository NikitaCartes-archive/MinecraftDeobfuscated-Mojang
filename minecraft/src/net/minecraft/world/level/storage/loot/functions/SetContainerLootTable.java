package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable extends LootItemConditionalFunction {
	private final ResourceLocation name;
	private final long seed;

	private SetContainerLootTable(LootItemCondition[] lootItemConditions, ResourceLocation resourceLocation, long l) {
		super(lootItemConditions);
		this.name = resourceLocation;
		this.seed = l;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_LOOT_TABLE;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.isEmpty()) {
			return itemStack;
		} else {
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.putString("LootTable", this.name.toString());
			if (this.seed != 0L) {
				compoundTag.putLong("LootTableSeed", this.seed);
			}

			itemStack.getOrCreateTag().put("BlockEntityTag", compoundTag);
			return itemStack;
		}
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

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerLootTable> {
		public void serialize(JsonObject jsonObject, SetContainerLootTable setContainerLootTable, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setContainerLootTable, jsonSerializationContext);
			jsonObject.addProperty("name", setContainerLootTable.name.toString());
			if (setContainerLootTable.seed != 0L) {
				jsonObject.addProperty("seed", setContainerLootTable.seed);
			}
		}

		public SetContainerLootTable deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
			long l = GsonHelper.getAsLong(jsonObject, "seed", 0L);
			return new SetContainerLootTable(lootItemConditions, resourceLocation, l);
		}
	}
}
