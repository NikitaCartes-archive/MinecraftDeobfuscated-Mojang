package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTable {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final LootTable EMPTY = new LootTable(LootContextParamSets.EMPTY, Optional.empty(), List.of(), List.of());
	public static final LootContextParamSet DEFAULT_PARAM_SET = LootContextParamSets.ALL_PARAMS;
	public static final long RANDOMIZE_SEED = 0L;
	public static final Codec<LootTable> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					LootContextParamSets.CODEC.optionalFieldOf("type", DEFAULT_PARAM_SET).forGetter(lootTable -> lootTable.paramSet),
					ExtraCodecs.strictOptionalField(ResourceLocation.CODEC, "random_sequence").forGetter(lootTable -> lootTable.randomSequence),
					ExtraCodecs.strictOptionalField(LootPool.CODEC.listOf(), "pools", List.of()).forGetter(lootTable -> lootTable.pools),
					ExtraCodecs.strictOptionalField(LootItemFunctions.CODEC.listOf(), "functions", List.of()).forGetter(lootTable -> lootTable.functions)
				)
				.apply(instance, LootTable::new)
	);
	private final LootContextParamSet paramSet;
	private final Optional<ResourceLocation> randomSequence;
	private final List<LootPool> pools;
	private final List<LootItemFunction> functions;
	private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

	LootTable(LootContextParamSet lootContextParamSet, Optional<ResourceLocation> optional, List<LootPool> list, List<LootItemFunction> list2) {
		this.paramSet = lootContextParamSet;
		this.randomSequence = optional;
		this.pools = list;
		this.functions = list2;
		this.compositeFunction = LootItemFunctions.compose(list2);
	}

	public static Consumer<ItemStack> createStackSplitter(ServerLevel serverLevel, Consumer<ItemStack> consumer) {
		return itemStack -> {
			if (itemStack.isItemEnabled(serverLevel.enabledFeatures())) {
				if (itemStack.getCount() < itemStack.getMaxStackSize()) {
					consumer.accept(itemStack);
				} else {
					int i = itemStack.getCount();

					while (i > 0) {
						ItemStack itemStack2 = itemStack.copyWithCount(Math.min(itemStack.getMaxStackSize(), i));
						i -= itemStack2.getCount();
						consumer.accept(itemStack2);
					}
				}
			}
		};
	}

	public void getRandomItemsRaw(LootParams lootParams, Consumer<ItemStack> consumer) {
		this.getRandomItemsRaw(new LootContext.Builder(lootParams).create(this.randomSequence), consumer);
	}

	public void getRandomItemsRaw(LootContext lootContext, Consumer<ItemStack> consumer) {
		LootContext.VisitedEntry<?> visitedEntry = LootContext.createVisitedEntry(this);
		if (lootContext.pushVisitedElement(visitedEntry)) {
			Consumer<ItemStack> consumer2 = LootItemFunction.decorate(this.compositeFunction, consumer, lootContext);

			for (LootPool lootPool : this.pools) {
				lootPool.addRandomItems(consumer2, lootContext);
			}

			lootContext.popVisitedElement(visitedEntry);
		} else {
			LOGGER.warn("Detected infinite loop in loot tables");
		}
	}

	public void getRandomItems(LootParams lootParams, long l, Consumer<ItemStack> consumer) {
		this.getRandomItemsRaw(
			new LootContext.Builder(lootParams).withOptionalRandomSeed(l).create(this.randomSequence), createStackSplitter(lootParams.getLevel(), consumer)
		);
	}

	public void getRandomItems(LootParams lootParams, Consumer<ItemStack> consumer) {
		this.getRandomItemsRaw(lootParams, createStackSplitter(lootParams.getLevel(), consumer));
	}

	public void getRandomItems(LootContext lootContext, Consumer<ItemStack> consumer) {
		this.getRandomItemsRaw(lootContext, createStackSplitter(lootContext.getLevel(), consumer));
	}

	public ObjectArrayList<ItemStack> getRandomItems(LootParams lootParams, long l) {
		return this.getRandomItems(new LootContext.Builder(lootParams).withOptionalRandomSeed(l).create(this.randomSequence));
	}

	public ObjectArrayList<ItemStack> getRandomItems(LootParams lootParams) {
		return this.getRandomItems(new LootContext.Builder(lootParams).create(this.randomSequence));
	}

	private ObjectArrayList<ItemStack> getRandomItems(LootContext lootContext) {
		ObjectArrayList<ItemStack> objectArrayList = new ObjectArrayList<>();
		this.getRandomItems(lootContext, objectArrayList::add);
		return objectArrayList;
	}

	public LootContextParamSet getParamSet() {
		return this.paramSet;
	}

	public void validate(ValidationContext validationContext) {
		for (int i = 0; i < this.pools.size(); i++) {
			((LootPool)this.pools.get(i)).validate(validationContext.forChild(".pools[" + i + "]"));
		}

		for (int i = 0; i < this.functions.size(); i++) {
			((LootItemFunction)this.functions.get(i)).validate(validationContext.forChild(".functions[" + i + "]"));
		}
	}

	public void fill(Container container, LootParams lootParams, long l) {
		LootContext lootContext = new LootContext.Builder(lootParams).withOptionalRandomSeed(l).create(this.randomSequence);
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
		private final ImmutableList.Builder<LootPool> pools = ImmutableList.builder();
		private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
		private LootContextParamSet paramSet = LootTable.DEFAULT_PARAM_SET;
		private Optional<ResourceLocation> randomSequence = Optional.empty();

		public LootTable.Builder withPool(LootPool.Builder builder) {
			this.pools.add(builder.build());
			return this;
		}

		public LootTable.Builder setParamSet(LootContextParamSet lootContextParamSet) {
			this.paramSet = lootContextParamSet;
			return this;
		}

		public LootTable.Builder setRandomSequence(ResourceLocation resourceLocation) {
			this.randomSequence = Optional.of(resourceLocation);
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
			return new LootTable(this.paramSet, this.randomSequence, this.pools.build(), this.functions.build());
		}
	}
}
