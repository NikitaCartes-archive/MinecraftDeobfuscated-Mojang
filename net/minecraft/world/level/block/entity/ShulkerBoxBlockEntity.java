/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

public class ShulkerBoxBlockEntity
extends RandomizableContainerBlockEntity
implements WorldlyContainer {
    private static final int[] SLOTS = IntStream.range(0, 27).toArray();
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    private int openCount;
    private AnimationStatus animationStatus = AnimationStatus.CLOSED;
    private float progress;
    private float progressOld;
    @Nullable
    private final DyeColor color;

    public ShulkerBoxBlockEntity(@Nullable DyeColor dyeColor, BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.SHULKER_BOX, blockPos, blockState);
        this.color = dyeColor;
    }

    public ShulkerBoxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.SHULKER_BOX, blockPos, blockState);
        this.color = ShulkerBoxBlock.getColorFromBlock(blockState.getBlock());
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
        ShulkerBoxBlockEntity.updateAnimation(level, blockPos, blockState, shulkerBoxBlockEntity);
        if (shulkerBoxBlockEntity.animationStatus == AnimationStatus.OPENING || shulkerBoxBlockEntity.animationStatus == AnimationStatus.CLOSING) {
            ShulkerBoxBlockEntity.moveCollidedEntities(level, blockPos, blockState, shulkerBoxBlockEntity.getProgress(1.0f));
        }
    }

    private static void updateAnimation(Level level, BlockPos blockPos, BlockState blockState, ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
        shulkerBoxBlockEntity.progressOld = shulkerBoxBlockEntity.progress;
        switch (shulkerBoxBlockEntity.animationStatus) {
            case CLOSED: {
                shulkerBoxBlockEntity.progress = 0.0f;
                break;
            }
            case OPENING: {
                shulkerBoxBlockEntity.progress += 0.1f;
                if (!(shulkerBoxBlockEntity.progress >= 1.0f)) break;
                ShulkerBoxBlockEntity.moveCollidedEntities(level, blockPos, blockState, shulkerBoxBlockEntity.getProgress(1.0f));
                shulkerBoxBlockEntity.animationStatus = AnimationStatus.OPENED;
                shulkerBoxBlockEntity.progress = 1.0f;
                ShulkerBoxBlockEntity.doNeighborUpdates(level, blockPos, blockState);
                break;
            }
            case CLOSING: {
                shulkerBoxBlockEntity.progress -= 0.1f;
                if (!(shulkerBoxBlockEntity.progress <= 0.0f)) break;
                shulkerBoxBlockEntity.animationStatus = AnimationStatus.CLOSED;
                shulkerBoxBlockEntity.progress = 0.0f;
                ShulkerBoxBlockEntity.doNeighborUpdates(level, blockPos, blockState);
                break;
            }
            case OPENED: {
                shulkerBoxBlockEntity.progress = 1.0f;
            }
        }
    }

    public AnimationStatus getAnimationStatus() {
        return this.animationStatus;
    }

    public AABB getBoundingBox(BlockState blockState) {
        return ShulkerBoxBlockEntity.getBoundingBox(blockState.getValue(ShulkerBoxBlock.FACING), this.getProgress(1.0f));
    }

    public static AABB getBoundingBox(Direction direction, float f) {
        return Shapes.block().bounds().expandTowards(0.5f * f * (float)direction.getStepX(), 0.5f * f * (float)direction.getStepY(), 0.5f * f * (float)direction.getStepZ());
    }

    private static AABB getTopBoundingBox(Direction direction, float f) {
        Direction direction2 = direction.getOpposite();
        return ShulkerBoxBlockEntity.getBoundingBox(direction, f).contract(direction2.getStepX(), direction2.getStepY(), direction2.getStepZ());
    }

    private static void moveCollidedEntities(Level level, BlockPos blockPos, BlockState blockState, float f) {
        if (!(blockState.getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }
        Direction direction = blockState.getValue(ShulkerBoxBlock.FACING);
        AABB aABB = ShulkerBoxBlockEntity.getTopBoundingBox(direction, f).move(blockPos);
        List<Entity> list = level.getEntities(null, aABB);
        if (list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size(); ++i) {
            Entity entity = list.get(i);
            if (entity.getPistonPushReaction() == PushReaction.IGNORE) continue;
            double d = 0.0;
            double e = 0.0;
            double g = 0.0;
            AABB aABB2 = entity.getBoundingBox();
            switch (direction.getAxis()) {
                case X: {
                    d = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? aABB.maxX - aABB2.minX : aABB2.maxX - aABB.minX;
                    d += 0.01;
                    break;
                }
                case Y: {
                    e = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? aABB.maxY - aABB2.minY : aABB2.maxY - aABB.minY;
                    e += 0.01;
                    break;
                }
                case Z: {
                    g = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? aABB.maxZ - aABB2.minZ : aABB2.maxZ - aABB.minZ;
                    g += 0.01;
                }
            }
            entity.move(MoverType.SHULKER_BOX, new Vec3(d * (double)direction.getStepX(), e * (double)direction.getStepY(), g * (double)direction.getStepZ()));
        }
    }

    @Override
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            this.openCount = j;
            if (j == 0) {
                this.animationStatus = AnimationStatus.CLOSING;
                ShulkerBoxBlockEntity.doNeighborUpdates(this.getLevel(), this.worldPosition, this.getBlockState());
            }
            if (j == 1) {
                this.animationStatus = AnimationStatus.OPENING;
                ShulkerBoxBlockEntity.doNeighborUpdates(this.getLevel(), this.worldPosition, this.getBlockState());
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    private static void doNeighborUpdates(Level level, BlockPos blockPos, BlockState blockState) {
        blockState.updateNeighbourShapes(level, blockPos, 3);
    }

    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }
            ++this.openCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount == 1) {
                this.level.playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5f, this.level.random.nextFloat() * 0.1f + 0.9f);
            }
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            --this.openCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount <= 0) {
                this.level.playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5f, this.level.random.nextFloat() * 0.1f + 0.9f);
            }
        }
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.shulkerBox");
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.loadFromTag(compoundTag);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        return this.saveToTag(compoundTag);
    }

    public void loadFromTag(CompoundTag compoundTag) {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compoundTag) && compoundTag.contains("Items", 9)) {
            ContainerHelper.loadAllItems(compoundTag, this.itemStacks);
        }
    }

    public CompoundTag saveToTag(CompoundTag compoundTag) {
        if (!this.trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.itemStacks, false);
        }
        return compoundTag;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.itemStacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.itemStacks = nonNullList;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return !(Block.byItem(itemStack.getItem()) instanceof ShulkerBoxBlock);
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return true;
    }

    public float getProgress(float f) {
        return Mth.lerp(f, this.progressOld, this.progress);
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public DyeColor getColor() {
        return this.color;
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new ShulkerBoxMenu(i, inventory, this);
    }

    public boolean isClosed() {
        return this.animationStatus == AnimationStatus.CLOSED;
    }

    public static enum AnimationStatus {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING;

    }
}

