/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.Nullable;

public class ClientboundSetTitlesPacket
implements Packet<ClientGamePacketListener> {
    private Type type;
    private Component text;
    private int fadeInTime;
    private int stayTime;
    private int fadeOutTime;

    public ClientboundSetTitlesPacket() {
    }

    public ClientboundSetTitlesPacket(Type type, Component component) {
        this(type, component, -1, -1, -1);
    }

    public ClientboundSetTitlesPacket(int i, int j, int k) {
        this(Type.TIMES, null, i, j, k);
    }

    public ClientboundSetTitlesPacket(Type type, @Nullable Component component, int i, int j, int k) {
        this.type = type;
        this.text = component;
        this.fadeInTime = i;
        this.stayTime = j;
        this.fadeOutTime = k;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.type = friendlyByteBuf.readEnum(Type.class);
        if (this.type == Type.TITLE || this.type == Type.SUBTITLE || this.type == Type.ACTIONBAR) {
            this.text = friendlyByteBuf.readComponent();
        }
        if (this.type == Type.TIMES) {
            this.fadeInTime = friendlyByteBuf.readInt();
            this.stayTime = friendlyByteBuf.readInt();
            this.fadeOutTime = friendlyByteBuf.readInt();
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeEnum(this.type);
        if (this.type == Type.TITLE || this.type == Type.SUBTITLE || this.type == Type.ACTIONBAR) {
            friendlyByteBuf.writeComponent(this.text);
        }
        if (this.type == Type.TIMES) {
            friendlyByteBuf.writeInt(this.fadeInTime);
            friendlyByteBuf.writeInt(this.stayTime);
            friendlyByteBuf.writeInt(this.fadeOutTime);
        }
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetTitles(this);
    }

    @Environment(value=EnvType.CLIENT)
    public Type getType() {
        return this.type;
    }

    @Environment(value=EnvType.CLIENT)
    public Component getText() {
        return this.text;
    }

    @Environment(value=EnvType.CLIENT)
    public int getFadeInTime() {
        return this.fadeInTime;
    }

    @Environment(value=EnvType.CLIENT)
    public int getStayTime() {
        return this.stayTime;
    }

    @Environment(value=EnvType.CLIENT)
    public int getFadeOutTime() {
        return this.fadeOutTime;
    }

    public static enum Type {
        TITLE,
        SUBTITLE,
        ACTIONBAR,
        TIMES,
        CLEAR,
        RESET;

    }
}

