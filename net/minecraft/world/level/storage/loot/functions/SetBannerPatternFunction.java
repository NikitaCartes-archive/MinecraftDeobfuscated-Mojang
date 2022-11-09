/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction
extends LootItemConditionalFunction {
    final List<Pair<Holder<BannerPattern>, DyeColor>> patterns;
    final boolean append;

    SetBannerPatternFunction(LootItemCondition[] lootItemConditions, List<Pair<Holder<BannerPattern>, DyeColor>> list, boolean bl) {
        super(lootItemConditions);
        this.patterns = list;
        this.append = bl;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        ListTag listTag2;
        CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
        if (compoundTag == null) {
            compoundTag = new CompoundTag();
        }
        BannerPattern.Builder builder = new BannerPattern.Builder();
        this.patterns.forEach(builder::addPattern);
        ListTag listTag = builder.toListTag();
        if (this.append) {
            listTag2 = compoundTag.getList("Patterns", 10).copy();
            listTag2.addAll(listTag);
        } else {
            listTag2 = listTag;
        }
        compoundTag.put("Patterns", listTag2);
        BlockItem.setBlockEntityData(itemStack, BlockEntityType.BANNER, compoundTag);
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_BANNER_PATTERN;
    }

    public static Builder setBannerPattern(boolean bl) {
        return new Builder(bl);
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final ImmutableList.Builder<Pair<Holder<BannerPattern>, DyeColor>> patterns = ImmutableList.builder();
        private final boolean append;

        Builder(boolean bl) {
            this.append = bl;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetBannerPatternFunction(this.getConditions(), (List<Pair<Holder<BannerPattern>, DyeColor>>)((Object)this.patterns.build()), this.append);
        }

        public Builder addPattern(ResourceKey<BannerPattern> resourceKey, DyeColor dyeColor) {
            return this.addPattern(BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(resourceKey), dyeColor);
        }

        public Builder addPattern(Holder<BannerPattern> holder, DyeColor dyeColor) {
            this.patterns.add((Object)Pair.of(holder, dyeColor));
            return this;
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }

    public static class Serializer
    extends LootItemConditionalFunction.Serializer<SetBannerPatternFunction> {
        @Override
        public void serialize(JsonObject jsonObject, SetBannerPatternFunction setBannerPatternFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setBannerPatternFunction, jsonSerializationContext);
            JsonArray jsonArray = new JsonArray();
            setBannerPatternFunction.patterns.forEach(pair -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("pattern", ((Holder)pair.getFirst()).unwrapKey().orElseThrow(() -> new JsonSyntaxException("Unknown pattern: " + pair.getFirst())).location().toString());
                jsonObject.addProperty("color", ((DyeColor)pair.getSecond()).getName());
                jsonArray.add(jsonObject);
            });
            jsonObject.add("patterns", jsonArray);
            jsonObject.addProperty("append", setBannerPatternFunction.append);
        }

        @Override
        public SetBannerPatternFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            ImmutableList.Builder builder = ImmutableList.builder();
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "patterns");
            for (int i = 0; i < jsonArray.size(); ++i) {
                JsonObject jsonObject2 = GsonHelper.convertToJsonObject(jsonArray.get(i), "pattern[" + i + "]");
                String string = GsonHelper.getAsString(jsonObject2, "pattern");
                Optional<Holder.Reference<BannerPattern>> optional = BuiltInRegistries.BANNER_PATTERN.getHolder(ResourceKey.create(Registries.BANNER_PATTERN, new ResourceLocation(string)));
                if (optional.isEmpty()) {
                    throw new JsonSyntaxException("Unknown pattern: " + string);
                }
                String string2 = GsonHelper.getAsString(jsonObject2, "color");
                DyeColor dyeColor = DyeColor.byName(string2, null);
                if (dyeColor == null) {
                    throw new JsonSyntaxException("Unknown color: " + string2);
                }
                builder.add(Pair.of((Holder)optional.get(), dyeColor));
            }
            boolean bl = GsonHelper.getAsBoolean(jsonObject, "append");
            return new SetBannerPatternFunction(lootItemConditions, (List<Pair<Holder<BannerPattern>, DyeColor>>)((Object)builder.build()), bl);
        }

        @Override
        public /* synthetic */ LootItemConditionalFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }
    }
}

