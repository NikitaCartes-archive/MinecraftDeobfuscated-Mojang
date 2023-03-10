/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerContents
extends LootItemConditionalFunction {
    final List<LootPoolEntryContainer> entries;
    final BlockEntityType<?> type;

    SetContainerContents(LootItemCondition[] lootItemConditions, BlockEntityType<?> blockEntityType, List<LootPoolEntryContainer> list) {
        super(lootItemConditions);
        this.type = blockEntityType;
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
        }
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        this.entries.forEach(lootPoolEntryContainer -> lootPoolEntryContainer.expand(lootContext, lootPoolEntry -> lootPoolEntry.createItemStack(LootTable.createStackSplitter(lootContext, nonNullList::add), lootContext)));
        CompoundTag compoundTag = new CompoundTag();
        ContainerHelper.saveAllItems(compoundTag, nonNullList);
        CompoundTag compoundTag2 = BlockItem.getBlockEntityData(itemStack);
        if (compoundTag2 == null) {
            compoundTag2 = compoundTag;
        } else {
            compoundTag2.merge(compoundTag);
        }
        BlockItem.setBlockEntityData(itemStack, this.type, compoundTag2);
        return itemStack;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        for (int i = 0; i < this.entries.size(); ++i) {
            this.entries.get(i).validate(validationContext.forChild(".entry[" + i + "]"));
        }
    }

    public static Builder setContents(BlockEntityType<?> blockEntityType) {
        return new Builder(blockEntityType);
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();
        private final BlockEntityType<?> type;

        public Builder(BlockEntityType<?> blockEntityType) {
            this.type = blockEntityType;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder withEntry(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add(builder.build());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetContainerContents(this.getConditions(), this.type, this.entries);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetContainerContents> {
        @Override
        public void serialize(JsonObject jsonObject, SetContainerContents setContainerContents, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setContainerContents, jsonSerializationContext);
            jsonObject.addProperty("type", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(setContainerContents.type).toString());
            jsonObject.add("entries", jsonSerializationContext.serialize(setContainerContents.entries));
        }

        @Override
        public SetContainerContents deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            LootPoolEntryContainer[] lootPoolEntryContainers = GsonHelper.getAsObject(jsonObject, "entries", jsonDeserializationContext, LootPoolEntryContainer[].class);
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "type"));
            BlockEntityType<?> blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown block entity type id '" + resourceLocation + "'"));
            return new SetContainerContents(lootItemConditions, blockEntityType, Arrays.asList(lootPoolEntryContainers));
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }
}

