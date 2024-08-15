package net.minecraft.world.level.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record DimensionTransition(
	ServerLevel newLevel,
	Vec3 pos,
	Vec3 speed,
	float yRot,
	float xRot,
	boolean missingRespawnBlock,
	DimensionTransition.PostDimensionTransition postDimensionTransition
) {
	public static final DimensionTransition.PostDimensionTransition DO_NOTHING = entity -> {
	};
	public static final DimensionTransition.PostDimensionTransition PLAY_PORTAL_SOUND = DimensionTransition::playPortalSound;
	public static final DimensionTransition.PostDimensionTransition PLACE_PORTAL_TICKET = DimensionTransition::placePortalTicket;

	public DimensionTransition(
		ServerLevel serverLevel, Vec3 vec3, Vec3 vec32, float f, float g, DimensionTransition.PostDimensionTransition postDimensionTransition
	) {
		this(serverLevel, vec3, vec32, f, g, false, postDimensionTransition);
	}

	public DimensionTransition(ServerLevel serverLevel, Entity entity, DimensionTransition.PostDimensionTransition postDimensionTransition) {
		this(serverLevel, findAdjustedSharedSpawnPos(serverLevel, entity), Vec3.ZERO, 0.0F, 0.0F, false, postDimensionTransition);
	}

	private static void playPortalSound(Entity entity) {
		if (entity instanceof ServerPlayer serverPlayer) {
			serverPlayer.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
		}
	}

	private static void placePortalTicket(Entity entity) {
		entity.placePortalTicket(BlockPos.containing(entity.position()));
	}

	public static DimensionTransition missingRespawnBlock(
		ServerLevel serverLevel, Entity entity, DimensionTransition.PostDimensionTransition postDimensionTransition
	) {
		return new DimensionTransition(serverLevel, findAdjustedSharedSpawnPos(serverLevel, entity), Vec3.ZERO, 0.0F, 0.0F, true, postDimensionTransition);
	}

	private static Vec3 findAdjustedSharedSpawnPos(ServerLevel serverLevel, Entity entity) {
		return entity.adjustSpawnLocation(serverLevel, serverLevel.getSharedSpawnPos()).getBottomCenter();
	}

	public DimensionTransition withRotation(float f, float g) {
		return new DimensionTransition(this.newLevel(), this.pos(), this.speed(), f, g, this.missingRespawnBlock(), this.postDimensionTransition());
	}

	@FunctionalInterface
	public interface PostDimensionTransition {
		void onTransition(Entity entity);

		default DimensionTransition.PostDimensionTransition then(DimensionTransition.PostDimensionTransition postDimensionTransition) {
			return entity -> {
				this.onTransition(entity);
				postDimensionTransition.onTransition(entity);
			};
		}
	}
}
