package net.minecraft.network.protocol.status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public class ClientboundStatusResponsePacket implements Packet<ClientStatusPacketListener> {
	private static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(ServerStatus.Version.class, new ServerStatus.Version.Serializer())
		.registerTypeAdapter(ServerStatus.Players.class, new ServerStatus.Players.Serializer())
		.registerTypeAdapter(ServerStatus.class, new ServerStatus.Serializer())
		.registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
		.registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
		.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
		.create();
	private final ServerStatus status;

	public ClientboundStatusResponsePacket(ServerStatus serverStatus) {
		this.status = serverStatus;
	}

	public ClientboundStatusResponsePacket(FriendlyByteBuf friendlyByteBuf) {
		this.status = GsonHelper.fromJson(GSON, friendlyByteBuf.readUtf(32767), ServerStatus.class);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(GSON.toJson(this.status));
	}

	public void handle(ClientStatusPacketListener clientStatusPacketListener) {
		clientStatusPacketListener.handleStatusResponse(this);
	}

	public ServerStatus getStatus() {
		return this.status;
	}
}
