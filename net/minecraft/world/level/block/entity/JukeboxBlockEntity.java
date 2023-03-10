/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;

public class JukeboxBlockEntity
extends BlockEntity
implements Clearable,
ContainerSingleItem {
    private static final int SONG_END_PADDING = 20;
    private final NonNullList<ItemStack> items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    private int ticksSinceLastEvent;
    private long tickCount;
    private long recordStartedTick;
    private boolean isPlaying;

    public JukeboxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.JUKEBOX, blockPos, blockState);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        if (compoundTag.contains("RecordItem", 10)) {
            this.items.set(0, ItemStack.of(compoundTag.getCompound("RecordItem")));
        }
        this.isPlaying = compoundTag.getBoolean("IsPlaying");
        this.recordStartedTick = compoundTag.getLong("RecordStartTick");
        this.tickCount = compoundTag.getLong("TickCount");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        if (!this.getFirstItem().isEmpty()) {
            compoundTag.put("RecordItem", this.getFirstItem().save(new CompoundTag()));
        }
        compoundTag.putBoolean("IsPlaying", this.isPlaying);
        compoundTag.putLong("RecordStartTick", this.recordStartedTick);
        compoundTag.putLong("TickCount", this.tickCount);
    }

    public boolean isRecordPlaying() {
        return !this.getFirstItem().isEmpty() && this.isPlaying;
    }

    private void setHasRecordBlockState(@Nullable Entity entity, boolean bl) {
        if (this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(JukeboxBlock.HAS_RECORD, bl), 2);
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(entity, this.getBlockState()));
        }
    }

    @VisibleForTesting
    public void startPlaying() {
        this.recordStartedTick = this.tickCount;
        this.isPlaying = true;
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.level.levelEvent(null, 1010, this.getBlockPos(), Item.getId(this.getFirstItem().getItem()));
        this.setChanged();
    }

    private void stopPlaying() {
        this.isPlaying = false;
        this.level.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.level.levelEvent(1011, this.getBlockPos(), 0);
        this.setChanged();
    }

    private void tick(Level level, BlockPos blockPos, BlockState blockState) {
        Item item;
        ++this.ticksSinceLastEvent;
        if (this.isRecordPlaying() && (item = this.getFirstItem().getItem()) instanceof RecordItem) {
            RecordItem recordItem = (RecordItem)item;
            if (this.shouldRecordStopPlaying(recordItem)) {
                this.stopPlaying();
            } else if (this.shouldSendJukeboxPlayingEvent()) {
                this.ticksSinceLastEvent = 0;
                level.gameEvent(GameEvent.JUKEBOX_PLAY, blockPos, GameEvent.Context.of(blockState));
                this.spawnMusicParticles(level, blockPos);
            }
        }
        ++this.tickCount;
    }

    private boolean shouldRecordStopPlaying(RecordItem recordItem) {
        return this.tickCount >= this.recordStartedTick + (long)recordItem.getLengthInTicks() + 20L;
    }

    private boolean shouldSendJukeboxPlayingEvent() {
        return this.ticksSinceLastEvent >= 20;
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
            this.setHasRecordBlockState(null, false);
            this.stopPlaying();
        }
        return itemStack;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        if (itemStack.is(ItemTags.MUSIC_DISCS) && this.level != null) {
            this.items.set(i, itemStack);
            this.setHasRecordBlockState(null, true);
            this.startPlaying();
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
        return itemStack.is(ItemTags.MUSIC_DISCS) && this.getItem(i).isEmpty();
    }

    @Override
    public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
        return container.hasAnyMatching(ItemStack::isEmpty);
    }

    private void spawnMusicParticles(Level level, BlockPos blockPos) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Vec3 vec3 = Vec3.atBottomCenterOf(blockPos).add(0.0, 1.2f, 0.0);
            float f = (float)level.getRandom().nextInt(4) / 24.0f;
            serverLevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, f, 0.0, 0.0, 1.0);
        }
    }

    public void popOutRecord() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        BlockPos blockPos = this.getBlockPos();
        ItemStack itemStack = this.getFirstItem();
        if (itemStack.isEmpty()) {
            return;
        }
        this.removeFirstItem();
        Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockPos, 0.5, 1.01, 0.5).offsetRandom(this.level.random, 0.7f);
        ItemStack itemStack2 = itemStack.copy();
        ItemEntity itemEntity = new ItemEntity(this.level, vec3.x(), vec3.y(), vec3.z(), itemStack2);
        itemEntity.setDefaultPickUpDelay();
        this.level.addFreshEntity(itemEntity);
    }

    public static void playRecordTick(Level level, BlockPos blockPos, BlockState blockState, JukeboxBlockEntity jukeboxBlockEntity) {
        jukeboxBlockEntity.tick(level, blockPos, blockState);
    }

    @VisibleForTesting
    public void setRecordWithoutPlaying(ItemStack itemStack) {
        this.items.set(0, itemStack);
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.setChanged();
    }
}

