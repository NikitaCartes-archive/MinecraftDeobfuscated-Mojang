/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;

@EnvironmentInterfaces(value={@EnvironmentInterface(value=EnvType.CLIENT, itf=LidBlockEntity.class)})
public class ChestBlockEntity
extends RandomizableContainerBlockEntity
implements LidBlockEntity,
TickableBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    protected float openness;
    protected float oOpenness;
    protected int openCount;
    private int tickInterval;

    protected ChestBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    public ChestBlockEntity() {
        this(BlockEntityType.CHEST);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.items) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.chest", new Object[0]);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.items);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (!this.trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.items);
        }
        return compoundTag;
    }

    @Override
    public void tick() {
        int i = this.worldPosition.getX();
        int j = this.worldPosition.getY();
        int k = this.worldPosition.getZ();
        ++this.tickInterval;
        this.openCount = ChestBlockEntity.getOpenCount(this.level, this, this.tickInterval, i, j, k, this.openCount);
        this.oOpenness = this.openness;
        float f = 0.1f;
        if (this.openCount > 0 && this.openness == 0.0f) {
            this.playSound(SoundEvents.CHEST_OPEN);
        }
        if (this.openCount == 0 && this.openness > 0.0f || this.openCount > 0 && this.openness < 1.0f) {
            float g = this.openness;
            this.openness = this.openCount > 0 ? (this.openness += 0.1f) : (this.openness -= 0.1f);
            if (this.openness > 1.0f) {
                this.openness = 1.0f;
            }
            float h = 0.5f;
            if (this.openness < 0.5f && g >= 0.5f) {
                this.playSound(SoundEvents.CHEST_CLOSE);
            }
            if (this.openness < 0.0f) {
                this.openness = 0.0f;
            }
        }
    }

    public static int getOpenCount(Level level, BaseContainerBlockEntity baseContainerBlockEntity, int i, int j, int k, int l, int m) {
        if (!level.isClientSide && m != 0 && (i + j + k + l) % 200 == 0) {
            m = ChestBlockEntity.getOpenCount(level, baseContainerBlockEntity, j, k, l);
        }
        return m;
    }

    public static int getOpenCount(Level level, BaseContainerBlockEntity baseContainerBlockEntity, int i, int j, int k) {
        int l = 0;
        float f = 5.0f;
        List<Player> list = level.getEntitiesOfClass(Player.class, new AABB((float)i - 5.0f, (float)j - 5.0f, (float)k - 5.0f, (float)(i + 1) + 5.0f, (float)(j + 1) + 5.0f, (float)(k + 1) + 5.0f));
        for (Player player : list) {
            Container container;
            if (!(player.containerMenu instanceof ChestMenu) || (container = ((ChestMenu)player.containerMenu).getContainer()) != baseContainerBlockEntity && (!(container instanceof CompoundContainer) || !((CompoundContainer)container).contains(baseContainerBlockEntity))) continue;
            ++l;
        }
        return l;
    }

    private void playSound(SoundEvent soundEvent) {
        ChestType chestType = this.getBlockState().getValue(ChestBlock.TYPE);
        if (chestType == ChestType.LEFT) {
            return;
        }
        double d = (double)this.worldPosition.getX() + 0.5;
        double e = (double)this.worldPosition.getY() + 0.5;
        double f = (double)this.worldPosition.getZ() + 0.5;
        if (chestType == ChestType.RIGHT) {
            Direction direction = ChestBlock.getConnectedDirection(this.getBlockState());
            d += (double)direction.getStepX() * 0.5;
            f += (double)direction.getStepZ() * 0.5;
        }
        this.level.playSound(null, d, e, f, soundEvent, SoundSource.BLOCKS, 0.5f, this.level.random.nextFloat() * 0.1f + 0.9f);
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            this.openCount = j;
            return true;
        }
        return super.triggerEvent(i, j);
    }

    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }
            ++this.openCount;
            this.signalOpenCount();
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            --this.openCount;
            this.signalOpenCount();
        }
    }

    protected void signalOpenCount() {
        Block block = this.getBlockState().getBlock();
        if (block instanceof ChestBlock) {
            this.level.blockEvent(this.worldPosition, block, 1, this.openCount);
            this.level.updateNeighborsAt(this.worldPosition, block);
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public float getOpenNess(float f) {
        return Mth.lerp(f, this.oOpenness, this.openness);
    }

    public static int getOpenCount(BlockGetter blockGetter, BlockPos blockPos) {
        BlockEntity blockEntity;
        BlockState blockState = blockGetter.getBlockState(blockPos);
        if (blockState.getBlock().isEntityBlock() && (blockEntity = blockGetter.getBlockEntity(blockPos)) instanceof ChestBlockEntity) {
            return ((ChestBlockEntity)blockEntity).openCount;
        }
        return 0;
    }

    public static void swapContents(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2) {
        NonNullList<ItemStack> nonNullList = chestBlockEntity.getItems();
        chestBlockEntity.setItems(chestBlockEntity2.getItems());
        chestBlockEntity2.setItems(nonNullList);
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return ChestMenu.threeRows(i, inventory, this);
    }
}

