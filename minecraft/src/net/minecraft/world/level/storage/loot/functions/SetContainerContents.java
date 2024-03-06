package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerContents extends LootItemConditionalFunction {
	public static final Codec<SetContainerContents> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.<Holder<BlockEntityType<?>>, List<LootPoolEntryContainer>>and(
					instance.group(
						BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec().fieldOf("type").forGetter(setContainerContents -> setContainerContents.type),
						LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter(setContainerContents -> setContainerContents.entries)
					)
				)
				.apply(instance, SetContainerContents::new)
	);
	private final Holder<BlockEntityType<?>> type;
	private final List<LootPoolEntryContainer> entries;

	SetContainerContents(List<LootItemCondition> list, Holder<BlockEntityType<?>> holder, List<LootPoolEntryContainer> list2) {
		super(list);
		this.type = holder;
		this.entries = List.copyOf(list2);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_CONTENTS;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.isEmpty()) {
			return itemStack;
		} else {
			NonNullList<ItemStack> nonNullList = NonNullList.create();
			this.entries
				.forEach(
					lootPoolEntryContainer -> lootPoolEntryContainer.expand(
							lootContext, lootPoolEntry -> lootPoolEntry.createItemStack(LootTable.createStackSplitter(lootContext.getLevel(), nonNullList::add), lootContext)
						)
				);
			itemStack.set(DataComponents.CONTAINER, ItemContainerContents.copyOf(nonNullList));
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

	public static SetContainerContents.Builder setContents(BlockEntityType<?> blockEntityType) {
		return new SetContainerContents.Builder(blockEntityType);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<SetContainerContents.Builder> {
		private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
		private final BlockEntityType<?> type;

		public Builder(BlockEntityType<?> blockEntityType) {
			this.type = blockEntityType;
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
			return new SetContainerContents(this.getConditions(), this.type.builtInRegistryHolder(), this.entries.build());
		}
	}
}
