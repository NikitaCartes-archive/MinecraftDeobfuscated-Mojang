package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

public class InteractWithDoor extends Behavior<LivingEntity> {
	public InteractWithDoor() {
		super(
			ImmutableMap.of(
				MemoryModuleType.PATH,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.INTERACTABLE_DOORS,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.OPENED_DOORS,
				MemoryStatus.REGISTERED
			)
		);
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		Brain<?> brain = livingEntity.getBrain();
		Path path = (Path)brain.getMemory(MemoryModuleType.PATH).get();
		List<GlobalPos> list = (List<GlobalPos>)brain.getMemory(MemoryModuleType.INTERACTABLE_DOORS).get();
		List<BlockPos> list2 = (List<BlockPos>)path.getNodes().stream().map(node -> new BlockPos(node.x, node.y, node.z)).collect(Collectors.toList());
		Set<BlockPos> set = this.getDoorsThatAreOnMyPath(serverLevel, list, list2);
		int i = path.getIndex() - 1;
		this.openOrCloseDoors(serverLevel, list2, set, i, livingEntity, brain);
	}

	private Set<BlockPos> getDoorsThatAreOnMyPath(ServerLevel serverLevel, List<GlobalPos> list, List<BlockPos> list2) {
		return (Set<BlockPos>)list.stream()
			.filter(globalPos -> globalPos.dimension() == serverLevel.dimensionType())
			.map(GlobalPos::pos)
			.filter(list2::contains)
			.collect(Collectors.toSet());
	}

	private void openOrCloseDoors(ServerLevel serverLevel, List<BlockPos> list, Set<BlockPos> set, int i, LivingEntity livingEntity, Brain<?> brain) {
		set.forEach(blockPos -> {
			int j = list.indexOf(blockPos);
			BlockState blockState = serverLevel.getBlockState(blockPos);
			Block block = blockState.getBlock();
			if (BlockTags.WOODEN_DOORS.contains(block) && block instanceof DoorBlock) {
				boolean bl = j >= i;
				((DoorBlock)block).setOpen(serverLevel, blockPos, bl);
				GlobalPos globalPos = GlobalPos.of(serverLevel.dimensionType(), blockPos);
				if (!brain.getMemory(MemoryModuleType.OPENED_DOORS).isPresent() && bl) {
					brain.setMemory(MemoryModuleType.OPENED_DOORS, Sets.<GlobalPos>newHashSet(globalPos));
				} else {
					brain.getMemory(MemoryModuleType.OPENED_DOORS).ifPresent(setx -> {
						if (bl) {
							setx.add(globalPos);
						} else {
							setx.remove(globalPos);
						}
					});
				}
			}
		});
		closeAllOpenedDoors(serverLevel, list, i, livingEntity, brain);
	}

	public static void closeAllOpenedDoors(ServerLevel serverLevel, List<BlockPos> list, int i, LivingEntity livingEntity, Brain<?> brain) {
		brain.getMemory(MemoryModuleType.OPENED_DOORS).ifPresent(set -> {
			Iterator<GlobalPos> iterator = set.iterator();

			while (iterator.hasNext()) {
				GlobalPos globalPos = (GlobalPos)iterator.next();
				BlockPos blockPos = globalPos.pos();
				int j = list.indexOf(blockPos);
				if (serverLevel.dimensionType() != globalPos.dimension()) {
					iterator.remove();
				} else {
					BlockState blockState = serverLevel.getBlockState(blockPos);
					Block block = blockState.getBlock();
					if (BlockTags.WOODEN_DOORS.contains(block) && block instanceof DoorBlock && j < i && blockPos.closerThan(livingEntity.position(), 4.0)) {
						((DoorBlock)block).setOpen(serverLevel, blockPos, false);
						iterator.remove();
					}
				}
			}
		});
	}
}
