/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;

public class ChiseledBookShelfBlockEntity
extends BlockEntity
implements Container {
    public static final int MAX_BOOKS_IN_STORAGE = 6;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    private int lastInteractedSlot = -1;

    public ChiseledBookShelfBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.CHISELED_BOOKSHELF, blockPos, blockState);
    }

    private void updateState(int i) {
        if (i < 0 || i >= 6) {
            LOGGER.error("Expected slot 0-5, got {}", (Object)i);
            return;
        }
        this.lastInteractedSlot = i;
        BlockState blockState = this.getBlockState();
        for (int j = 0; j < ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); ++j) {
            boolean bl = !this.getItem(j).isEmpty();
            BooleanProperty booleanProperty = ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(j);
            blockState = (BlockState)blockState.setValue(booleanProperty, bl);
        }
        Objects.requireNonNull(this.level).setBlock(this.worldPosition, blockState, 3);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        this.items.clear();
        ContainerHelper.loadAllItems(compoundTag, this.items);
        this.lastInteractedSlot = compoundTag.getInt("last_interacted_slot");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        ContainerHelper.saveAllItems(compoundTag, this.items, true);
        compoundTag.putInt("last_interacted_slot", this.lastInteractedSlot);
    }

    public int count() {
        return (int)this.items.stream().filter(Predicate.not(ItemStack::isEmpty)).count();
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public int getContainerSize() {
        return 6;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack itemStack = Objects.requireNonNullElse(this.items.get(i), ItemStack.EMPTY);
        this.items.set(i, ItemStack.EMPTY);
        if (!itemStack.isEmpty()) {
            this.updateState(i);
        }
        return itemStack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return this.removeItem(i, 1);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        if (itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
            this.items.set(i, itemStack);
            this.updateState(i);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        return itemStack.is(ItemTags.BOOKSHELF_BOOKS) && this.getItem(i).isEmpty();
    }

    public int getLastInteractedSlot() {
        return this.lastInteractedSlot;
    }
}

