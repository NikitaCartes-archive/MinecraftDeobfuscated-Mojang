/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KnowledgeBookItem
extends Item {
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
        if (compoundTag == null || !compoundTag.contains("Recipes", 9)) {
            LOGGER.error("Tag not valid: {}", (Object)compoundTag);
            return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, itemStack);
        }
        if (!level.isClientSide) {
            ListTag listTag = compoundTag.getList("Recipes", 8);
            ArrayList<Recipe<?>> list = Lists.newArrayList();
            RecipeManager recipeManager = level.getServer().getRecipeManager();
            for (int i = 0; i < listTag.size(); ++i) {
                String string = listTag.getString(i);
                Optional<Recipe<?>> optional = recipeManager.byKey(new ResourceLocation(string));
                if (!optional.isPresent()) {
                    LOGGER.error("Invalid recipe: {}", (Object)string);
                    return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, itemStack);
                }
                list.add(optional.get());
            }
            player.awardRecipes(list);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStack);
    }
}

