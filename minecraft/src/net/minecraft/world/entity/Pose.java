package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum Pose {
	STANDING(0),
	FALL_FLYING(1),
	SLEEPING(2),
	SWIMMING(3),
	SPIN_ATTACK(4),
	CROUCHING(5),
	LONG_JUMPING(6),
	DYING(7),
	CROAKING(8),
	USING_TONGUE(9),
	SITTING(10),
	ROARING(11),
	SNIFFING(12),
	EMERGING(13),
	DIGGING(14),
	SLIDING(15),
	SHOOTING(16),
	INHALING(17);

	public static final IntFunction<Pose> BY_ID = ByIdMap.continuous(Pose::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	public static final StreamCodec<ByteBuf, Pose> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Pose::id);
	private final int id;

	private Pose(final int j) {
		this.id = j;
	}

	public int id() {
		return this.id;
	}
}
