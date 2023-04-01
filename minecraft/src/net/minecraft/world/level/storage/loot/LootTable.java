package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.Rules;
import net.minecraft.voting.rules.actual.RuleFeatureToggles;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public class LootTable {
	static final Logger LOGGER = LogUtils.getLogger();
	public static final LootTable EMPTY = new LootTable(LootContextParamSets.EMPTY, new LootPool[0], new LootItemFunction[0]);
	public static final LootContextParamSet DEFAULT_PARAM_SET = LootContextParamSets.ALL_PARAMS;
	final LootContextParamSet paramSet;
	final LootPool[] pools;
	final LootItemFunction[] functions;
	private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

	LootTable(LootContextParamSet lootContextParamSet, LootPool[] lootPools, LootItemFunction[] lootItemFunctions) {
		this.paramSet = lootContextParamSet;
		this.pools = lootPools;
		this.functions = lootItemFunctions;
		this.compositeFunction = LootItemFunctions.compose(lootItemFunctions);
	}

	public static Consumer<ItemStack> createStackSplitter(LootContext lootContext, Consumer<ItemStack> consumer) {
		return itemStack -> {
			itemStack = Rules.DOUBLE_OR_HALF_ITEM_DROPS.adjustItemStack(Rules.REPLACE_LOOT_DROPS.adjustItemStack(lootContext.getLevel().registryAccess(), itemStack));
			if (itemStack.isItemEnabled(lootContext.getLevel().enabledFeatures()) && RuleFeatureToggles.isEnabled(itemStack)) {
				if (itemStack.getCount() < itemStack.getMaxStackSize()) {
					consumer.accept(itemStack);
				} else {
					int i = itemStack.getCount();

					while (i > 0) {
						ItemStack itemStack2 = itemStack.copy();
						itemStack2.setCount(Math.min(itemStack.getMaxStackSize(), i));
						i -= itemStack2.getCount();
						consumer.accept(itemStack2);
					}
				}
			}
		};
	}

	public void getRandomItemsRaw(LootContext lootContext, Consumer<ItemStack> consumer) {
		if (lootContext.addVisitedTable(this)) {
			Consumer<ItemStack> consumer2 = LootItemFunction.decorate(this.compositeFunction, consumer, lootContext);

			for (LootPool lootPool : this.pools) {
				lootPool.addRandomItems(consumer2, lootContext);
			}

			lootContext.removeVisitedTable(this);
		} else {
			LOGGER.warn("Detected infinite loop in loot tables");
		}
	}

	public void getRandomItems(LootContext lootContext, Consumer<ItemStack> consumer) {
		this.getRandomItemsRaw(lootContext, createStackSplitter(lootContext, consumer));
	}

	public ObjectArrayList<ItemStack> getRandomItems(LootContext lootContext) {
		ObjectArrayList<ItemStack> objectArrayList = new ObjectArrayList<>();
		this.getRandomItems(lootContext, objectArrayList::add);
		return objectArrayList;
	}

	public LootContextParamSet getParamSet() {
		return this.paramSet;
	}

	public void validate(ValidationContext validationContext) {
		for (int i = 0; i < this.pools.length; i++) {
			this.pools[i].validate(validationContext.forChild(".pools[" + i + "]"));
		}

		for (int i = 0; i < this.functions.length; i++) {
			this.functions[i].validate(validationContext.forChild(".functions[" + i + "]"));
		}
	}

	public void fill(Container container, LootContext lootContext) {
		ObjectArrayList<ItemStack> objectArrayList = this.getRandomItems(lootContext);
		RandomSource randomSource = lootContext.getRandom();
		List<Integer> list = this.getAvailableSlots(container, randomSource);
		this.shuffleAndSplitItems(objectArrayList, list.size(), randomSource);

		for (ItemStack itemStack : objectArrayList) {
			if (list.isEmpty()) {
				LOGGER.warn("Tried to over-fill a container");
				return;
			}

			if (itemStack.isEmpty()) {
				container.setItem((Integer)list.remove(list.size() - 1), ItemStack.EMPTY);
			} else {
				container.setItem((Integer)list.remove(list.size() - 1), itemStack);
			}
		}
	}

	private void shuffleAndSplitItems(ObjectArrayList<ItemStack> objectArrayList, int i, RandomSource randomSource) {
		List<ItemStack> list = Lists.<ItemStack>newArrayList();
		Iterator<ItemStack> iterator = objectArrayList.iterator();

		while (iterator.hasNext()) {
			ItemStack itemStack = (ItemStack)iterator.next();
			if (itemStack.isEmpty()) {
				iterator.remove();
			} else if (itemStack.getCount() > 1) {
				list.add(itemStack);
				iterator.remove();
			}
		}

		while (i - objectArrayList.size() - list.size() > 0 && !list.isEmpty()) {
			ItemStack itemStack2 = (ItemStack)list.remove(Mth.nextInt(randomSource, 0, list.size() - 1));
			int j = Mth.nextInt(randomSource, 1, itemStack2.getCount() / 2);
			ItemStack itemStack3 = itemStack2.split(j);
			if (itemStack2.getCount() > 1 && randomSource.nextBoolean()) {
				list.add(itemStack2);
			} else {
				objectArrayList.add(itemStack2);
			}

			if (itemStack3.getCount() > 1 && randomSource.nextBoolean()) {
				list.add(itemStack3);
			} else {
				objectArrayList.add(itemStack3);
			}
		}

		objectArrayList.addAll(list);
		Util.shuffle(objectArrayList, randomSource);
	}

	private List<Integer> getAvailableSlots(Container container, RandomSource randomSource) {
		ObjectArrayList<Integer> objectArrayList = new ObjectArrayList<>();

		for (int i = 0; i < container.getContainerSize(); i++) {
			if (container.getItem(i).isEmpty()) {
				objectArrayList.add(i);
			}
		}

		Util.shuffle(objectArrayList, randomSource);
		return objectArrayList;
	}

	public static LootTable.Builder lootTable() {
		return new LootTable.Builder();
	}

	public static class Builder implements FunctionUserBuilder<LootTable.Builder> {
		private final List<LootPool> pools = Lists.<LootPool>newArrayList();
		private final List<LootItemFunction> functions = Lists.<LootItemFunction>newArrayList();
		private LootContextParamSet paramSet = LootTable.DEFAULT_PARAM_SET;

		public LootTable.Builder withPool(LootPool.Builder builder) {
			this.pools.add(builder.build());
			return this;
		}

		public LootTable.Builder setParamSet(LootContextParamSet lootContextParamSet) {
			this.paramSet = lootContextParamSet;
			return this;
		}

		public LootTable.Builder apply(LootItemFunction.Builder builder) {
			this.functions.add(builder.build());
			return this;
		}

		public LootTable.Builder unwrap() {
			return this;
		}

		public LootTable build() {
			return new LootTable(this.paramSet, (LootPool[])this.pools.toArray(new LootPool[0]), (LootItemFunction[])this.functions.toArray(new LootItemFunction[0]));
		}
	}

	public static class Serializer implements JsonDeserializer<LootTable>, JsonSerializer<LootTable> {
		public LootTable deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "loot table");
			LootPool[] lootPools = GsonHelper.getAsObject(jsonObject, "pools", new LootPool[0], jsonDeserializationContext, LootPool[].class);
			LootContextParamSet lootContextParamSet = null;
			if (jsonObject.has("type")) {
				String string = GsonHelper.getAsString(jsonObject, "type");
				lootContextParamSet = LootContextParamSets.get(new ResourceLocation(string));
			}

			LootItemFunction[] lootItemFunctions = GsonHelper.getAsObject(
				jsonObject, "functions", new LootItemFunction[0], jsonDeserializationContext, LootItemFunction[].class
			);
			return new LootTable(lootContextParamSet != null ? lootContextParamSet : LootContextParamSets.ALL_PARAMS, lootPools, lootItemFunctions);
		}

		public JsonElement serialize(LootTable lootTable, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			if (lootTable.paramSet != LootTable.DEFAULT_PARAM_SET) {
				ResourceLocation resourceLocation = LootContextParamSets.getKey(lootTable.paramSet);
				if (resourceLocation != null) {
					jsonObject.addProperty("type", resourceLocation.toString());
				} else {
					LootTable.LOGGER.warn("Failed to find id for param set {}", lootTable.paramSet);
				}
			}

			if (lootTable.pools.length > 0) {
				jsonObject.add("pools", jsonSerializationContext.serialize(lootTable.pools));
			}

			if (!ArrayUtils.isEmpty((Object[])lootTable.functions)) {
				jsonObject.add("functions", jsonSerializationContext.serialize(lootTable.functions));
			}

			return jsonObject;
		}
	}
}
