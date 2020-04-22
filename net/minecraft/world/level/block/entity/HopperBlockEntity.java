/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

public class HopperBlockEntity
extends RandomizableContainerBlockEntity
implements Hopper,
TickableBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int cooldownTime = -1;
    private long tickedGameTime;

    public HopperBlockEntity() {
        super(BlockEntityType.HOPPER);
    }

    @Override
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.items);
        }
        this.cooldownTime = compoundTag.getInt("TransferCooldown");
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (!this.trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.items);
        }
        compoundTag.putInt("TransferCooldown", this.cooldownTime);
        return compoundTag;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        this.unpackLootTable(null);
        return ContainerHelper.removeItem(this.getItems(), i, j);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.unpackLootTable(null);
        this.getItems().set(i, itemStack);
        if (itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.hopper");
    }

    @Override
    public void tick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        --this.cooldownTime;
        this.tickedGameTime = this.level.getGameTime();
        if (!this.isOnCooldown()) {
            this.setCooldown(0);
            this.tryMoveItems(() -> HopperBlockEntity.suckInItems(this));
        }
    }

    private boolean tryMoveItems(Supplier<Boolean> supplier) {
        if (this.level == null || this.level.isClientSide) {
            return false;
        }
        if (!this.isOnCooldown() && this.getBlockState().getValue(HopperBlock.ENABLED).booleanValue()) {
            boolean bl = false;
            if (!this.isEmpty()) {
                bl = this.ejectItems();
            }
            if (!this.inventoryFull()) {
                bl |= supplier.get().booleanValue();
            }
            if (bl) {
                this.setCooldown(8);
                this.setChanged();
                return true;
            }
        }
        return false;
    }

    private boolean inventoryFull() {
        for (ItemStack itemStack : this.items) {
            if (!itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxStackSize()) continue;
            return false;
        }
        return true;
    }

    private boolean ejectItems() {
        Container container = this.getAttachedContainer();
        if (container == null) {
            return false;
        }
        Direction direction = this.getBlockState().getValue(HopperBlock.FACING).getOpposite();
        if (this.isFullContainer(container, direction)) {
            return false;
        }
        for (int i = 0; i < this.getContainerSize(); ++i) {
            if (this.getItem(i).isEmpty()) continue;
            ItemStack itemStack = this.getItem(i).copy();
            ItemStack itemStack2 = HopperBlockEntity.addItem(this, container, this.removeItem(i, 1), direction);
            if (itemStack2.isEmpty()) {
                container.setChanged();
                return true;
            }
            this.setItem(i, itemStack);
        }
        return false;
    }

    private static IntStream getSlots(Container container, Direction direction) {
        if (container instanceof WorldlyContainer) {
            return IntStream.of(((WorldlyContainer)container).getSlotsForFace(direction));
        }
        return IntStream.range(0, container.getContainerSize());
    }

    private boolean isFullContainer(Container container, Direction direction) {
        return HopperBlockEntity.getSlots(container, direction).allMatch(i -> {
            ItemStack itemStack = container.getItem(i);
            return itemStack.getCount() >= itemStack.getMaxStackSize();
        });
    }

    private static boolean isEmptyContainer(Container container, Direction direction) {
        return HopperBlockEntity.getSlots(container, direction).allMatch(i -> container.getItem(i).isEmpty());
    }

    public static boolean suckInItems(Hopper hopper) {
        Container container = HopperBlockEntity.getSourceContainer(hopper);
        if (container != null) {
            Direction direction = Direction.DOWN;
            if (HopperBlockEntity.isEmptyContainer(container, direction)) {
                return false;
            }
            return HopperBlockEntity.getSlots(container, direction).anyMatch(i -> HopperBlockEntity.tryTakeInItemFromSlot(hopper, container, i, direction));
        }
        for (ItemEntity itemEntity : HopperBlockEntity.getItemsAtAndAbove(hopper)) {
            if (!HopperBlockEntity.addItem(hopper, itemEntity)) continue;
            return true;
        }
        return false;
    }

    private static boolean tryTakeInItemFromSlot(Hopper hopper, Container container, int i, Direction direction) {
        ItemStack itemStack = container.getItem(i);
        if (!itemStack.isEmpty() && HopperBlockEntity.canTakeItemFromContainer(container, itemStack, i, direction)) {
            ItemStack itemStack2 = itemStack.copy();
            ItemStack itemStack3 = HopperBlockEntity.addItem(container, hopper, container.removeItem(i, 1), null);
            if (itemStack3.isEmpty()) {
                container.setChanged();
                return true;
            }
            container.setItem(i, itemStack2);
        }
        return false;
    }

    public static boolean addItem(Container container, ItemEntity itemEntity) {
        boolean bl = false;
        ItemStack itemStack = itemEntity.getItem().copy();
        ItemStack itemStack2 = HopperBlockEntity.addItem(null, container, itemStack, null);
        if (itemStack2.isEmpty()) {
            bl = true;
            itemEntity.remove();
        } else {
            itemEntity.setItem(itemStack2);
        }
        return bl;
    }

    public static ItemStack addItem(@Nullable Container container, Container container2, ItemStack itemStack, @Nullable Direction direction) {
        if (container2 instanceof WorldlyContainer && direction != null) {
            WorldlyContainer worldlyContainer = (WorldlyContainer)container2;
            int[] is = worldlyContainer.getSlotsForFace(direction);
            for (int i = 0; i < is.length && !itemStack.isEmpty(); ++i) {
                itemStack = HopperBlockEntity.tryMoveInItem(container, container2, itemStack, is[i], direction);
            }
        } else {
            int j = container2.getContainerSize();
            for (int k = 0; k < j && !itemStack.isEmpty(); ++k) {
                itemStack = HopperBlockEntity.tryMoveInItem(container, container2, itemStack, k, direction);
            }
        }
        return itemStack;
    }

    private static boolean canPlaceItemInContainer(Container container, ItemStack itemStack, int i, @Nullable Direction direction) {
        if (!container.canPlaceItem(i, itemStack)) {
            return false;
        }
        return !(container instanceof WorldlyContainer) || ((WorldlyContainer)container).canPlaceItemThroughFace(i, itemStack, direction);
    }

    private static boolean canTakeItemFromContainer(Container container, ItemStack itemStack, int i, Direction direction) {
        return !(container instanceof WorldlyContainer) || ((WorldlyContainer)container).canTakeItemThroughFace(i, itemStack, direction);
    }

    private static ItemStack tryMoveInItem(@Nullable Container container, Container container2, ItemStack itemStack, int i, @Nullable Direction direction) {
        ItemStack itemStack2 = container2.getItem(i);
        if (HopperBlockEntity.canPlaceItemInContainer(container2, itemStack, i, direction)) {
            int k;
            boolean bl = false;
            boolean bl2 = container2.isEmpty();
            if (itemStack2.isEmpty()) {
                container2.setItem(i, itemStack);
                itemStack = ItemStack.EMPTY;
                bl = true;
            } else if (HopperBlockEntity.canMergeItems(itemStack2, itemStack)) {
                int j = itemStack.getMaxStackSize() - itemStack2.getCount();
                k = Math.min(itemStack.getCount(), j);
                itemStack.shrink(k);
                itemStack2.grow(k);
                boolean bl3 = bl = k > 0;
            }
            if (bl) {
                HopperBlockEntity hopperBlockEntity;
                if (bl2 && container2 instanceof HopperBlockEntity && !(hopperBlockEntity = (HopperBlockEntity)container2).isOnCustomCooldown()) {
                    k = 0;
                    if (container instanceof HopperBlockEntity) {
                        HopperBlockEntity hopperBlockEntity2 = (HopperBlockEntity)container;
                        if (hopperBlockEntity.tickedGameTime >= hopperBlockEntity2.tickedGameTime) {
                            k = 1;
                        }
                    }
                    hopperBlockEntity.setCooldown(8 - k);
                }
                container2.setChanged();
            }
        }
        return itemStack;
    }

    @Nullable
    private Container getAttachedContainer() {
        Direction direction = this.getBlockState().getValue(HopperBlock.FACING);
        return HopperBlockEntity.getContainerAt(this.getLevel(), this.worldPosition.relative(direction));
    }

    @Nullable
    public static Container getSourceContainer(Hopper hopper) {
        return HopperBlockEntity.getContainerAt(hopper.getLevel(), hopper.getLevelX(), hopper.getLevelY() + 1.0, hopper.getLevelZ());
    }

    public static List<ItemEntity> getItemsAtAndAbove(Hopper hopper) {
        return hopper.getSuckShape().toAabbs().stream().flatMap(aABB -> hopper.getLevel().getEntitiesOfClass(ItemEntity.class, aABB.move(hopper.getLevelX() - 0.5, hopper.getLevelY() - 0.5, hopper.getLevelZ() - 0.5), EntitySelector.ENTITY_STILL_ALIVE).stream()).collect(Collectors.toList());
    }

    @Nullable
    public static Container getContainerAt(Level level, BlockPos blockPos) {
        return HopperBlockEntity.getContainerAt(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
    }

    @Nullable
    public static Container getContainerAt(Level level, double d, double e, double f) {
        List<Entity> list;
        BlockEntity blockEntity;
        Container container = null;
        BlockPos blockPos = new BlockPos(d, e, f);
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof WorldlyContainerHolder) {
            container = ((WorldlyContainerHolder)((Object)block)).getContainer(blockState, level, blockPos);
        } else if (block.isEntityBlock() && (blockEntity = level.getBlockEntity(blockPos)) instanceof Container && (container = (Container)((Object)blockEntity)) instanceof ChestBlockEntity && block instanceof ChestBlock) {
            container = ChestBlock.getContainer((ChestBlock)block, blockState, level, blockPos, true);
        }
        if (container == null && !(list = level.getEntities((Entity)null, new AABB(d - 0.5, e - 0.5, f - 0.5, d + 0.5, e + 0.5, f + 0.5), EntitySelector.CONTAINER_ENTITY_SELECTOR)).isEmpty()) {
            container = (Container)((Object)list.get(level.random.nextInt(list.size())));
        }
        return container;
    }

    private static boolean canMergeItems(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack.getItem() != itemStack2.getItem()) {
            return false;
        }
        if (itemStack.getDamageValue() != itemStack2.getDamageValue()) {
            return false;
        }
        if (itemStack.getCount() > itemStack.getMaxStackSize()) {
            return false;
        }
        return ItemStack.tagMatches(itemStack, itemStack2);
    }

    @Override
    public double getLevelX() {
        return (double)this.worldPosition.getX() + 0.5;
    }

    @Override
    public double getLevelY() {
        return (double)this.worldPosition.getY() + 0.5;
    }

    @Override
    public double getLevelZ() {
        return (double)this.worldPosition.getZ() + 0.5;
    }

    private void setCooldown(int i) {
        this.cooldownTime = i;
    }

    private boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    private boolean isOnCustomCooldown() {
        return this.cooldownTime > 8;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    public void entityInside(Entity entity) {
        if (entity instanceof ItemEntity) {
            BlockPos blockPos = this.getBlockPos();
            if (Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ())), this.getSuckShape(), BooleanOp.AND)) {
                this.tryMoveItems(() -> HopperBlockEntity.addItem(this, (ItemEntity)entity));
            }
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new HopperMenu(i, inventory, this);
    }
}

