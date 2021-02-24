/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public class ClientboundStatusResponsePacket
implements Packet<ClientStatusPacketListener> {
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter((Type)((Object)ServerStatus.Version.class), new ServerStatus.Version.Serializer()).registerTypeAdapter((Type)((Object)ServerStatus.Players.class), new ServerStatus.Players.Serializer()).registerTypeAdapter((Type)((Object)ServerStatus.class), new ServerStatus.Serializer()).registerTypeHierarchyAdapter(Component.class, new Component.Serializer()).registerTypeHierarchyAdapter(Style.class, new Style.Serializer()).registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory()).create();
    private final ServerStatus status;

    public ClientboundStatusResponsePacket(ServerStatus serverStatus) {
        this.status = serverStatus;
    }

    public ClientboundStatusResponsePacket(FriendlyByteBuf friendlyByteBuf) {
        this.status = GsonHelper.fromJson(GSON, friendlyByteBuf.readUtf(Short.MAX_VALUE), ServerStatus.class);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(GSON.toJson(this.status));
    }

    @Override
    public void handle(ClientStatusPacketListener clientStatusPacketListener) {
        clientStatusPacketListener.handleStatusResponse(this);
    }

    @Environment(value=EnvType.CLIENT)
    public ServerStatus getStatus() {
        return this.status;
    }
}

