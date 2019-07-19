package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
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
	public void validate(
		LootTableProblemCollector lootTableProblemCollector,
		Function<ResourceLocation, LootTable> function,
		Set<ResourceLocation> set,
		LootContextParamSet lootContextParamSet
	) {
		if (set.contains(this.name)) {
			lootTableProblemCollector.reportProblem("Table " + this.name + " is recursively called");
		} else {
			super.validate(lootTableProblemCollector, function, set, lootContextParamSet);
			LootTable lootTable = (LootTable)function.apply(this.name);
			if (lootTable == null) {
				lootTableProblemCollector.reportProblem("Unknown loot table called " + this.name);
			} else {
				Set<ResourceLocation> set2 = ImmutableSet.<ResourceLocation>builder().addAll(set).add(this.name).build();
				lootTable.validate(lootTableProblemCollector.forChild("->{" + this.name + "}"), function, set2, lootContextParamSet);
			}
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerLootTable> {
		protected Serializer() {
			super(new ResourceLocation("set_loot_table"), SetContainerLootTable.class);
		}

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
