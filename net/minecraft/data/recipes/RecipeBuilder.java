/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.recipes;

import java.util.function.Consumer;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public interface RecipeBuilder {
    public static final ResourceLocation ROOT_RECIPE_ADVANCEMENT = new ResourceLocation("recipes/root");

    public RecipeBuilder unlockedBy(String var1, CriterionTriggerInstance var2);

    public RecipeBuilder group(@Nullable String var1);

    public Item getResult();

    public void save(Consumer<FinishedRecipe> var1, ResourceLocation var2);

    default public void save(Consumer<FinishedRecipe> consumer) {
        this.save(consumer, RecipeBuilder.getDefaultRecipeId(this.getResult()));
    }

    default public void save(Consumer<FinishedRecipe> consumer, String string) {
        ResourceLocation resourceLocation2 = new ResourceLocation(string);
        ResourceLocation resourceLocation = RecipeBuilder.getDefaultRecipeId(this.getResult());
        if (resourceLocation2.equals(resourceLocation)) {
            throw new IllegalStateException("Recipe " + string + " should remove its 'save' argument as it is equal to default one");
        }
        this.save(consumer, resourceLocation2);
    }

    public static ResourceLocation getDefaultRecipeId(ItemLike itemLike) {
        return BuiltInRegistries.ITEM.getKey(itemLike.asItem());
    }
}

