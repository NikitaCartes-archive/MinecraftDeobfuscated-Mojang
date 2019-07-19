/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import java.util.Collections;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface RecipeHolder {
    public void setRecipeUsed(@Nullable Recipe<?> var1);

    @Nullable
    public Recipe<?> getRecipeUsed();

    default public void awardAndReset(Player player) {
        Recipe<?> recipe = this.getRecipeUsed();
        if (recipe != null && !recipe.isSpecial()) {
            player.awardRecipes(Collections.singleton(recipe));
            this.setRecipeUsed(null);
        }
    }

    default public boolean setRecipeUsed(Level level, ServerPlayer serverPlayer, Recipe<?> recipe) {
        if (recipe.isSpecial() || !level.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) || serverPlayer.getRecipeBook().contains(recipe)) {
            this.setRecipeUsed(recipe);
            return true;
        }
        return false;
    }
}

