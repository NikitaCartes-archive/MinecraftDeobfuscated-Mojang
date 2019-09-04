/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.FillPlayerHead;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetAttributesFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.functions.SetContainerLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetLoreFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;

public class LootItemFunctions {
    private static final Map<ResourceLocation, LootItemFunction.Serializer<?>> FUNCTIONS_BY_NAME = Maps.newHashMap();
    private static final Map<Class<? extends LootItemFunction>, LootItemFunction.Serializer<?>> FUNCTIONS_BY_CLASS = Maps.newHashMap();
    public static final BiFunction<ItemStack, LootContext, ItemStack> IDENTITY = (itemStack, lootContext) -> itemStack;

    public static <T extends LootItemFunction> void register(LootItemFunction.Serializer<? extends T> serializer) {
        ResourceLocation resourceLocation = serializer.getName();
        Class<T> class_ = serializer.getFunctionClass();
        if (FUNCTIONS_BY_NAME.containsKey(resourceLocation)) {
            throw new IllegalArgumentException("Can't re-register item function name " + resourceLocation);
        }
        if (FUNCTIONS_BY_CLASS.containsKey(class_)) {
            throw new IllegalArgumentException("Can't re-register item function class " + class_.getName());
        }
        FUNCTIONS_BY_NAME.put(resourceLocation, serializer);
        FUNCTIONS_BY_CLASS.put(class_, serializer);
    }

    public static LootItemFunction.Serializer<?> getSerializer(ResourceLocation resourceLocation) {
        LootItemFunction.Serializer<?> serializer = FUNCTIONS_BY_NAME.get(resourceLocation);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown loot item function '" + resourceLocation + "'");
        }
        return serializer;
    }

    public static <T extends LootItemFunction> LootItemFunction.Serializer<T> getSerializer(T lootItemFunction) {
        LootItemFunction.Serializer<?> serializer = FUNCTIONS_BY_CLASS.get(lootItemFunction.getClass());
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown loot item function " + lootItemFunction);
        }
        return serializer;
    }

    public static BiFunction<ItemStack, LootContext, ItemStack> compose(BiFunction<ItemStack, LootContext, ItemStack>[] biFunctions) {
        switch (biFunctions.length) {
            case 0: {
                return IDENTITY;
            }
            case 1: {
                return biFunctions[0];
            }
            case 2: {
                BiFunction<ItemStack, LootContext, ItemStack> biFunction = biFunctions[0];
                BiFunction<ItemStack, LootContext, ItemStack> biFunction2 = biFunctions[1];
                return (itemStack, lootContext) -> (ItemStack)biFunction2.apply((ItemStack)biFunction.apply((ItemStack)itemStack, (LootContext)lootContext), (LootContext)lootContext);
            }
        }
        return (itemStack, lootContext) -> {
            for (BiFunction biFunction : biFunctions) {
                itemStack = (ItemStack)biFunction.apply(itemStack, lootContext);
            }
            return itemStack;
        };
    }

    static {
        LootItemFunctions.register(new SetItemCountFunction.Serializer());
        LootItemFunctions.register(new EnchantWithLevelsFunction.Serializer());
        LootItemFunctions.register(new EnchantRandomlyFunction.Serializer());
        LootItemFunctions.register(new SetNbtFunction.Serializer());
        LootItemFunctions.register(new SmeltItemFunction.Serializer());
        LootItemFunctions.register(new LootingEnchantFunction.Serializer());
        LootItemFunctions.register(new SetItemDamageFunction.Serializer());
        LootItemFunctions.register(new SetAttributesFunction.Serializer());
        LootItemFunctions.register(new SetNameFunction.Serializer());
        LootItemFunctions.register(new ExplorationMapFunction.Serializer());
        LootItemFunctions.register(new SetStewEffectFunction.Serializer());
        LootItemFunctions.register(new CopyNameFunction.Serializer());
        LootItemFunctions.register(new SetContainerContents.Serializer());
        LootItemFunctions.register(new LimitCount.Serializer());
        LootItemFunctions.register(new ApplyBonusCount.Serializer());
        LootItemFunctions.register(new SetContainerLootTable.Serializer());
        LootItemFunctions.register(new ApplyExplosionDecay.Serializer());
        LootItemFunctions.register(new SetLoreFunction.Serializer());
        LootItemFunctions.register(new FillPlayerHead.Serializer());
        LootItemFunctions.register(new CopyNbtFunction.Serializer());
        LootItemFunctions.register(new CopyBlockState.Serializer());
    }

    public static class Serializer
    implements JsonDeserializer<LootItemFunction>,
    JsonSerializer<LootItemFunction> {
        @Override
        public LootItemFunction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            LootItemFunction.Serializer<ResourceLocation> serializer;
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "function");
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "function"));
            try {
                serializer = LootItemFunctions.getSerializer(resourceLocation);
            } catch (IllegalArgumentException illegalArgumentException) {
                throw new JsonSyntaxException("Unknown function '" + resourceLocation + "'");
            }
            return serializer.deserialize(jsonObject, jsonDeserializationContext);
        }

        @Override
        public JsonElement serialize(LootItemFunction lootItemFunction, Type type, JsonSerializationContext jsonSerializationContext) {
            LootItemFunction.Serializer<LootItemFunction> serializer = LootItemFunctions.getSerializer(lootItemFunction);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("function", serializer.getName().toString());
            serializer.serialize(jsonObject, lootItemFunction, jsonSerializationContext);
            return jsonObject;
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((LootItemFunction)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

