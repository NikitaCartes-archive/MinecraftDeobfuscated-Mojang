package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.OptionalBox.Mu;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

public class InteractWithDoor {
	private static final int COOLDOWN_BEFORE_RERUNNING_IN_SAME_NODE = 20;
	private static final double SKIP_CLOSING_DOOR_IF_FURTHER_AWAY_THAN = 3.0;
	private static final double MAX_DISTANCE_TO_HOLD_DOOR_OPEN_FOR_OTHER_MOBS = 2.0;

	public static BehaviorControl<LivingEntity> create() {
		MutableObject<Node> mutableObject = new MutableObject<>(null);
		MutableInt mutableInt = new MutableInt(0);
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.present(MemoryModuleType.PATH),
						instance.registered(MemoryModuleType.DOORS_TO_CLOSE),
						instance.registered(MemoryModuleType.NEAREST_LIVING_ENTITIES)
					)
					.apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, livingEntity, l) -> {
							Path path = instance.get(memoryAccessor);
							Optional<Set<GlobalPos>> optional = instance.tryGet(memoryAccessor2);
							if (!path.notStarted() && !path.isDone()) {
								if (Objects.equals(mutableObject.getValue(), path.getNextNode())) {
									mutableInt.setValue(20);
								} else if (mutableInt.decrementAndGet() > 0) {
									return false;
								}

								mutableObject.setValue(path.getNextNode());
								Node node = path.getPreviousNode();
								Node node2 = path.getNextNode();
								BlockPos blockPos = node.asBlockPos();
								BlockState blockState = serverLevel.getBlockState(blockPos);
								if (blockState.is(BlockTags.WOODEN_DOORS, blockStateBase -> blockStateBase.getBlock() instanceof DoorBlock)) {
									DoorBlock doorBlock = (DoorBlock)blockState.getBlock();
									if (!doorBlock.isOpen(blockState)) {
										doorBlock.setOpen(livingEntity, serverLevel, blockState, blockPos, true);
									}

									optional = rememberDoorToClose(memoryAccessor2, optional, serverLevel, blockPos);
								}

								BlockPos blockPos2 = node2.asBlockPos();
								BlockState blockState2 = serverLevel.getBlockState(blockPos2);
								if (blockState2.is(BlockTags.WOODEN_DOORS, blockStateBase -> blockStateBase.getBlock() instanceof DoorBlock)) {
									DoorBlock doorBlock2 = (DoorBlock)blockState2.getBlock();
									if (!doorBlock2.isOpen(blockState2)) {
										doorBlock2.setOpen(livingEntity, serverLevel, blockState2, blockPos2, true);
										optional = rememberDoorToClose(memoryAccessor2, optional, serverLevel, blockPos2);
									}
								}

								optional.ifPresent(set -> closeDoorsThatIHaveOpenedOrPassedThrough(serverLevel, livingEntity, node, node2, set, instance.tryGet(memoryAccessor3)));
								return true;
							} else {
								return false;
							}
						})
		);
	}

	public static void closeDoorsThatIHaveOpenedOrPassedThrough(
		ServerLevel serverLevel, LivingEntity livingEntity, @Nullable Node node, @Nullable Node node2, Set<GlobalPos> set, Optional<List<LivingEntity>> optional
	) {
		Iterator<GlobalPos> iterator = set.iterator();

		while (iterator.hasNext()) {
			GlobalPos globalPos = (GlobalPos)iterator.next();
			BlockPos blockPos = globalPos.pos();
			if ((node == null || !node.asBlockPos().equals(blockPos)) && (node2 == null || !node2.asBlockPos().equals(blockPos))) {
				if (isDoorTooFarAway(serverLevel, livingEntity, globalPos)) {
					iterator.remove();
				} else {
					BlockState blockState = serverLevel.getBlockState(blockPos);
					if (!blockState.is(BlockTags.WOODEN_DOORS, blockStateBase -> blockStateBase.getBlock() instanceof DoorBlock)) {
						iterator.remove();
					} else {
						DoorBlock doorBlock = (DoorBlock)blockState.getBlock();
						if (!doorBlock.isOpen(blockState)) {
							iterator.remove();
						} else if (areOtherMobsComingThroughDoor(livingEntity, blockPos, optional)) {
							iterator.remove();
						} else {
							doorBlock.setOpen(livingEntity, serverLevel, blockState, blockPos, false);
							iterator.remove();
						}
					}
				}
			}
		}
	}

	private static boolean areOtherMobsComingThroughDoor(LivingEntity livingEntity, BlockPos blockPos, Optional<List<LivingEntity>> optional) {
		return optional.isEmpty()
			? false
			: ((List)optional.get())
				.stream()
				.filter(livingEntity2 -> livingEntity2.getType() == livingEntity.getType())
				.filter(livingEntityx -> blockPos.closerToCenterThan(livingEntityx.position(), 2.0))
				.anyMatch(livingEntityx -> isMobComingThroughDoor(livingEntityx.getBrain(), blockPos));
	}

	private static boolean isMobComingThroughDoor(Brain<?> brain, BlockPos blockPos) {
		if (!brain.hasMemoryValue(MemoryModuleType.PATH)) {
			return false;
		} else {
			Path path = (Path)brain.getMemory(MemoryModuleType.PATH).get();
			if (path.isDone()) {
				return false;
			} else {
				Node node = path.getPreviousNode();
				if (node == null) {
					return false;
				} else {
					Node node2 = path.getNextNode();
					return blockPos.equals(node.asBlockPos()) || blockPos.equals(node2.asBlockPos());
				}
			}
		}
	}

	private static boolean isDoorTooFarAway(ServerLevel serverLevel, LivingEntity livingEntity, GlobalPos globalPos) {
		return globalPos.dimension() != serverLevel.dimension() || !globalPos.pos().closerToCenterThan(livingEntity.position(), 3.0);
	}

	private static Optional<Set<GlobalPos>> rememberDoorToClose(
		MemoryAccessor<Mu, Set<GlobalPos>> memoryAccessor, Optional<Set<GlobalPos>> optional, ServerLevel serverLevel, BlockPos blockPos
	) {
		GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), blockPos);
		return Optional.of((Set)optional.map(set -> {
			set.add(globalPos);
			return set;
		}).orElseGet(() -> {
			Set<GlobalPos> set = Sets.<GlobalPos>newHashSet(globalPos);
			memoryAccessor.set(set);
			return set;
		}));
	}
}
