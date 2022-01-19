/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

public class InteractWithDoor
extends Behavior<LivingEntity> {
    private static final int COOLDOWN_BEFORE_RERUNNING_IN_SAME_NODE = 20;
    private static final double SKIP_CLOSING_DOOR_IF_FURTHER_AWAY_THAN = 2.0;
    private static final double MAX_DISTANCE_TO_HOLD_DOOR_OPEN_FOR_OTHER_MOBS = 2.0;
    @Nullable
    private Node lastCheckedNode;
    private int remainingCooldown;

    public InteractWithDoor() {
        super(ImmutableMap.of(MemoryModuleType.PATH, MemoryStatus.VALUE_PRESENT, MemoryModuleType.DOORS_TO_CLOSE, MemoryStatus.REGISTERED));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
        Path path = livingEntity.getBrain().getMemory(MemoryModuleType.PATH).get();
        if (path.notStarted() || path.isDone()) {
            return false;
        }
        if (!Objects.equals(this.lastCheckedNode, path.getNextNode())) {
            this.remainingCooldown = 20;
            return true;
        }
        if (this.remainingCooldown > 0) {
            --this.remainingCooldown;
        }
        return this.remainingCooldown == 0;
    }

    @Override
    protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        DoorBlock doorBlock2;
        BlockPos blockPos2;
        BlockState blockState2;
        Path path = livingEntity.getBrain().getMemory(MemoryModuleType.PATH).get();
        this.lastCheckedNode = path.getNextNode();
        Node node = path.getPreviousNode();
        Node node2 = path.getNextNode();
        BlockPos blockPos = node.asBlockPos();
        BlockState blockState = serverLevel.getBlockState(blockPos);
        if (blockState.is(BlockTags.WOODEN_DOORS, blockStateBase -> blockStateBase.getBlock() instanceof DoorBlock)) {
            DoorBlock doorBlock = (DoorBlock)blockState.getBlock();
            if (!doorBlock.isOpen(blockState)) {
                doorBlock.setOpen(livingEntity, serverLevel, blockState, blockPos, true);
            }
            this.rememberDoorToClose(serverLevel, livingEntity, blockPos);
        }
        if ((blockState2 = serverLevel.getBlockState(blockPos2 = node2.asBlockPos())).is(BlockTags.WOODEN_DOORS, blockStateBase -> blockStateBase.getBlock() instanceof DoorBlock) && !(doorBlock2 = (DoorBlock)blockState2.getBlock()).isOpen(blockState2)) {
            doorBlock2.setOpen(livingEntity, serverLevel, blockState2, blockPos2, true);
            this.rememberDoorToClose(serverLevel, livingEntity, blockPos2);
        }
        InteractWithDoor.closeDoorsThatIHaveOpenedOrPassedThrough(serverLevel, livingEntity, node, node2);
    }

    public static void closeDoorsThatIHaveOpenedOrPassedThrough(ServerLevel serverLevel, LivingEntity livingEntity, @Nullable Node node, @Nullable Node node2) {
        Brain<Set<GlobalPos>> brain = livingEntity.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.DOORS_TO_CLOSE)) {
            Iterator<GlobalPos> iterator = brain.getMemory(MemoryModuleType.DOORS_TO_CLOSE).get().iterator();
            while (iterator.hasNext()) {
                GlobalPos globalPos = iterator.next();
                BlockPos blockPos = globalPos.pos();
                if (node != null && node.asBlockPos().equals(blockPos) || node2 != null && node2.asBlockPos().equals(blockPos)) continue;
                if (InteractWithDoor.isDoorTooFarAway(serverLevel, livingEntity, globalPos)) {
                    iterator.remove();
                    continue;
                }
                BlockState blockState = serverLevel.getBlockState(blockPos);
                if (!blockState.is(BlockTags.WOODEN_DOORS, blockStateBase -> blockStateBase.getBlock() instanceof DoorBlock)) {
                    iterator.remove();
                    continue;
                }
                DoorBlock doorBlock = (DoorBlock)blockState.getBlock();
                if (!doorBlock.isOpen(blockState)) {
                    iterator.remove();
                    continue;
                }
                if (InteractWithDoor.areOtherMobsComingThroughDoor(serverLevel, livingEntity, blockPos)) {
                    iterator.remove();
                    continue;
                }
                doorBlock.setOpen(livingEntity, serverLevel, blockState, blockPos, false);
                iterator.remove();
            }
        }
    }

    private static boolean areOtherMobsComingThroughDoor(ServerLevel serverLevel, LivingEntity livingEntity3, BlockPos blockPos) {
        Brain<List<LivingEntity>> brain = livingEntity3.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.NEAREST_LIVING_ENTITIES)) {
            return false;
        }
        return brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get().stream().filter(livingEntity2 -> livingEntity2.getType() == livingEntity3.getType()).filter(livingEntity -> blockPos.closerThan(livingEntity.position(), 2.0)).anyMatch(livingEntity -> InteractWithDoor.isMobComingThroughDoor(serverLevel, livingEntity, blockPos));
    }

    private static boolean isMobComingThroughDoor(ServerLevel serverLevel, LivingEntity livingEntity, BlockPos blockPos) {
        if (!livingEntity.getBrain().hasMemoryValue(MemoryModuleType.PATH)) {
            return false;
        }
        Path path = livingEntity.getBrain().getMemory(MemoryModuleType.PATH).get();
        if (path.isDone()) {
            return false;
        }
        Node node = path.getPreviousNode();
        if (node == null) {
            return false;
        }
        Node node2 = path.getNextNode();
        return blockPos.equals(node.asBlockPos()) || blockPos.equals(node2.asBlockPos());
    }

    private static boolean isDoorTooFarAway(ServerLevel serverLevel, LivingEntity livingEntity, GlobalPos globalPos) {
        return globalPos.dimension() != serverLevel.dimension() || !globalPos.pos().closerThan(livingEntity.position(), 2.0);
    }

    private void rememberDoorToClose(ServerLevel serverLevel, LivingEntity livingEntity, BlockPos blockPos) {
        Brain<?> brain = livingEntity.getBrain();
        GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), blockPos);
        if (brain.getMemory(MemoryModuleType.DOORS_TO_CLOSE).isPresent()) {
            brain.getMemory(MemoryModuleType.DOORS_TO_CLOSE).get().add(globalPos);
        } else {
            brain.setMemory(MemoryModuleType.DOORS_TO_CLOSE, Sets.newHashSet(globalPos));
        }
    }
}

