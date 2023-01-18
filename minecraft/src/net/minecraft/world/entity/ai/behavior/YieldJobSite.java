package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.pathfinder.Path;

public class YieldJobSite {
	public static BehaviorControl<Villager> create(float f) {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.present(MemoryModuleType.POTENTIAL_JOB_SITE),
						instance.absent(MemoryModuleType.JOB_SITE),
						instance.present(MemoryModuleType.NEAREST_LIVING_ENTITIES),
						instance.registered(MemoryModuleType.WALK_TARGET),
						instance.registered(MemoryModuleType.LOOK_TARGET)
					)
					.apply(
						instance,
						(memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4, memoryAccessor5) -> (serverLevel, villager, l) -> {
								if (villager.isBaby()) {
									return false;
								} else if (villager.getVillagerData().getProfession() != VillagerProfession.NONE) {
									return false;
								} else {
									BlockPos blockPos = instance.<GlobalPos>get(memoryAccessor).pos();
									Optional<Holder<PoiType>> optional = serverLevel.getPoiManager().getType(blockPos);
									if (optional.isEmpty()) {
										return true;
									} else {
										instance.<List>get(memoryAccessor3)
											.stream()
											.filter(livingEntity -> livingEntity instanceof Villager && livingEntity != villager)
											.map(livingEntity -> (Villager)livingEntity)
											.filter(LivingEntity::isAlive)
											.filter(villagerx -> nearbyWantsJobsite((Holder<PoiType>)optional.get(), villagerx, blockPos))
											.findFirst()
											.ifPresent(villagerx -> {
												memoryAccessor4.erase();
												memoryAccessor5.erase();
												memoryAccessor.erase();
												if (villagerx.getBrain().getMemory(MemoryModuleType.JOB_SITE).isEmpty()) {
													BehaviorUtils.setWalkAndLookTargetMemories(villagerx, blockPos, f, 1);
													villagerx.getBrain().setMemory(MemoryModuleType.POTENTIAL_JOB_SITE, GlobalPos.of(serverLevel.dimension(), blockPos));
													DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos);
												}
											});
										return true;
									}
								}
							}
					)
		);
	}

	private static boolean nearbyWantsJobsite(Holder<PoiType> holder, Villager villager, BlockPos blockPos) {
		boolean bl = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).isPresent();
		if (bl) {
			return false;
		} else {
			Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
			VillagerProfession villagerProfession = villager.getVillagerData().getProfession();
			if (villagerProfession.heldJobSite().test(holder)) {
				return optional.isEmpty() ? canReachPos(villager, blockPos, holder.value()) : ((GlobalPos)optional.get()).pos().equals(blockPos);
			} else {
				return false;
			}
		}
	}

	private static boolean canReachPos(PathfinderMob pathfinderMob, BlockPos blockPos, PoiType poiType) {
		Path path = pathfinderMob.getNavigation().createPath(blockPos, poiType.validRange());
		return path != null && path.canReach();
	}
}
