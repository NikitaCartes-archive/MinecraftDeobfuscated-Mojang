package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
	final LootPoolEntryContainer[] entries;
	final LootItemCondition[] conditions;
	private final Predicate<LootContext> compositeCondition;
	final LootItemFunction[] functions;
	private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
	final NumberProvider rolls;
	final NumberProvider bonusRolls;

	LootPool(
		LootPoolEntryContainer[] lootPoolEntryContainers,
		LootItemCondition[] lootItemConditions,
		LootItemFunction[] lootItemFunctions,
		NumberProvider numberProvider,
		NumberProvider numberProvider2
	) {
		this.entries = lootPoolEntryContainers;
		this.conditions = lootItemConditions;
		this.compositeCondition = LootItemConditions.andConditions(lootItemConditions);
		this.functions = lootItemFunctions;
		this.compositeFunction = LootItemFunctions.compose(lootItemFunctions);
		this.rolls = numberProvider;
		this.bonusRolls = numberProvider2;
	}

	private void addRandomItem(Consumer<ItemStack> consumer, LootContext lootContext) {
		Random random = lootContext.getRandom();
		List<LootPoolEntry> list = Lists.<LootPoolEntry>newArrayList();
		MutableInt mutableInt = new MutableInt();

		for (LootPoolEntryContainer lootPoolEntryContainer : this.entries) {
			lootPoolEntryContainer.expand(lootContext, lootPoolEntryx -> {
				int i = lootPoolEntryx.getWeight(lootContext.getLuck());
				if (i > 0) {
					list.add(lootPoolEntryx);
					mutableInt.add(i);
				}
			});
		}

		int i = list.size();
		if (mutableInt.intValue() != 0 && i != 0) {
			if (i == 1) {
				((LootPoolEntry)list.get(0)).createItemStack(consumer, lootContext);
			} else {
				int j = random.nextInt(mutableInt.intValue());

				for (LootPoolEntry lootPoolEntry : list) {
					j -= lootPoolEntry.getWeight(lootContext.getLuck());
					if (j < 0) {
						lootPoolEntry.createItemStack(consumer, lootContext);
						return;
					}
				}
			}
		}
	}

	public void addRandomItems(Consumer<ItemStack> consumer, LootContext lootContext) {
		if (this.compositeCondition.test(lootContext)) {
			Consumer<ItemStack> consumer2 = LootItemFunction.decorate(this.compositeFunction, consumer, lootContext);
			int i = this.rolls.getInt(lootContext) + Mth.floor(this.bonusRolls.getFloat(lootContext) * lootContext.getLuck());

			for (int j = 0; j < i; j++) {
				this.addRandomItem(consumer2, lootContext);
			}
		}
	}

	public void validate(ValidationContext validationContext) {
		for (int i = 0; i < this.conditions.length; i++) {
			this.conditions[i].validate(validationContext.forChild(".condition[" + i + "]"));
		}

		for (int i = 0; i < this.functions.length; i++) {
			this.functions[i].validate(validationContext.forChild(".functions[" + i + "]"));
		}

		for (int i = 0; i < this.entries.length; i++) {
			this.entries[i].validate(validationContext.forChild(".entries[" + i + "]"));
		}

		this.rolls.validate(validationContext.forChild(".rolls"));
		this.bonusRolls.validate(validationContext.forChild(".bonusRolls"));
	}

	public static LootPool.Builder lootPool() {
		return new LootPool.Builder();
	}

	public static class Builder implements FunctionUserBuilder<LootPool.Builder>, ConditionUserBuilder<LootPool.Builder> {
		private final List<LootPoolEntryContainer> entries = Lists.<LootPoolEntryContainer>newArrayList();
		private final List<LootItemCondition> conditions = Lists.<LootItemCondition>newArrayList();
		private final List<LootItemFunction> functions = Lists.<LootItemFunction>newArrayList();
		private NumberProvider rolls = ConstantValue.exactly(1.0F);
		private NumberProvider bonusRolls = ConstantValue.exactly(0.0F);

		public LootPool.Builder setRolls(NumberProvider numberProvider) {
			this.rolls = numberProvider;
			return this;
		}

		public LootPool.Builder unwrap() {
			return this;
		}

		public LootPool.Builder setBonusRolls(NumberProvider numberProvider) {
			this.bonusRolls = numberProvider;
			return this;
		}

		public LootPool.Builder add(LootPoolEntryContainer.Builder<?> builder) {
			this.entries.add(builder.build());
			return this;
		}

		public LootPool.Builder when(LootItemCondition.Builder builder) {
			this.conditions.add(builder.build());
			return this;
		}

		public LootPool.Builder apply(LootItemFunction.Builder builder) {
			this.functions.add(builder.build());
			return this;
		}

		public LootPool build() {
			if (this.rolls == null) {
				throw new IllegalArgumentException("Rolls not set");
			} else {
				return new LootPool(
					(LootPoolEntryContainer[])this.entries.toArray(new LootPoolEntryContainer[0]),
					(LootItemCondition[])this.conditions.toArray(new LootItemCondition[0]),
					(LootItemFunction[])this.functions.toArray(new LootItemFunction[0]),
					this.rolls,
					this.bonusRolls
				);
			}
		}
	}

	public static class Serializer implements JsonDeserializer<LootPool>, JsonSerializer<LootPool> {
		public LootPool deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "loot pool");
			LootPoolEntryContainer[] lootPoolEntryContainers = GsonHelper.getAsObject(jsonObject, "entries", jsonDeserializationContext, LootPoolEntryContainer[].class);
			LootItemCondition[] lootItemConditions = GsonHelper.getAsObject(
				jsonObject, "conditions", new LootItemCondition[0], jsonDeserializationContext, LootItemCondition[].class
			);
			LootItemFunction[] lootItemFunctions = GsonHelper.getAsObject(
				jsonObject, "functions", new LootItemFunction[0], jsonDeserializationContext, LootItemFunction[].class
			);
			NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "rolls", jsonDeserializationContext, NumberProvider.class);
			NumberProvider numberProvider2 = GsonHelper.getAsObject(
				jsonObject, "bonus_rolls", ConstantValue.exactly(0.0F), jsonDeserializationContext, NumberProvider.class
			);
			return new LootPool(lootPoolEntryContainers, lootItemConditions, lootItemFunctions, numberProvider, numberProvider2);
		}

		public JsonElement serialize(LootPool lootPool, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("rolls", jsonSerializationContext.serialize(lootPool.rolls));
			jsonObject.add("bonus_rolls", jsonSerializationContext.serialize(lootPool.bonusRolls));
			jsonObject.add("entries", jsonSerializationContext.serialize(lootPool.entries));
			if (!ArrayUtils.isEmpty((Object[])lootPool.conditions)) {
				jsonObject.add("conditions", jsonSerializationContext.serialize(lootPool.conditions));
			}

			if (!ArrayUtils.isEmpty((Object[])lootPool.functions)) {
				jsonObject.add("functions", jsonSerializationContext.serialize(lootPool.functions));
			}

			return jsonObject;
		}
	}
}
