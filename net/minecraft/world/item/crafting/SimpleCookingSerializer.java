/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe>
implements RecipeSerializer<T> {
    private final int defaultCookingTime;
    private final CookieBaker<T> factory;

    public SimpleCookingSerializer(CookieBaker<T> cookieBaker, int i) {
        this.defaultCookingTime = i;
        this.factory = cookieBaker;
    }

    @Override
    public T fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        String string = GsonHelper.getAsString(jsonObject, "group", "");
        JsonElement jsonElement = GsonHelper.isArrayNode(jsonObject, "ingredient") ? GsonHelper.getAsJsonArray(jsonObject, "ingredient") : GsonHelper.getAsJsonObject(jsonObject, "ingredient");
        Ingredient ingredient = Ingredient.fromJson(jsonElement);
        String string2 = GsonHelper.getAsString(jsonObject, "result");
        ResourceLocation resourceLocation2 = new ResourceLocation(string2);
        ItemStack itemStack = new ItemStack(Registry.ITEM.getOptional(resourceLocation2).orElseThrow(() -> new IllegalStateException("Item: " + string2 + " does not exist")));
        float f = GsonHelper.getAsFloat(jsonObject, "experience", 0.0f);
        int i = GsonHelper.getAsInt(jsonObject, "cookingtime", this.defaultCookingTime);
        return this.factory.create(resourceLocation, string, ingredient, itemStack, f, i);
    }

    @Override
    public T fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        String string = friendlyByteBuf.readUtf(Short.MAX_VALUE);
        Ingredient ingredient = Ingredient.fromNetwork(friendlyByteBuf);
        ItemStack itemStack = friendlyByteBuf.readItem();
        float f = friendlyByteBuf.readFloat();
        int i = friendlyByteBuf.readVarInt();
        return this.factory.create(resourceLocation, string, ingredient, itemStack, f, i);
    }

    @Override
    public void toNetwork(FriendlyByteBuf friendlyByteBuf, T abstractCookingRecipe) {
        friendlyByteBuf.writeUtf(((AbstractCookingRecipe)abstractCookingRecipe).group);
        ((AbstractCookingRecipe)abstractCookingRecipe).ingredient.toNetwork(friendlyByteBuf);
        friendlyByteBuf.writeItem(((AbstractCookingRecipe)abstractCookingRecipe).result);
        friendlyByteBuf.writeFloat(((AbstractCookingRecipe)abstractCookingRecipe).experience);
        friendlyByteBuf.writeVarInt(((AbstractCookingRecipe)abstractCookingRecipe).cookingTime);
    }

    @Override
    public /* synthetic */ Recipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        return this.fromNetwork(resourceLocation, friendlyByteBuf);
    }

    @Override
    public /* synthetic */ Recipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        return this.fromJson(resourceLocation, jsonObject);
    }

    static interface CookieBaker<T extends AbstractCookingRecipe> {
        public T create(ResourceLocation var1, String var2, Ingredient var3, ItemStack var4, float var5, int var6);
    }
}

