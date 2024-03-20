package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products.P4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootPoolSingletonContainer extends LootPoolEntryContainer {
	public static final int DEFAULT_WEIGHT = 1;
	public static final int DEFAULT_QUALITY = 0;
	protected final int weight;
	protected final int quality;
	protected final List<LootItemFunction> functions;
	final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
	private final LootPoolEntry entry = new LootPoolSingletonContainer.EntryBase() {
		@Override
		public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
			LootPoolSingletonContainer.this.createItemStack(
				LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, consumer, lootContext), lootContext
			);
		}
	};

	protected LootPoolSingletonContainer(int i, int j, List<LootItemCondition> list, List<LootItemFunction> list2) {
		super(list);
		this.weight = i;
		this.quality = j;
		this.functions = list2;
		this.compositeFunction = LootItemFunctions.compose(list2);
	}

	protected static <T extends LootPoolSingletonContainer> P4<Mu<T>, Integer, Integer, List<LootItemCondition>, List<LootItemFunction>> singletonFields(
		Instance<T> instance
	) {
		return instance.group(
				ExtraCodecs.strictOptionalField(Codec.INT, "weight", 1).forGetter(lootPoolSingletonContainer -> lootPoolSingletonContainer.weight),
				ExtraCodecs.strictOptionalField(Codec.INT, "quality", 0).forGetter(lootPoolSingletonContainer -> lootPoolSingletonContainer.quality)
			)
			.and(commonFields(instance).t1())
			.and(
				ExtraCodecs.strictOptionalField(LootItemFunctions.ROOT_CODEC.listOf(), "functions", List.of())
					.forGetter(lootPoolSingletonContainer -> lootPoolSingletonContainer.functions)
			);
	}

	@Override
	public void validate(ValidationContext validationContext) {
		super.validate(validationContext);

		for (int i = 0; i < this.functions.size(); i++) {
			((LootItemFunction)this.functions.get(i)).validate(validationContext.forChild(".functions[" + i + "]"));
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
		private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();

		public T apply(LootItemFunction.Builder builder) {
			this.functions.add(builder.build());
			return this.getThis();
		}

		protected List<LootItemFunction> getFunctions() {
			return this.functions.build();
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
		LootPoolSingletonContainer build(int i, int j, List<LootItemCondition> list, List<LootItemFunction> list2);
	}
}
