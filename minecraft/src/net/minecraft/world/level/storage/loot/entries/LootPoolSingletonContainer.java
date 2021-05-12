package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootPoolSingletonContainer extends LootPoolEntryContainer {
	public static final int DEFAULT_WEIGHT = 1;
	public static final int DEFAULT_QUALITY = 0;
	protected final int weight;
	protected final int quality;
	protected final LootItemFunction[] functions;
	final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
	private final LootPoolEntry entry = new LootPoolSingletonContainer.EntryBase() {
		@Override
		public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
			LootPoolSingletonContainer.this.createItemStack(
				LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, consumer, lootContext), lootContext
			);
		}
	};

	protected LootPoolSingletonContainer(int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
		super(lootItemConditions);
		this.weight = i;
		this.quality = j;
		this.functions = lootItemFunctions;
		this.compositeFunction = LootItemFunctions.compose(lootItemFunctions);
	}

	@Override
	public void validate(ValidationContext validationContext) {
		super.validate(validationContext);

		for (int i = 0; i < this.functions.length; i++) {
			this.functions[i].validate(validationContext.forChild(".functions[" + i + "]"));
		}
	}

	protected abstract void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext);

	@Override
	public boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
		if (this.canRun(lootContext)) {
			consumer.accept(this.entry);
			return true;
		} else {
			return false;
		}
	}

	public static LootPoolSingletonContainer.Builder<?> simpleBuilder(LootPoolSingletonContainer.EntryConstructor entryConstructor) {
		return new LootPoolSingletonContainer.DummyBuilder(entryConstructor);
	}

	public abstract static class Builder<T extends LootPoolSingletonContainer.Builder<T>>
		extends LootPoolEntryContainer.Builder<T>
		implements FunctionUserBuilder<T> {
		protected int weight = 1;
		protected int quality = 0;
		private final List<LootItemFunction> functions = Lists.<LootItemFunction>newArrayList();

		public T apply(LootItemFunction.Builder builder) {
			this.functions.add(builder.build());
			return this.getThis();
		}

		protected LootItemFunction[] getFunctions() {
			return (LootItemFunction[])this.functions.toArray(new LootItemFunction[0]);
		}

		public T setWeight(int i) {
			this.weight = i;
			return this.getThis();
		}

		public T setQuality(int i) {
			this.quality = i;
			return this.getThis();
		}
	}

	static class DummyBuilder extends LootPoolSingletonContainer.Builder<LootPoolSingletonContainer.DummyBuilder> {
		private final LootPoolSingletonContainer.EntryConstructor constructor;

		public DummyBuilder(LootPoolSingletonContainer.EntryConstructor entryConstructor) {
			this.constructor = entryConstructor;
		}

		protected LootPoolSingletonContainer.DummyBuilder getThis() {
			return this;
		}

		@Override
		public LootPoolEntryContainer build() {
			return this.constructor.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
		}
	}

	protected abstract class EntryBase implements LootPoolEntry {
		@Override
		public int getWeight(float f) {
			return Math.max(Mth.floor((float)LootPoolSingletonContainer.this.weight + (float)LootPoolSingletonContainer.this.quality * f), 0);
		}
	}

	@FunctionalInterface
	protected interface EntryConstructor {
		LootPoolSingletonContainer build(int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions);
	}

	public abstract static class Serializer<T extends LootPoolSingletonContainer> extends LootPoolEntryContainer.Serializer<T> {
		public void serializeCustom(JsonObject jsonObject, T lootPoolSingletonContainer, JsonSerializationContext jsonSerializationContext) {
			if (lootPoolSingletonContainer.weight != 1) {
				jsonObject.addProperty("weight", lootPoolSingletonContainer.weight);
			}

			if (lootPoolSingletonContainer.quality != 0) {
				jsonObject.addProperty("quality", lootPoolSingletonContainer.quality);
			}

			if (!ArrayUtils.isEmpty((Object[])lootPoolSingletonContainer.functions)) {
				jsonObject.add("functions", jsonSerializationContext.serialize(lootPoolSingletonContainer.functions));
			}
		}

		public final T deserializeCustom(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			int i = GsonHelper.getAsInt(jsonObject, "weight", 1);
			int j = GsonHelper.getAsInt(jsonObject, "quality", 0);
			LootItemFunction[] lootItemFunctions = GsonHelper.getAsObject(
				jsonObject, "functions", new LootItemFunction[0], jsonDeserializationContext, LootItemFunction[].class
			);
			return this.deserialize(jsonObject, jsonDeserializationContext, i, j, lootItemConditions, lootItemFunctions);
		}

		protected abstract T deserialize(
			JsonObject jsonObject,
			JsonDeserializationContext jsonDeserializationContext,
			int i,
			int j,
			LootItemCondition[] lootItemConditions,
			LootItemFunction[] lootItemFunctions
		);
	}
}
