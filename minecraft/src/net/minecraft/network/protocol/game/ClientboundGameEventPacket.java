package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundGameEventPacket implements Packet<ClientGamePacketListener> {
	public static final ClientboundGameEventPacket.Type NO_RESPAWN_BLOCK_AVAILABLE = new ClientboundGameEventPacket.Type(0);
	public static final ClientboundGameEventPacket.Type START_RAINING = new ClientboundGameEventPacket.Type(1);
	public static final ClientboundGameEventPacket.Type STOP_RAINING = new ClientboundGameEventPacket.Type(2);
	public static final ClientboundGameEventPacket.Type CHANGE_GAME_MODE = new ClientboundGameEventPacket.Type(3);
	public static final ClientboundGameEventPacket.Type WIN_GAME = new ClientboundGameEventPacket.Type(4);
	public static final ClientboundGameEventPacket.Type DEMO_EVENT = new ClientboundGameEventPacket.Type(5);
	public static final ClientboundGameEventPacket.Type ARROW_HIT_PLAYER = new ClientboundGameEventPacket.Type(6);
	public static final ClientboundGameEventPacket.Type RAIN_LEVEL_CHANGE = new ClientboundGameEventPacket.Type(7);
	public static final ClientboundGameEventPacket.Type THUNDER_LEVEL_CHANGE = new ClientboundGameEventPacket.Type(8);
	public static final ClientboundGameEventPacket.Type PUFFER_FISH_STING = new ClientboundGameEventPacket.Type(9);
	public static final ClientboundGameEventPacket.Type GUARDIAN_ELDER_EFFECT = new ClientboundGameEventPacket.Type(10);
	public static final ClientboundGameEventPacket.Type IMMEDIATE_RESPAWN = new ClientboundGameEventPacket.Type(11);
	public static final ClientboundGameEventPacket.Type LIMITED_CRAFTING = new ClientboundGameEventPacket.Type(12);
	public static final ClientboundGameEventPacket.Type LEVEL_CHUNKS_LOAD_START = new ClientboundGameEventPacket.Type(13);
	public static final int DEMO_PARAM_INTRO = 0;
	public static final int DEMO_PARAM_HINT_1 = 101;
	public static final int DEMO_PARAM_HINT_2 = 102;
	public static final int DEMO_PARAM_HINT_3 = 103;
	public static final int DEMO_PARAM_HINT_4 = 104;
	private final ClientboundGameEventPacket.Type event;
	private final float param;

	public ClientboundGameEventPacket(ClientboundGameEventPacket.Type type, float f) {
		this.event = type;
		this.param = f;
	}

	public ClientboundGameEventPacket(FriendlyByteBuf friendlyByteBuf) {
		this.event = ClientboundGameEventPacket.Type.TYPES.get(friendlyByteBuf.readUnsignedByte());
		this.param = friendlyByteBuf.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.event.id);
		friendlyByteBuf.writeFloat(this.param);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleGameEvent(this);
	}

	public ClientboundGameEventPacket.Type getEvent() {
		return this.event;
	}

	public float getParam() {
		return this.param;
	}

	public static class Type {
		static final Int2ObjectMap<ClientboundGameEventPacket.Type> TYPES = new Int2ObjectOpenHashMap<>();
		final int id;

		public Type(int i) {
			this.id = i;
			TYPES.put(i, this);
		}
	}
}
