package net.minecraft.world.level.storage.loot;

import com.google.gson.GsonBuilder;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class Deserializers {
	public static GsonBuilder createConditionSerializer() {
		return new GsonBuilder()
			.registerTypeAdapter(RandomValueBounds.class, new RandomValueBounds.Serializer())
			.registerTypeAdapter(BinomialDistributionGenerator.class, new BinomialDistributionGenerator.Serializer())
			.registerTypeAdapter(ConstantIntValue.class, new ConstantIntValue.Serializer())
			.registerTypeHierarchyAdapter(LootItemCondition.class, new LootItemConditions.Serializer())
			.registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer());
	}

	public static GsonBuilder createFunctionSerializer() {
		return createConditionSerializer()
			.registerTypeAdapter(IntLimiter.class, new IntLimiter.Serializer())
			.registerTypeHierarchyAdapter(LootPoolEntryContainer.class, new LootPoolEntries.Serializer())
			.registerTypeHierarchyAdapter(LootItemFunction.class, new LootItemFunctions.Serializer());
	}

	public static GsonBuilder createLootTableSerializer() {
		return createFunctionSerializer()
			.registerTypeAdapter(LootPool.class, new LootPool.Serializer())
			.registerTypeAdapter(LootTable.class, new LootTable.Serializer());
	}
}
