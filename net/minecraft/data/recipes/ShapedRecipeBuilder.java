/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public class ShapedRecipeBuilder
extends CraftingRecipeBuilder
implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    @Nullable
    private String group;
    private boolean showNotification = true;

    public ShapedRecipeBuilder(RecipeCategory recipeCategory, ItemLike itemLike, int i) {
        this.category = recipeCategory;
        this.result = itemLike.asItem();
        this.count = i;
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory recipeCategory, ItemLike itemLike) {
        return ShapedRecipeBuilder.shaped(recipeCategory, itemLike, 1);
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory recipeCategory, ItemLike itemLike, int i) {
        return new ShapedRecipeBuilder(recipeCategory, itemLike, i);
    }

    public ShapedRecipeBuilder define(Character character, TagKey<Item> tagKey) {
        return this.define(character, Ingredient.of(tagKey));
    }

    public ShapedRecipeBuilder define(Character character, ItemLike itemLike) {
        return this.define(character, Ingredient.of(itemLike));
    }

    public ShapedRecipeBuilder define(Character character, Ingredient ingredient) {
        if (this.key.containsKey(character)) {
            throw new IllegalArgumentException("Symbol '" + character + "' is already defined!");
        }
        if (character.charValue() == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        }
        this.key.put(character, ingredient);
        return this;
    }

    public ShapedRecipeBuilder pattern(String string) {
        if (!this.rows.isEmpty() && string.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        }
        this.rows.add(string);
        return this;
    }

    @Override
    public ShapedRecipeBuilder unlockedBy(String string, CriterionTriggerInstance criterionTriggerInstance) {
        this.advancement.addCriterion(string, criterionTriggerInstance);
        return this;
    }

    @Override
    public ShapedRecipeBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    public ShapedRecipeBuilder showNotification(boolean bl) {
        this.showNotification = bl;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation) {
        this.ensureValid(resourceLocation);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation)).rewards(AdvancementRewards.Builder.recipe(resourceLocation)).requirements(RequirementsStrategy.OR);
        consumer.accept(new Result(resourceLocation, this.result, this.count, this.group == null ? "" : this.group, ShapedRecipeBuilder.determineBookCategory(this.category), this.rows, this.key, this.advancement, resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/"), this.showNotification));
    }

    private void ensureValid(ResourceLocation resourceLocation) {
        if (this.rows.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped recipe " + resourceLocation + "!");
        }
        HashSet<Character> set = Sets.newHashSet(this.key.keySet());
        set.remove(Character.valueOf(' '));
        for (String string : this.rows) {
            for (int i = 0; i < string.length(); ++i) {
                char c = string.charAt(i);
                if (!this.key.containsKey(Character.valueOf(c)) && c != ' ') {
                    throw new IllegalStateException("Pattern in recipe " + resourceLocation + " uses undefined symbol '" + c + "'");
                }
                set.remove(Character.valueOf(c));
            }
        }
        if (!set.isEmpty()) {
            throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + resourceLocation);
        }
        if (this.rows.size() == 1 && this.rows.get(0).length() == 1) {
            throw new IllegalStateException("Shaped recipe " + resourceLocation + " only takes in a single item - should it be a shapeless recipe instead?");
        }
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
        }
    }

    @Override
    public /* synthetic */ RecipeBuilder group(@Nullable String string) {
        return this.group(string);
    }

    @Override
    public /* synthetic */ RecipeBuilder unlockedBy(String string, CriterionTriggerInstance criterionTriggerInstance) {
        return this.unlockedBy(string, criterionTriggerInstance);
    }

    static class Result
    extends CraftingRecipeBuilder.CraftingResult {
        private final ResourceLocation id;
        private final Item result;
        private final int count;
        private final String group;
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final boolean showNotification;

        public Result(ResourceLocation resourceLocation, Item item, int i, String string, CraftingBookCategory craftingBookCategory, List<String> list, Map<Character, Ingredient> map, Advancement.Builder builder, ResourceLocation resourceLocation2, boolean bl) {
            super(craftingBookCategory);
            this.id = resourceLocation;
            this.result = item;
            this.count = i;
            this.group = string;
            this.pattern = list;
            this.key = map;
            this.advancement = builder;
            this.advancementId = resourceLocation2;
            this.showNotification = bl;
        }

        @Override
        public void serializeRecipeData(JsonObject jsonObject) {
            super.serializeRecipeData(jsonObject);
            if (!this.group.isEmpty()) {
                jsonObject.addProperty("group", this.group);
            }
            JsonArray jsonArray = new JsonArray();
            for (String string : this.pattern) {
                jsonArray.add(string);
            }
            jsonObject.add("pattern", jsonArray);
            JsonObject jsonObject2 = new JsonObject();
            for (Map.Entry<Character, Ingredient> entry : this.key.entrySet()) {
                jsonObject2.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }
            jsonObject.add("key", jsonObject2);
            JsonObject jsonObject3 = new JsonObject();
            jsonObject3.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
            if (this.count > 1) {
                jsonObject3.addProperty("count", this.count);
            }
            jsonObject.add("result", jsonObject3);
            jsonObject.addProperty("show_notification", this.showNotification);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPED_RECIPE;
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

