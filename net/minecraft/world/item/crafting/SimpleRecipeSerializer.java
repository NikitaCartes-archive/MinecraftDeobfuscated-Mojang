/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SimpleRecipeSerializer<T extends Recipe<?>>
implements RecipeSerializer<T> {
    private final Function<ResourceLocation, T> constructor;

    public SimpleRecipeSerializer(Function<ResourceLocation, T> function) {
        this.constructor = function;
    }

    @Override
    public T fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        return (T)((Recipe)this.constructor.apply(resourceLocation));
    }

    @Override
    public T fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        return (T)((Recipe)this.constructor.apply(resourceLocation));
    }

    @Override
    public void toNetwork(FriendlyByteBuf friendlyByteBuf, T recipe) {
    }
}

