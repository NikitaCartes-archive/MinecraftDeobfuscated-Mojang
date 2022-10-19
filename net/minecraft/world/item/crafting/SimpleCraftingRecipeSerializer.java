/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SimpleCraftingRecipeSerializer<T extends CraftingRecipe>
implements RecipeSerializer<T> {
    private final Factory<T> constructor;

    public SimpleCraftingRecipeSerializer(Factory<T> factory) {
        this.constructor = factory;
    }

    @Override
    public T fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        CraftingBookCategory craftingBookCategory = Objects.requireNonNullElse(CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(jsonObject, "category", null)), CraftingBookCategory.MISC);
        return this.constructor.create(resourceLocation, craftingBookCategory);
    }

    @Override
    public T fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        CraftingBookCategory craftingBookCategory = friendlyByteBuf.readEnum(CraftingBookCategory.class);
        return this.constructor.create(resourceLocation, craftingBookCategory);
    }

    @Override
    public void toNetwork(FriendlyByteBuf friendlyByteBuf, T craftingRecipe) {
        friendlyByteBuf.writeEnum(craftingRecipe.category());
    }

    @Override
    public /* synthetic */ Recipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        return this.fromNetwork(resourceLocation, friendlyByteBuf);
    }

    @Override
    public /* synthetic */ Recipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        return this.fromJson(resourceLocation, jsonObject);
    }

    @FunctionalInterface
    public static interface Factory<T extends CraftingRecipe> {
        public T create(ResourceLocation var1, CraftingBookCategory var2);
    }
}

