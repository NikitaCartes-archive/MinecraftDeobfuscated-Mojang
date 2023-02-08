/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerPlaceRecipe<C extends Container>
implements PlaceRecipe<Integer> {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final StackedContents stackedContents = new StackedContents();
    protected Inventory inventory;
    protected RecipeBookMenu<C> menu;

    public ServerPlaceRecipe(RecipeBookMenu<C> recipeBookMenu) {
        this.menu = recipeBookMenu;
    }

    public void recipeClicked(ServerPlayer serverPlayer, @Nullable Recipe<C> recipe, boolean bl) {
        if (recipe == null || !serverPlayer.getRecipeBook().contains(recipe)) {
            return;
        }
        this.inventory = serverPlayer.getInventory();
        if (!this.testClearGrid() && !serverPlayer.isCreative()) {
            return;
        }
        this.stackedContents.clear();
        serverPlayer.getInventory().fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        if (this.stackedContents.canCraft(recipe, null)) {
            this.handleRecipeClicked(recipe, bl);
        } else {
            this.clearGrid();
            serverPlayer.connection.send(new ClientboundPlaceGhostRecipePacket(serverPlayer.containerMenu.containerId, recipe));
        }
        serverPlayer.getInventory().setChanged();
    }

    protected void clearGrid() {
        for (int i = 0; i < this.menu.getSize(); ++i) {
            if (!this.menu.shouldMoveToInventory(i)) continue;
            ItemStack itemStack = this.menu.getSlot(i).getItem().copy();
            this.inventory.placeItemBackInInventory(itemStack, false);
            this.menu.getSlot(i).set(itemStack);
        }
        this.menu.clearCraftingContent();
    }

    protected void handleRecipeClicked(Recipe<C> recipe, boolean bl) {
        IntArrayList intList;
        int j;
        boolean bl2 = this.menu.recipeMatches(recipe);
        int i = this.stackedContents.getBiggestCraftableStack(recipe, null);
        if (bl2) {
            for (j = 0; j < this.menu.getGridHeight() * this.menu.getGridWidth() + 1; ++j) {
                ItemStack itemStack;
                if (j == this.menu.getResultSlotIndex() || (itemStack = this.menu.getSlot(j).getItem()).isEmpty() || Math.min(i, itemStack.getMaxStackSize()) >= itemStack.getCount() + 1) continue;
                return;
            }
        }
        if (this.stackedContents.canCraft(recipe, intList = new IntArrayList(), j = this.getStackSize(bl, i, bl2))) {
            int k = j;
            IntListIterator intListIterator = intList.iterator();
            while (intListIterator.hasNext()) {
                int l = (Integer)intListIterator.next();
                int m = StackedContents.fromStackingIndex(l).getMaxStackSize();
                if (m >= k) continue;
                k = m;
            }
            j = k;
            if (this.stackedContents.canCraft(recipe, intList, j)) {
                this.clearGrid();
                this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, intList.iterator(), j);
            }
        }
    }

    @Override
    public void addItemToSlot(Iterator<Integer> iterator, int i, int j, int k, int l) {
        Slot slot = this.menu.getSlot(i);
        ItemStack itemStack = StackedContents.fromStackingIndex(iterator.next());
        if (!itemStack.isEmpty()) {
            for (int m = 0; m < j; ++m) {
                this.moveItemToGrid(slot, itemStack);
            }
        }
    }

    protected int getStackSize(boolean bl, int i, boolean bl2) {
        int j = 1;
        if (bl) {
            j = i;
        } else if (bl2) {
            j = 64;
            for (int k = 0; k < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++k) {
                ItemStack itemStack;
                if (k == this.menu.getResultSlotIndex() || (itemStack = this.menu.getSlot(k).getItem()).isEmpty() || j <= itemStack.getCount()) continue;
                j = itemStack.getCount();
            }
            if (j < 64) {
                ++j;
            }
        }
        return j;
    }

    protected void moveItemToGrid(Slot slot, ItemStack itemStack) {
        int i = this.inventory.findSlotMatchingUnusedItem(itemStack);
        if (i == -1) {
            return;
        }
        ItemStack itemStack2 = this.inventory.getItem(i).copy();
        if (itemStack2.isEmpty()) {
            return;
        }
        if (itemStack2.getCount() > 1) {
            this.inventory.removeItem(i, 1);
        } else {
            this.inventory.removeItemNoUpdate(i);
        }
        itemStack2.setCount(1);
        if (slot.getItem().isEmpty()) {
            slot.set(itemStack2);
        } else {
            slot.getItem().grow(1);
        }
    }

    private boolean testClearGrid() {
        ArrayList<ItemStack> list = Lists.newArrayList();
        int i = this.getAmountOfFreeSlotsInInventory();
        for (int j = 0; j < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++j) {
            ItemStack itemStack;
            if (j == this.menu.getResultSlotIndex() || (itemStack = this.menu.getSlot(j).getItem().copy()).isEmpty()) continue;
            int k = this.inventory.getSlotWithRemainingSpace(itemStack);
            if (k == -1 && list.size() <= i) {
                for (ItemStack itemStack2 : list) {
                    if (!itemStack2.sameItem(itemStack) || itemStack2.getCount() == itemStack2.getMaxStackSize() || itemStack2.getCount() + itemStack.getCount() > itemStack2.getMaxStackSize()) continue;
                    itemStack2.grow(itemStack.getCount());
                    itemStack.setCount(0);
                    break;
                }
                if (itemStack.isEmpty()) continue;
                if (list.size() < i) {
                    list.add(itemStack);
                    continue;
                }
                return false;
            }
            if (k != -1) continue;
            return false;
        }
        return true;
    }

    private int getAmountOfFreeSlotsInInventory() {
        int i = 0;
        for (ItemStack itemStack : this.inventory.items) {
            if (!itemStack.isEmpty()) continue;
            ++i;
        }
        return i;
    }
}

