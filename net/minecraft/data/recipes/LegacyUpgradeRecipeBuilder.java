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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval=true)
public class LegacyUpgradeRecipeBuilder {
    private final Ingredient base;
    private final Ingredient addition;
    private final RecipeCategory category;
    private final Item result;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    private final RecipeSerializer<?> type;

    public LegacyUpgradeRecipeBuilder(RecipeSerializer<?> recipeSerializer, Ingredient ingredient, Ingredient ingredient2, RecipeCategory recipeCategory, Item item) {
        this.category = recipeCategory;
        this.type = recipeSerializer;
        this.base = ingredient;
        this.addition = ingredient2;
        this.result = item;
    }

    public static LegacyUpgradeRecipeBuilder smithing(Ingredient ingredient, Ingredient ingredient2, RecipeCategory recipeCategory, Item item) {
        return new LegacyUpgradeRecipeBuilder(RecipeSerializer.SMITHING, ingredient, ingredient2, recipeCategory, item);
    }

    public LegacyUpgradeRecipeBuilder unlocks(String string, CriterionTriggerInstance criterionTriggerInstance) {
        this.advancement.addCriterion(string, criterionTriggerInstance);
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer, String string) {
        this.save(consumer, new ResourceLocation(string));
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation) {
        this.ensureValid(resourceLocation);
        this.advancement.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation)).rewards(AdvancementRewards.Builder.recipe(resourceLocation)).requirements(RequirementsStrategy.OR);
        consumer.accept(new Result(resourceLocation, this.type, this.base, this.addition, this.result, this.advancement, resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation resourceLocation) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
        }
    }

    public static class Result
    implements FinishedRecipe {
        private final ResourceLocation id;
        private final Ingredient base;
        private final Ingredient addition;
        private final Item result;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final RecipeSerializer<?> type;

        public Result(ResourceLocation resourceLocation, RecipeSerializer<?> recipeSerializer, Ingredient ingredient, Ingredient ingredient2, Item item, Advancement.Builder builder, ResourceLocation resourceLocation2) {
            this.id = resourceLocation;
            this.type = recipeSerializer;
            this.base = ingredient;
            this.addition = ingredient2;
            this.result = item;
            this.advancement = builder;
            this.advancementId = resourceLocation2;
        }

        @Override
        public void serializeRecipeData(JsonObject jsonObject) {
            jsonObject.add("base", this.base.toJson());
            jsonObject.add("addition", this.addition.toJson());
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
            jsonObject.add("result", jsonObject2);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return this.type;
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

