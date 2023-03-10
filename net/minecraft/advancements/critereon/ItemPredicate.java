/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public class ItemPredicate {
    public static final ItemPredicate ANY = new ItemPredicate();
    @Nullable
    private final TagKey<Item> tag;
    @Nullable
    private final Set<Item> items;
    private final MinMaxBounds.Ints count;
    private final MinMaxBounds.Ints durability;
    private final EnchantmentPredicate[] enchantments;
    private final EnchantmentPredicate[] storedEnchantments;
    @Nullable
    private final Potion potion;
    private final NbtPredicate nbt;

    public ItemPredicate() {
        this.tag = null;
        this.items = null;
        this.potion = null;
        this.count = MinMaxBounds.Ints.ANY;
        this.durability = MinMaxBounds.Ints.ANY;
        this.enchantments = EnchantmentPredicate.NONE;
        this.storedEnchantments = EnchantmentPredicate.NONE;
        this.nbt = NbtPredicate.ANY;
    }

    public ItemPredicate(@Nullable TagKey<Item> tagKey, @Nullable Set<Item> set, MinMaxBounds.Ints ints, MinMaxBounds.Ints ints2, EnchantmentPredicate[] enchantmentPredicates, EnchantmentPredicate[] enchantmentPredicates2, @Nullable Potion potion, NbtPredicate nbtPredicate) {
        this.tag = tagKey;
        this.items = set;
        this.count = ints;
        this.durability = ints2;
        this.enchantments = enchantmentPredicates;
        this.storedEnchantments = enchantmentPredicates2;
        this.potion = potion;
        this.nbt = nbtPredicate;
    }

    public boolean matches(ItemStack itemStack) {
        Map<Enchantment, Integer> map;
        if (this == ANY) {
            return true;
        }
        if (this.tag != null && !itemStack.is(this.tag)) {
            return false;
        }
        if (this.items != null && !this.items.contains(itemStack.getItem())) {
            return false;
        }
        if (!this.count.matches(itemStack.getCount())) {
            return false;
        }
        if (!this.durability.isAny() && !itemStack.isDamageableItem()) {
            return false;
        }
        if (!this.durability.matches(itemStack.getMaxDamage() - itemStack.getDamageValue())) {
            return false;
        }
        if (!this.nbt.matches(itemStack)) {
            return false;
        }
        if (this.enchantments.length > 0) {
            map = EnchantmentHelper.deserializeEnchantments(itemStack.getEnchantmentTags());
            for (EnchantmentPredicate enchantmentPredicate : this.enchantments) {
                if (enchantmentPredicate.containedIn(map)) continue;
                return false;
            }
        }
        if (this.storedEnchantments.length > 0) {
            map = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(itemStack));
            for (EnchantmentPredicate enchantmentPredicate : this.storedEnchantments) {
                if (enchantmentPredicate.containedIn(map)) continue;
                return false;
            }
        }
        Potion potion = PotionUtils.getPotion(itemStack);
        return this.potion == null || this.potion == potion;
    }

    public static ItemPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "item");
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("count"));
        MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject.get("durability"));
        if (jsonObject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
        }
        NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonObject.get("nbt"));
        ImmutableCollection set = null;
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "items", null);
        if (jsonArray != null) {
            ImmutableSet.Builder builder = ImmutableSet.builder();
            for (JsonElement jsonElement2 : jsonArray) {
                ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.convertToString(jsonElement2, "item"));
                builder.add((Item)BuiltInRegistries.ITEM.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown item id '" + resourceLocation + "'")));
            }
            set = builder.build();
        }
        TagKey<Item> tagKey = null;
        if (jsonObject.has("tag")) {
            ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
            tagKey = TagKey.create(Registries.ITEM, resourceLocation2);
        }
        Potion potion = null;
        if (jsonObject.has("potion")) {
            ResourceLocation resourceLocation3 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "potion"));
            potion = (Potion)BuiltInRegistries.POTION.getOptional(resourceLocation3).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + resourceLocation3 + "'"));
        }
        EnchantmentPredicate[] enchantmentPredicates = EnchantmentPredicate.fromJsonArray(jsonObject.get("enchantments"));
        EnchantmentPredicate[] enchantmentPredicates2 = EnchantmentPredicate.fromJsonArray(jsonObject.get("stored_enchantments"));
        return new ItemPredicate(tagKey, (Set<Item>)((Object)set), ints, ints2, enchantmentPredicates, enchantmentPredicates2, potion, nbtPredicate);
    }

    public JsonElement serializeToJson() {
        JsonArray jsonArray;
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (this.items != null) {
            jsonArray = new JsonArray();
            for (Item item : this.items) {
                jsonArray.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }
            jsonObject.add("items", jsonArray);
        }
        if (this.tag != null) {
            jsonObject.addProperty("tag", this.tag.location().toString());
        }
        jsonObject.add("count", this.count.serializeToJson());
        jsonObject.add("durability", this.durability.serializeToJson());
        jsonObject.add("nbt", this.nbt.serializeToJson());
        if (this.enchantments.length > 0) {
            jsonArray = new JsonArray();
            for (EnchantmentPredicate enchantmentPredicate : this.enchantments) {
                jsonArray.add(enchantmentPredicate.serializeToJson());
            }
            jsonObject.add("enchantments", jsonArray);
        }
        if (this.storedEnchantments.length > 0) {
            jsonArray = new JsonArray();
            for (EnchantmentPredicate enchantmentPredicate : this.storedEnchantments) {
                jsonArray.add(enchantmentPredicate.serializeToJson());
            }
            jsonObject.add("stored_enchantments", jsonArray);
        }
        if (this.potion != null) {
            jsonObject.addProperty("potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
        }
        return jsonObject;
    }

    public static ItemPredicate[] fromJsonArray(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return new ItemPredicate[0];
        }
        JsonArray jsonArray = GsonHelper.convertToJsonArray(jsonElement, "items");
        ItemPredicate[] itemPredicates = new ItemPredicate[jsonArray.size()];
        for (int i = 0; i < itemPredicates.length; ++i) {
            itemPredicates[i] = ItemPredicate.fromJson(jsonArray.get(i));
        }
        return itemPredicates;
    }

    public static class Builder {
        private final List<EnchantmentPredicate> enchantments = Lists.newArrayList();
        private final List<EnchantmentPredicate> storedEnchantments = Lists.newArrayList();
        @Nullable
        private Set<Item> items;
        @Nullable
        private TagKey<Item> tag;
        private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
        private MinMaxBounds.Ints durability = MinMaxBounds.Ints.ANY;
        @Nullable
        private Potion potion;
        private NbtPredicate nbt = NbtPredicate.ANY;

        private Builder() {
        }

        public static Builder item() {
            return new Builder();
        }

        public Builder of(ItemLike ... itemLikes) {
            this.items = Stream.of(itemLikes).map(ItemLike::asItem).collect(ImmutableSet.toImmutableSet());
            return this;
        }

        public Builder of(TagKey<Item> tagKey) {
            this.tag = tagKey;
            return this;
        }

        public Builder withCount(MinMaxBounds.Ints ints) {
            this.count = ints;
            return this;
        }

        public Builder hasDurability(MinMaxBounds.Ints ints) {
            this.durability = ints;
            return this;
        }

        public Builder isPotion(Potion potion) {
            this.potion = potion;
            return this;
        }

        public Builder hasNbt(CompoundTag compoundTag) {
            this.nbt = new NbtPredicate(compoundTag);
            return this;
        }

        public Builder hasEnchantment(EnchantmentPredicate enchantmentPredicate) {
            this.enchantments.add(enchantmentPredicate);
            return this;
        }

        public Builder hasStoredEnchantment(EnchantmentPredicate enchantmentPredicate) {
            this.storedEnchantments.add(enchantmentPredicate);
            return this;
        }

        public ItemPredicate build() {
            return new ItemPredicate(this.tag, this.items, this.count, this.durability, this.enchantments.toArray(EnchantmentPredicate.NONE), this.storedEnchantments.toArray(EnchantmentPredicate.NONE), this.potion, this.nbt);
        }
    }
}

