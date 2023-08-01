package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;

public record GameEventListenerDebugPayload(PositionSource listenerPos, int listenerRange) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/game_event_listeners");

	public GameEventListenerDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(PositionSourceType.fromNetwork(friendlyByteBuf), friendlyByteBuf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		PositionSourceType.toNetwork(this.listenerPos, friendlyByteBuf);
		friendlyByteBuf.writeVarInt(this.listenerRange);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
