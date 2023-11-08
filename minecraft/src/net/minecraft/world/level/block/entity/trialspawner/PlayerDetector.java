package net.minecraft.world.level.block.entity.trialspawner;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public interface PlayerDetector {
	PlayerDetector PLAYERS = (serverLevel, blockPos, i) -> serverLevel.getPlayers(
				serverPlayer -> serverPlayer.blockPosition().closerThan(blockPos, (double)i) && !serverPlayer.isCreative() && !serverPlayer.isSpectator()
			)
			.stream()
			.map(Entity::getUUID)
			.toList();
	PlayerDetector SHEEP = (serverLevel, blockPos, i) -> {
		AABB aABB = new AABB(blockPos).inflate((double)i);
		return serverLevel.getEntities(EntityType.SHEEP, aABB, LivingEntity::isAlive).stream().map(Entity::getUUID).toList();
	};

	List<UUID> detect(ServerLevel serverLevel, BlockPos blockPos, int i);
}
