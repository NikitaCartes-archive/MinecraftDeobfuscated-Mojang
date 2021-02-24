package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCustomPayloadPacket implements Packet<ClientGamePacketListener> {
	public static final ResourceLocation BRAND = new ResourceLocation("brand");
	public static final ResourceLocation DEBUG_PATHFINDING_PACKET = new ResourceLocation("debug/path");
	public static final ResourceLocation DEBUG_NEIGHBORSUPDATE_PACKET = new ResourceLocation("debug/neighbors_update");
	public static final ResourceLocation DEBUG_STRUCTURES_PACKET = new ResourceLocation("debug/structures");
	public static final ResourceLocation DEBUG_WORLDGENATTEMPT_PACKET = new ResourceLocation("debug/worldgen_attempt");
	public static final ResourceLocation DEBUG_POI_TICKET_COUNT_PACKET = new ResourceLocation("debug/poi_ticket_count");
	public static final ResourceLocation DEBUG_POI_ADDED_PACKET = new ResourceLocation("debug/poi_added");
	public static final ResourceLocation DEBUG_POI_REMOVED_PACKET = new ResourceLocation("debug/poi_removed");
	public static final ResourceLocation DEBUG_VILLAGE_SECTIONS = new ResourceLocation("debug/village_sections");
	public static final ResourceLocation DEBUG_GOAL_SELECTOR = new ResourceLocation("debug/goal_selector");
	public static final ResourceLocation DEBUG_BRAIN = new ResourceLocation("debug/brain");
	public static final ResourceLocation DEBUG_BEE = new ResourceLocation("debug/bee");
	public static final ResourceLocation DEBUG_HIVE = new ResourceLocation("debug/hive");
	public static final ResourceLocation DEBUG_GAME_TEST_ADD_MARKER = new ResourceLocation("debug/game_test_add_marker");
	public static final ResourceLocation DEBUG_GAME_TEST_CLEAR = new ResourceLocation("debug/game_test_clear");
	public static final ResourceLocation DEBUG_RAIDS = new ResourceLocation("debug/raids");
	public static final ResourceLocation DEBUG_GAME_EVENT = new ResourceLocation("debug/game_event");
	public static final ResourceLocation DEBUG_GAME_EVENT_LISTENER = new ResourceLocation("debug/game_event_listeners");
	private final ResourceLocation identifier;
	private final FriendlyByteBuf data;

	public ClientboundCustomPayloadPacket(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
		this.identifier = resourceLocation;
		this.data = friendlyByteBuf;
		if (friendlyByteBuf.writerIndex() > 1048576) {
			throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
		}
	}

	public ClientboundCustomPayloadPacket(FriendlyByteBuf friendlyByteBuf) {
		this.identifier = friendlyByteBuf.readResourceLocation();
		int i = friendlyByteBuf.readableBytes();
		if (i >= 0 && i <= 1048576) {
			this.data = new FriendlyByteBuf(friendlyByteBuf.readBytes(i));
		} else {
			throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeResourceLocation(this.identifier);
		friendlyByteBuf.writeBytes(this.data.copy());
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleCustomPayload(this);
	}

	@Environment(EnvType.CLIENT)
	public ResourceLocation getIdentifier() {
		return this.identifier;
	}

	@Environment(EnvType.CLIENT)
	public FriendlyByteBuf getData() {
		return new FriendlyByteBuf(this.data.copy());
	}
}
