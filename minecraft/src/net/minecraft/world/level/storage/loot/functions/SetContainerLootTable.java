package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable extends LootItemConditionalFunction {
	final ResourceLocation name;
	final long seed;
	final BlockEntityType<?> type;

	SetContainerLootTable(LootItemCondition[] lootItemConditions, ResourceLocation resourceLocation, long l, BlockEntityType<?> blockEntityType) {
		super(lootItemConditions);
		this.name = resourceLocation;
		this.seed = l;
		this.type = blockEntityType;
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
			CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
			if (compoundTag == null) {
				compoundTag = new CompoundTag();
			}

			compoundTag.putString("LootTable", this.name.toString());
			if (this.seed != 0L) {
				compoundTag.putLong("LootTableSeed", this.seed);
			}

			BlockItem.setBlockEntityData(itemStack, this.type, compoundTag);
			return itemStack;
		}
	}

	@Override
	public void validate(ValidationContext validationContext) {
		super.validate(validationContext);
		LootDataId<LootTable> lootDataId = new LootDataId<>(LootDataType.TABLE, this.name);
		if (validationContext.resolver().getElementOptional(lootDataId).isEmpty()) {
			validationContext.reportProblem("Missing loot table used for container: " + this.name);
		}
	}

	public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> blockEntityType, ResourceLocation resourceLocation) {
		return simpleBuilder(lootItemConditions -> new SetContainerLootTable(lootItemConditions, resourceLocation, 0L, blockEntityType));
	}

	public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> blockEntityType, ResourceLocation resourceLocation, long l) {
		return simpleBuilder(lootItemConditions -> new SetContainerLootTable(lootItemConditions, resourceLocation, l, blockEntityType));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerLootTable> {
		public void serialize(JsonObject jsonObject, SetContainerLootTable setContainerLootTable, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setContainerLootTable, jsonSerializationContext);
			jsonObject.addProperty("name", setContainerLootTable.name.toString());
			jsonObject.addProperty("type", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(setContainerLootTable.type).toString());
			if (setContainerLootTable.seed != 0L) {
				jsonObject.addProperty("seed", setContainerLootTable.seed);
			}
		}

		public SetContainerLootTable deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
			long l = GsonHelper.getAsLong(jsonObject, "seed", 0L);
			ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "type"));
			BlockEntityType<?> blockEntityType = (BlockEntityType<?>)BuiltInRegistries.BLOCK_ENTITY_TYPE
				.getOptional(resourceLocation2)
				.orElseThrow(() -> new JsonSyntaxException("Unknown block entity type id '" + resourceLocation2 + "'"));
			return new SetContainerLootTable(lootItemConditions, resourceLocation, l, blockEntityType);
		}
	}
}
