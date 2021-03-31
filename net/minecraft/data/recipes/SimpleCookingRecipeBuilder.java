/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public class SimpleCookingRecipeBuilder {
    private final Item result;
    private final Ingredient ingredient;
    private final float experience;
    private final int cookingTime;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    private String group;
    private final SimpleCookingSerializer<?> serializer;

    private SimpleCookingRecipeBuilder(ItemLike itemLike, Ingredient ingredient, float f, int i, SimpleCookingSerializer<?> simpleCookingSerializer) {
        this.result = itemLike.asItem();
        this.ingredient = ingredient;
        this.experience = f;
        this.cookingTime = i;
        this.serializer = simpleCookingSerializer;
    }

    public static SimpleCookingRecipeBuilder cooking(Ingredient ingredient, ItemLike itemLike, float f, int i, SimpleCookingSerializer<?> simpleCookingSerializer) {
        return new SimpleCookingRecipeBuilder(itemLike, ingredient, f, i, simpleCookingSerializer);
    }

    public static SimpleCookingRecipeBuilder campfireCooking(Ingredient ingredient, ItemLike itemLike, float f, int i) {
        return SimpleCookingRecipeBuilder.cooking(ingredient, itemLike, f, i, RecipeSerializer.CAMPFIRE_COOKING_RECIPE);
    }

    public static SimpleCookingRecipeBuilder blasting(Ingredient ingredient, ItemLike itemLike, float f, int i) {
        return SimpleCookingRecipeBuilder.cooking(ingredient, itemLike, f, i, RecipeSerializer.BLASTING_RECIPE);
    }

    public static SimpleCookingRecipeBuilder smelting(Ingredient ingredient, ItemLike itemLike, float f, int i) {
        return SimpleCookingRecipeBuilder.cooking(ingredient, itemLike, f, i, RecipeSerializer.SMELTING_RECIPE);
    }

    public static SimpleCookingRecipeBuilder smoking(Ingredient ingredient, ItemLike itemLike, float f, int i) {
        return SimpleCookingRecipeBuilder.cooking(ingredient, itemLike, f, i, RecipeSerializer.SMOKING_RECIPE);
    }

    public SimpleCookingRecipeBuilder unlockedBy(String string, CriterionTriggerInstance criterionTriggerInstance) {
        this.advancement.addCriterion(string, criterionTriggerInstance);
        return this;
    }

    public SimpleCookingRecipeBuilder group(String string) {
        this.group = string;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        this.save(consumer, Registry.ITEM.getKey(this.result));
    }

    public void save(Consumer<FinishedRecipe> consumer, String string) {
        ResourceLocation resourceLocation2 = new ResourceLocation(string);
        ResourceLocation resourceLocation = Registry.ITEM.getKey(this.result);
        if (resourceLocation2.equals(resourceLocation)) {
            throw new IllegalStateException("Recipe " + resourceLocation2 + " should remove its 'save' argument");
        }
        this.save(consumer, resourceLocation2);
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation) {
        this.ensureValid(resourceLocation);
        this.advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation)).rewards(AdvancementRewards.Builder.recipe(resourceLocation)).requirements(RequirementsStrategy.OR);
        consumer.accept(new Result(resourceLocation, this.group == null ? "" : this.group, this.ingredient, this.result, this.experience, this.cookingTime, this.advancement, new ResourceLocation(resourceLocation.getNamespace(), "recipes/" + this.result.getItemCategory().getRecipeFolderName() + "/" + resourceLocation.getPath()), this.serializer));
    }

    private void ensureValid(ResourceLocation resourceLocation) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
        }
    }

    public static class Result
    implements FinishedRecipe {
        private final ResourceLocation id;
        private final String group;
        private final Ingredient ingredient;
        private final Item result;
        private final float experience;
        private final int cookingTime;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

        public Result(ResourceLocation resourceLocation, String string, Ingredient ingredient, Item item, float f, int i, Advancement.Builder builder, ResourceLocation resourceLocation2, RecipeSerializer<? extends AbstractCookingRecipe> recipeSerializer) {
            this.id = resourceLocation;
            this.group = string;
            this.ingredient = ingredient;
            this.result = item;
            this.experience = f;
            this.cookingTime = i;
            this.advancement = builder;
            this.advancementId = resourceLocation2;
            this.serializer = recipeSerializer;
        }

        @Override
        public void serializeRecipeData(JsonObject jsonObject) {
            if (!this.group.isEmpty()) {
                jsonObject.addProperty("group", this.group);
            }
            jsonObject.add("ingredient", this.ingredient.toJson());
            jsonObject.addProperty("result", Registry.ITEM.getKey(this.result).toString());
            jsonObject.addProperty("experience", Float.valueOf(this.experience));
            jsonObject.addProperty("cookingtime", this.cookingTime);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return this.serializer;
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        @Nullable
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Override
        @Nullable
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}

