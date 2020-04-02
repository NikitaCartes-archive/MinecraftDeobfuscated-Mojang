/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class UseBonemeal
extends Behavior<Villager> {
    private long nextWorkCycleTime;
    private long lastBonemealingSession;
    private int timeWorkedSoFar;
    private Optional<BlockPos> cropPos = Optional.empty();

    public UseBonemeal() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
        if (villager.tickCount % 10 != 0 || this.lastBonemealingSession != 0L && this.lastBonemealingSession + 160L > (long)villager.tickCount) {
            return false;
        }
        if (villager.getInventory().countItem(Items.BONE_MEAL) <= 0) {
            return false;
        }
        this.cropPos = this.pickNextTarget(serverLevel, villager);
        return this.cropPos.isPresent();
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
        return this.timeWorkedSoFar < 80 && this.cropPos.isPresent();
    }

    private Optional<BlockPos> pickNextTarget(ServerLevel serverLevel, Villager villager) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        Optional<BlockPos> optional = Optional.empty();
        int i = 0;
        for (int j = -1; j <= 1; ++j) {
            for (int k = -1; k <= 1; ++k) {
                for (int l = -1; l <= 1; ++l) {
                    mutableBlockPos.setWithOffset(villager.blockPosition(), j, k, l);
                    if (!this.validPos(mutableBlockPos, serverLevel) || serverLevel.random.nextInt(++i) != 0) continue;
                    optional = Optional.of(mutableBlockPos.immutable());
                }
            }
        }
        return optional;
    }

    private boolean validPos(BlockPos blockPos, ServerLevel serverLevel) {
        BlockState blockState = serverLevel.getBlockState(blockPos);
        Block block = blockState.getBlock();
        return block instanceof CropBlock && !((CropBlock)block).isMaxAge(blockState);
    }

    @Override
    protected void start(ServerLevel serverLevel, Villager villager, long l) {
        this.setCurrentCropAsTarget(villager);
        villager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BONE_MEAL));
        this.nextWorkCycleTime = l;
        this.timeWorkedSoFar = 0;
    }

    private void setCurrentCropAsTarget(Villager villager) {
        this.cropPos.ifPresent(blockPos -> {
            BlockPosTracker blockPosTracker = new BlockPosTracker((BlockPos)blockPos);
            villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, blockPosTracker);
            villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPosTracker, 0.5f, 1));
        });
    }

    @Override
    protected void stop(ServerLevel serverLevel, Villager villager, long l) {
        villager.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.lastBonemealingSession = villager.tickCount;
    }

    @Override
    protected void tick(ServerLevel serverLevel, Villager villager, long l) {
        BlockPos blockPos = this.cropPos.get();
        if (l < this.nextWorkCycleTime || !blockPos.closerThan(villager.position(), 1.0)) {
            return;
        }
        ItemStack itemStack = ItemStack.EMPTY;
        SimpleContainer simpleContainer = villager.getInventory();
        int i = simpleContainer.getContainerSize();
        for (int j = 0; j < i; ++j) {
            ItemStack itemStack2 = simpleContainer.getItem(j);
            if (itemStack2.getItem() != Items.BONE_MEAL) continue;
            itemStack = itemStack2;
            break;
        }
        if (!itemStack.isEmpty() && BoneMealItem.growCrop(itemStack, serverLevel, blockPos)) {
            serverLevel.levelEvent(2005, blockPos, 0);
            this.cropPos = this.pickNextTarget(serverLevel, villager);
            this.setCurrentCropAsTarget(villager);
            this.nextWorkCycleTime = l + 40L;
        }
        ++this.timeWorkedSoFar;
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}

