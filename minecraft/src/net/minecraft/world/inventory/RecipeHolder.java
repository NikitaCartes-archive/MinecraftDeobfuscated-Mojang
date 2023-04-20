package net.minecraft.world.inventory;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface RecipeHolder {
	void setRecipeUsed(@Nullable Recipe<?> recipe);

	@Nullable
	Recipe<?> getRecipeUsed();

	default void awardUsedRecipes(Player player, List<ItemStack> list) {
		Recipe<?> recipe = this.getRecipeUsed();
		if (recipe != null) {
			player.triggerRecipeCrafted(recipe, list);
			if (!recipe.isSpecial()) {
				player.awardRecipes(Collections.singleton(recipe));
				this.setRecipeUsed(null);
			}
		}
	}

	default boolean setRecipeUsed(Level level, ServerPlayer serverPlayer, Recipe<?> recipe) {
		if (!recipe.isSpecial() && level.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) && !serverPlayer.getRecipeBook().contains(recipe)) {
			return false;
		} else {
			this.setRecipeUsed(recipe);
			return true;
		}
	}
}
