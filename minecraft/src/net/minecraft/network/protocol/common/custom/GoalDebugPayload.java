package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record GoalDebugPayload(int entityId, BlockPos pos, List<GoalDebugPayload.DebugGoal> goals) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/goal_selector");

	public GoalDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readInt(), friendlyByteBuf.readBlockPos(), friendlyByteBuf.readList(GoalDebugPayload.DebugGoal::new));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.entityId);
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeCollection(this.goals, (friendlyByteBufx, debugGoal) -> debugGoal.write(friendlyByteBufx));
	}

	@Override
	public ResourceLocation id() {
		return ID;
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
