/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

public interface FinishedRecipe {
    public void serializeRecipeData(JsonObject var1);

    default public JsonObject serializeRecipe() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", BuiltInRegistries.RECIPE_SERIALIZER.getKey(this.getType()).toString());
        this.serializeRecipeData(jsonObject);
        return jsonObject;
    }

    public ResourceLocation getId();

    public RecipeSerializer<?> getType();

    @Nullable
    public JsonObject serializeAdvancement();

    @Nullable
    public ResourceLocation getAdvancementId();
}

