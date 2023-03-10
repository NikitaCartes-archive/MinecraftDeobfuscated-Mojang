/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

public class EnchantmentPredicate {
    public static final EnchantmentPredicate ANY = new EnchantmentPredicate();
    public static final EnchantmentPredicate[] NONE = new EnchantmentPredicate[0];
    @Nullable
    private final Enchantment enchantment;
    private final MinMaxBounds.Ints level;

    public EnchantmentPredicate() {
        this.enchantment = null;
        this.level = MinMaxBounds.Ints.ANY;
    }

    public EnchantmentPredicate(@Nullable Enchantment enchantment, MinMaxBounds.Ints ints) {
        this.enchantment = enchantment;
        this.level = ints;
    }

    public boolean containedIn(Map<Enchantment, Integer> map) {
        if (this.enchantment != null) {
            if (!map.containsKey(this.enchantment)) {
                return false;
            }
            int i = map.get(this.enchantment);
            if (this.level != MinMaxBounds.Ints.ANY && !this.level.matches(i)) {
                return false;
            }
        } else if (this.level != MinMaxBounds.Ints.ANY) {
            for (Integer integer : map.values()) {
                if (!this.level.matches(integer)) continue;
                return true;
            }
            return false;
        }
        return true;
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (this.enchantment != null) {
            jsonObject.addProperty("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment).toString());
        }
        jsonObject.add("levels", this.level.serializeToJson());
        return jsonObject;
    }

    public static EnchantmentPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "enchantment");
        Enchantment enchantment = null;
        if (jsonObject.has("enchantment")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "enchantment"));
            enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown enchantment '" + resourceLocation + "'"));
        }
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("levels"));
        return new EnchantmentPredicate(enchantment, ints);
    }

    public static EnchantmentPredicate[] fromJsonArray(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return NONE;
        }
        JsonArray jsonArray = GsonHelper.convertToJsonArray(jsonElement, "enchantments");
        EnchantmentPredicate[] enchantmentPredicates = new EnchantmentPredicate[jsonArray.size()];
        for (int i = 0; i < enchantmentPredicates.length; ++i) {
            enchantmentPredicates[i] = EnchantmentPredicate.fromJson(jsonArray.get(i));
        }
        return enchantmentPredicates;
    }
}

