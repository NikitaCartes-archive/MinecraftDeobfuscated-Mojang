/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;

public class EnchantmentMenu
extends AbstractContainerMenu {
    private final Container enchantSlots = new SimpleContainer(2){

        @Override
        public void setChanged() {
            super.setChanged();
            EnchantmentMenu.this.slotsChanged(this);
        }
    };
    private final ContainerLevelAccess access;
    private final RandomSource random = RandomSource.create();
    private final DataSlot enchantmentSeed = DataSlot.standalone();
    public final int[] costs = new int[3];
    public final int[] enchantClue = new int[]{-1, -1, -1};
    public final int[] levelClue = new int[]{-1, -1, -1};

    public EnchantmentMenu(int i, Inventory inventory) {
        this(i, inventory, ContainerLevelAccess.NULL);
    }

    public EnchantmentMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(MenuType.ENCHANTMENT, i);
        int j;
        this.access = containerLevelAccess;
        this.addSlot(new Slot(this.enchantSlots, 0, 15, 47){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return true;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(this.enchantSlots, 1, 35, 47){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(Items.LAPIS_LAZULI);
            }
        });
        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(inventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(inventory, j, 8 + j * 18, 142));
        }
        this.addDataSlot(DataSlot.shared(this.costs, 0));
        this.addDataSlot(DataSlot.shared(this.costs, 1));
        this.addDataSlot(DataSlot.shared(this.costs, 2));
        this.addDataSlot(this.enchantmentSeed).set(inventory.player.getEnchantmentSeed());
        this.addDataSlot(DataSlot.shared(this.enchantClue, 0));
        this.addDataSlot(DataSlot.shared(this.enchantClue, 1));
        this.addDataSlot(DataSlot.shared(this.enchantClue, 2));
        this.addDataSlot(DataSlot.shared(this.levelClue, 0));
        this.addDataSlot(DataSlot.shared(this.levelClue, 1));
        this.addDataSlot(DataSlot.shared(this.levelClue, 2));
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.enchantSlots) {
            ItemStack itemStack = container.getItem(0);
            if (itemStack.isEmpty() || !itemStack.isEnchantable()) {
                for (int i = 0; i < 3; ++i) {
                    this.costs[i] = 0;
                    this.enchantClue[i] = -1;
                    this.levelClue[i] = -1;
                }
            } else {
                this.access.execute((level, blockPos) -> {
                    int j;
                    int i = 0;
                    for (BlockPos blockPos2 : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
                        if (!EnchantmentTableBlock.isValidBookShelf(level, blockPos, blockPos2)) continue;
                        ++i;
                    }
                    this.random.setSeed(this.enchantmentSeed.get());
                    for (j = 0; j < 3; ++j) {
                        this.costs[j] = EnchantmentHelper.getEnchantmentCost(this.random, j, i, itemStack);
                        this.enchantClue[j] = -1;
                        this.levelClue[j] = -1;
                        if (this.costs[j] >= j + 1) continue;
                        this.costs[j] = 0;
                    }
                    for (j = 0; j < 3; ++j) {
                        List<EnchantmentInstance> list;
                        if (this.costs[j] <= 0 || (list = this.getEnchantmentList(itemStack, j, this.costs[j])) == null || list.isEmpty()) continue;
                        EnchantmentInstance enchantmentInstance = list.get(this.random.nextInt(list.size()));
                        this.enchantClue[j] = BuiltInRegistries.ENCHANTMENT.getId(enchantmentInstance.enchantment);
                        this.levelClue[j] = enchantmentInstance.level;
                    }
                    this.broadcastChanges();
                });
            }
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int i) {
        if (i < 0 || i >= this.costs.length) {
            Util.logAndPauseIfInIde(player.getName() + " pressed invalid button id: " + i);
            return false;
        }
        ItemStack itemStack = this.enchantSlots.getItem(0);
        ItemStack itemStack2 = this.enchantSlots.getItem(1);
        int j = i + 1;
        if ((itemStack2.isEmpty() || itemStack2.getCount() < j) && !player.getAbilities().instabuild) {
            return false;
        }
        if (this.costs[i] > 0 && !itemStack.isEmpty() && (player.experienceLevel >= j && player.experienceLevel >= this.costs[i] || player.getAbilities().instabuild)) {
            this.access.execute((level, blockPos) -> {
                ItemStack itemStack3 = itemStack;
                List<EnchantmentInstance> list = this.getEnchantmentList(itemStack3, i, this.costs[i]);
                if (!list.isEmpty()) {
                    player.onEnchantmentPerformed(itemStack3, j);
                    boolean bl = itemStack3.is(Items.BOOK);
                    if (bl) {
                        itemStack3 = new ItemStack(Items.ENCHANTED_BOOK);
                        CompoundTag compoundTag = itemStack.getTag();
                        if (compoundTag != null) {
                            itemStack3.setTag(compoundTag.copy());
                        }
                        this.enchantSlots.setItem(0, itemStack3);
                    }
                    for (int k = 0; k < list.size(); ++k) {
                        EnchantmentInstance enchantmentInstance = list.get(k);
                        if (bl) {
                            EnchantedBookItem.addEnchantment(itemStack3, enchantmentInstance);
                            continue;
                        }
                        itemStack3.enchant(enchantmentInstance.enchantment, enchantmentInstance.level);
                    }
                    if (!player.getAbilities().instabuild) {
                        itemStack2.shrink(j);
                        if (itemStack2.isEmpty()) {
                            this.enchantSlots.setItem(1, ItemStack.EMPTY);
                        }
                    }
                    player.awardStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer)player, itemStack3, j);
                    }
                    this.enchantSlots.setChanged();
                    this.enchantmentSeed.set(player.getEnchantmentSeed());
                    this.slotsChanged(this.enchantSlots);
                    level.playSound(null, (BlockPos)blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, level.random.nextFloat() * 0.1f + 0.9f);
                }
            });
            return true;
        }
        return false;
    }

    private List<EnchantmentInstance> getEnchantmentList(ItemStack itemStack, int i, int j) {
        this.random.setSeed(this.enchantmentSeed.get() + i);
        List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(this.random, itemStack, j, false);
        if (itemStack.is(Items.BOOK) && list.size() > 1) {
            list.remove(this.random.nextInt(list.size()));
        }
        return list;
    }

    public int getGoldCount() {
        ItemStack itemStack = this.enchantSlots.getItem(1);
        if (itemStack.isEmpty()) {
            return 0;
        }
        return itemStack.getCount();
    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed.get();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.enchantSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return EnchantmentMenu.stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (i == 0) {
                if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (i == 1) {
                if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemStack2.is(Items.LAPIS_LAZULI)) {
                if (!this.moveItemStackTo(itemStack2, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!((Slot)this.slots.get(0)).hasItem() && ((Slot)this.slots.get(0)).mayPlace(itemStack2)) {
                ItemStack itemStack3 = itemStack2.copy();
                itemStack3.setCount(1);
                itemStack2.shrink(1);
                ((Slot)this.slots.get(0)).setByPlayer(itemStack3);
            } else {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack2);
        }
        return itemStack;
    }
}

