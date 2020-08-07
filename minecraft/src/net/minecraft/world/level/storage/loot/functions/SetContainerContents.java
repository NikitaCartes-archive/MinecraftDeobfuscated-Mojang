package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerContents extends LootItemConditionalFunction {
	private final List<LootPoolEntryContainer> entries;

	private SetContainerContents(LootItemCondition[] lootItemConditions, List<LootPoolEntryContainer> list) {
		super(lootItemConditions);
		this.entries = ImmutableList.copyOf(list);
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
							lootContext, lootPoolEntry -> lootPoolEntry.createItemStack(LootTable.createStackSplitter(nonNullList::add), lootContext)
						)
				);
			CompoundTag compoundTag = new CompoundTag();
			ContainerHelper.saveAllItems(compoundTag, nonNullList);
			CompoundTag compoundTag2 = itemStack.getOrCreateTag();
			compoundTag2.put("BlockEntityTag", compoundTag.merge(compoundTag2.getCompound("BlockEntityTag")));
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

	public static SetContainerContents.Builder setContents() {
		return new SetContainerContents.Builder();
	}

	public static class Builder extends LootItemConditionalFunction.Builder<SetContainerContents.Builder> {
		private final List<LootPoolEntryContainer> entries = Lists.<LootPoolEntryContainer>newArrayList();

		protected SetContainerContents.Builder getThis() {
			return this;
		}

		public SetContainerContents.Builder withEntry(LootPoolEntryContainer.Builder<?> builder) {
			this.entries.add(builder.build());
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new SetContainerContents(this.getConditions(), this.entries);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerContents> {
		public void serialize(JsonObject jsonObject, SetContainerContents setContainerContents, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setContainerContents, jsonSerializationContext);
			jsonObject.add("entries", jsonSerializationContext.serialize(setContainerContents.entries));
		}

		public SetContainerContents deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			LootPoolEntryContainer[] lootPoolEntryContainers = GsonHelper.getAsObject(jsonObject, "entries", jsonDeserializationContext, LootPoolEntryContainer[].class);
			return new SetContainerContents(lootItemConditions, Arrays.asList(lootPoolEntryContainers));
		}
	}
}
