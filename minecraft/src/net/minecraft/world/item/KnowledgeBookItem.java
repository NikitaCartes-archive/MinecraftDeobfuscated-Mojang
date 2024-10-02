package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class KnowledgeBookItem extends Item {
	private static final Logger LOGGER = LogUtils.getLogger();

	public KnowledgeBookItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		List<ResourceKey<Recipe<?>>> list = itemStack.getOrDefault(DataComponents.RECIPES, List.of());
		itemStack.consume(1, player);
		if (list.isEmpty()) {
			return InteractionResult.FAIL;
		} else {
			if (!level.isClientSide) {
				RecipeManager recipeManager = level.getServer().getRecipeManager();
				List<RecipeHolder<?>> list2 = new ArrayList(list.size());

				for (ResourceKey<Recipe<?>> resourceKey : list) {
					Optional<RecipeHolder<?>> optional = recipeManager.byKey(resourceKey);
					if (!optional.isPresent()) {
						LOGGER.error("Invalid recipe: {}", resourceKey);
						return InteractionResult.FAIL;
					}

					list2.add((RecipeHolder)optional.get());
				}

				player.awardRecipes(list2);
				player.awardStat(Stats.ITEM_USED.get(this));
			}

			return InteractionResult.SUCCESS;
		}
	}
}
