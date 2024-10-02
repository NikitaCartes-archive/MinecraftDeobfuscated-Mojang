package net.minecraft.world.entity;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public record PositionMoveRotation(Vec3 position, Vec3 deltaMovement, float yRot, float xRot) {
	public static final StreamCodec<FriendlyByteBuf, PositionMoveRotation> STREAM_CODEC = StreamCodec.composite(
		Vec3.STREAM_CODEC,
		PositionMoveRotation::position,
		Vec3.STREAM_CODEC,
		PositionMoveRotation::deltaMovement,
		ByteBufCodecs.FLOAT,
		PositionMoveRotation::yRot,
		ByteBufCodecs.FLOAT,
		PositionMoveRotation::xRot,
		PositionMoveRotation::new
	);

	public static PositionMoveRotation of(Entity entity) {
		return new PositionMoveRotation(entity.position(), entity.getKnownMovement(), entity.getYRot(), entity.getXRot());
	}

	public static PositionMoveRotation ofEntityUsingLerpTarget(Entity entity) {
		return new PositionMoveRotation(
			new Vec3(entity.lerpTargetX(), entity.lerpTargetY(), entity.lerpTargetZ()), entity.getKnownMovement(), entity.getYRot(), entity.getXRot()
		);
	}

	public static PositionMoveRotation of(TeleportTransition teleportTransition) {
		return new PositionMoveRotation(teleportTransition.position(), teleportTransition.deltaMovement(), teleportTransition.yRot(), teleportTransition.xRot());
	}

	public static PositionMoveRotation calculateAbsolute(PositionMoveRotation positionMoveRotation, PositionMoveRotation positionMoveRotation2, Set<Relative> set) {
		double d = set.contains(Relative.X) ? positionMoveRotation.position.x : 0.0;
		double e = set.contains(Relative.Y) ? positionMoveRotation.position.y : 0.0;
		double f = set.contains(Relative.Z) ? positionMoveRotation.position.z : 0.0;
		float g = set.contains(Relative.Y_ROT) ? positionMoveRotation.yRot : 0.0F;
		float h = set.contains(Relative.X_ROT) ? positionMoveRotation.xRot : 0.0F;
		Vec3 vec3 = new Vec3(d + positionMoveRotation2.position.x, e + positionMoveRotation2.position.y, f + positionMoveRotation2.position.z);
		float i = g + positionMoveRotation2.yRot;
		float j = h + positionMoveRotation2.xRot;
		Vec3 vec32 = positionMoveRotation.deltaMovement;
		if (set.contains(Relative.ROTATE_DELTA)) {
			float k = positionMoveRotation.yRot - i;
			float l = positionMoveRotation.xRot - j;
			vec32 = vec32.xRot((float)Math.toRadians((double)l));
			vec32 = vec32.yRot((float)Math.toRadians((double)k));
		}

		Vec3 vec33 = new Vec3(
			calculateDelta(vec32.x, positionMoveRotation2.deltaMovement.x, set, Relative.DELTA_X),
			calculateDelta(vec32.y, positionMoveRotation2.deltaMovement.y, set, Relative.DELTA_Y),
			calculateDelta(vec32.z, positionMoveRotation2.deltaMovement.z, set, Relative.DELTA_Z)
		);
		return new PositionMoveRotation(vec3, vec33, i, j);
	}

	private static double calculateDelta(double d, double e, Set<Relative> set, Relative relative) {
		return set.contains(relative) ? d + e : e;
	}
}
