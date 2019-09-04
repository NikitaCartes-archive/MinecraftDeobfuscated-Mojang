package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class PickUpItems extends Behavior<Villager> {
	private List<ItemEntity> items = Lists.<ItemEntity>newArrayList();

	public PickUpItems() {
		super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		this.items = serverLevel.getEntitiesOfClass(ItemEntity.class, villager.getBoundingBox().inflate(4.0, 2.0, 4.0));
		return !this.items.isEmpty();
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		ItemEntity itemEntity = (ItemEntity)this.items.get(serverLevel.random.nextInt(this.items.size()));
		if (villager.wantToPickUp(itemEntity.getItem().getItem())) {
			Vec3 vec3 = itemEntity.position();
			villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(new BlockPos(vec3)));
			villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, 0.5F, 0));
		}
	}
}
