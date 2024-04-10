package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerContents extends LootItemConditionalFunction {
	public static final MapCodec<SetContainerContents> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<ContainerComponentManipulator<?>, List<LootPoolEntryContainer>>and(
					instance.group(
						ContainerComponentManipulators.CODEC.fieldOf("component").forGetter(setContainerContents -> setContainerContents.component),
						LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter(setContainerContents -> setContainerContents.entries)
					)
				)
				.apply(instance, SetContainerContents::new)
	);
	private final ContainerComponentManipulator<?> component;
	private final List<LootPoolEntryContainer> entries;

	SetContainerContents(List<LootItemCondition> list, ContainerComponentManipulator<?> containerComponentManipulator, List<LootPoolEntryContainer> list2) {
		super(list);
		this.component = containerComponentManipulator;
		this.entries = List.copyOf(list2);
	}

	@Override
	public LootItemFunctionType<SetContainerContents> getType() {
		return LootItemFunctions.SET_CONTENTS;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.isEmpty()) {
			return itemStack;
		} else {
			java.util.stream.Stream.Builder<ItemStack> builder = Stream.builder();
			this.entries
				.forEach(
					lootPoolEntryContainer -> lootPoolEntryContainer.expand(
							lootContext, lootPoolEntry -> lootPoolEntry.createItemStack(LootTable.createStackSplitter(lootContext.getLevel(), builder::add), lootContext)
						)
				);
			this.component.setContents(itemStack, builder.build());
			return itemStack;
		}
	}

	@Override
	public void validate(ValidationContext validationContext) {
		super.validate(validationContext);

		for (int i = 0; i < this.entries.size(); i++) {
			((LootPoolEntryContainer)this.entries.get(i)).validate(validationContext.forChild(".entry[" + i + "]"));
		}
	}

	public static SetContainerContents.Builder setContents(ContainerComponentManipulator<?> containerComponentManipulator) {
		return new SetContainerContents.Builder(containerComponentManipulator);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<SetContainerContents.Builder> {
		private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
		private final ContainerComponentManipulator<?> component;

		public Builder(ContainerComponentManipulator<?> containerComponentManipulator) {
			this.component = containerComponentManipulator;
		}

		protected SetContainerContents.Builder getThis() {
			return this;
		}

		public SetContainerContents.Builder withEntry(LootPoolEntryContainer.Builder<?> builder) {
			this.entries.add(builder.build());
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new SetContainerContents(this.getConditions(), this.component, this.entries.build());
		}
	}
}
