/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SmithingMenu
extends ItemCombinerMenu {
    public static final int TEMPLATE_SLOT = 0;
    public static final int BASE_SLOT = 1;
    public static final int ADDITIONAL_SLOT = 2;
    public static final int RESULT_SLOT = 3;
    public static final int TEMPLATE_SLOT_X_PLACEMENT = 8;
    public static final int BASE_SLOT_X_PLACEMENT = 26;
    public static final int ADDITIONAL_SLOT_X_PLACEMENT = 44;
    private static final int RESULT_SLOT_X_PLACEMENT = 98;
    public static final int SLOT_Y_PLACEMENT = 48;
    private final Level level;
    @Nullable
    private SmithingRecipe selectedRecipe;
    private final List<SmithingRecipe> recipes;

    public SmithingMenu(int i, Inventory inventory) {
        this(i, inventory, ContainerLevelAccess.NULL);
    }

    public SmithingMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(MenuType.SMITHING, i, inventory, containerLevelAccess);
        this.level = inventory.player.level;
        this.recipes = this.level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING);
    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create().withSlot(0, 8, 48, itemStack -> this.recipes.stream().anyMatch(smithingRecipe -> smithingRecipe.isTemplateIngredient((ItemStack)itemStack))).withSlot(1, 26, 48, itemStack -> this.recipes.stream().anyMatch(smithingRecipe -> smithingRecipe.isBaseIngredient((ItemStack)itemStack) && smithingRecipe.isTemplateIngredient(((Slot)this.slots.get(0)).getItem()))).withSlot(2, 44, 48, itemStack -> this.recipes.stream().anyMatch(smithingRecipe -> smithingRecipe.isAdditionIngredient((ItemStack)itemStack) && smithingRecipe.isTemplateIngredient(((Slot)this.slots.get(0)).getItem()))).withResultSlot(3, 98, 48).build();
    }

    @Override
    protected boolean isValidBlock(BlockState blockState) {
        return blockState.is(Blocks.SMITHING_TABLE);
    }

    @Override
    protected boolean mayPickup(Player player, boolean bl) {
        return this.selectedRecipe != null && this.selectedRecipe.matches(this.inputSlots, this.level);
    }

    @Override
    protected void onTake(Player player, ItemStack itemStack) {
        itemStack.onCraftedBy(player.level, player, itemStack.getCount());
        this.resultSlots.awardUsedRecipes(player);
        this.shrinkStackInSlot(0);
        this.shrinkStackInSlot(1);
        this.shrinkStackInSlot(2);
        this.access.execute((level, blockPos) -> level.levelEvent(1044, (BlockPos)blockPos, 0));
    }

    private void shrinkStackInSlot(int i) {
        ItemStack itemStack = this.inputSlots.getItem(i);
        itemStack.shrink(1);
        this.inputSlots.setItem(i, itemStack);
    }

    @Override
    public void createResult() {
        List<SmithingRecipe> list = this.level.getRecipeManager().getRecipesFor(RecipeType.SMITHING, this.inputSlots, this.level);
        if (list.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        } else {
            SmithingRecipe smithingRecipe = list.get(0);
            ItemStack itemStack = smithingRecipe.assemble(this.inputSlots, this.level.registryAccess());
            if (itemStack.isItemEnabled(this.level.enabledFeatures())) {
                this.selectedRecipe = smithingRecipe;
                this.resultSlots.setRecipeUsed(smithingRecipe);
                this.resultSlots.setItem(0, itemStack);
            }
        }
    }

    @Override
    public int getSlotToQuickMoveTo(ItemStack itemStack) {
        return this.recipes.stream().map(smithingRecipe -> SmithingMenu.findSlotMatchingIngredient(smithingRecipe, itemStack)).filter(Optional::isPresent).findFirst().orElse(Optional.of(0)).get();
    }

    private static Optional<Integer> findSlotMatchingIngredient(SmithingRecipe smithingRecipe, ItemStack itemStack) {
        if (smithingRecipe.isTemplateIngredient(itemStack)) {
            return Optional.of(0);
        }
        if (smithingRecipe.isBaseIngredient(itemStack)) {
            return Optional.of(1);
        }
        if (smithingRecipe.isAdditionIngredient(itemStack)) {
            return Optional.of(2);
        }
        return Optional.empty();
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
    }

    @Override
    public boolean canMoveIntoInputSlots(ItemStack itemStack) {
        return this.recipes.stream().map(smithingRecipe -> SmithingMenu.findSlotMatchingIngredient(smithingRecipe, itemStack)).anyMatch(Optional::isPresent);
    }
}

