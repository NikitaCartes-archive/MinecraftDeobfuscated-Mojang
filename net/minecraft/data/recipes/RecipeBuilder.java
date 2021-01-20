/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.recipes;

import java.util.function.Consumer;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;

public interface RecipeBuilder {
    public RecipeBuilder unlockedBy(String var1, CriterionTriggerInstance var2);

    public RecipeBuilder group(String var1);

    public void save(Consumer<FinishedRecipe> var1);
}

