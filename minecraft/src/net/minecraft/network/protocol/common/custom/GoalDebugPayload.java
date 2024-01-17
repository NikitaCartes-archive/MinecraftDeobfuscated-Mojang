package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record GoalDebugPayload(int entityId, BlockPos pos, List<GoalDebugPayload.DebugGoal> goals) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, GoalDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(GoalDebugPayload::write, GoalDebugPayload::new);
	public static final CustomPacketPayload.Type<GoalDebugPayload> TYPE = CustomPacketPayload.createType("debug/goal_selector");

	private GoalDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readInt(), friendlyByteBuf.readBlockPos(), friendlyByteBuf.readList(GoalDebugPayload.DebugGoal::new));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.entityId);
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeCollection(this.goals, (friendlyByteBufx, debugGoal) -> debugGoal.write(friendlyByteBufx));
	}

	@Override
	public CustomPacketPayload.Type<GoalDebugPayload> type() {
		return TYPE;
	}

	public static record DebugGoal(int priority, boolean isRunning, String name) {
		public DebugGoal(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readInt(), friendlyByteBuf.readBoolean(), friendlyByteBuf.readUtf(255));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeInt(this.priority);
			friendlyByteBuf.writeBoolean(this.isRunning);
			friendlyByteBuf.writeUtf(this.name);
		}
	}
}
