package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KnowledgeBookItem extends Item {
	private static final Logger LOGGER = LogManager.getLogger();

	public KnowledgeBookItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		CompoundTag compoundTag = itemStack.getTag();
		if (!player.abilities.instabuild) {
			player.setItemInHand(interactionHand, ItemStack.EMPTY);
		}

		if (compoundTag != null && compoundTag.contains("Recipes", 9)) {
			if (!level.isClientSide) {
				ListTag listTag = compoundTag.getList("Recipes", 8);
				List<Recipe<?>> list = Lists.<Recipe<?>>newArrayList();
				RecipeManager recipeManager = level.getServer().getRecipeManager();

				for (int i = 0; i < listTag.size(); i++) {
					String string = listTag.getString(i);
					Optional<? extends Recipe<?>> optional = recipeManager.byKey(new ResourceLocation(string));
					if (!optional.isPresent()) {
						LOGGER.error("Invalid recipe: {}", string);
						return InteractionResultHolder.fail(itemStack);
					}

					list.add(optional.get());
				}

				player.awardRecipes(list);
				player.awardStat(Stats.ITEM_USED.get(this));
			}

			return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
		} else {
			LOGGER.error("Tag not valid: {}", compoundTag);
			return InteractionResultHolder.fail(itemStack);
		}
	}
}
