package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootTableReference extends LootPoolSingletonContainer {
	private final ResourceLocation name;

	private LootTableReference(ResourceLocation resourceLocation, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
		super(i, j, lootItemConditions, lootItemFunctions);
		this.name = resourceLocation;
	}

	@Override
	public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
		LootTable lootTable = lootContext.getLootTables().get(this.name);
		lootTable.getRandomItemsRaw(lootContext, consumer);
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

	public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation resourceLocation) {
		return simpleBuilder((i, j, lootItemConditions, lootItemFunctions) -> new LootTableReference(resourceLocation, i, j, lootItemConditions, lootItemFunctions));
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<LootTableReference> {
		public Serializer() {
			super(new ResourceLocation("loot_table"), LootTableReference.class);
		}

		public void serialize(JsonObject jsonObject, LootTableReference lootTableReference, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, lootTableReference, jsonSerializationContext);
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
