/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class ClientboundOpenScreenPacket
implements Packet<ClientGamePacketListener> {
    private final int containerId;
    private final MenuType<?> type;
    private final Component title;

    public ClientboundOpenScreenPacket(int i, MenuType<?> menuType, Component component) {
        this.containerId = i;
        this.type = menuType;
        this.title = component;
    }

    public ClientboundOpenScreenPacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readVarInt();
        this.type = friendlyByteBuf.readById(Registry.MENU);
        this.title = friendlyByteBuf.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.containerId);
        friendlyByteBuf.writeId(Registry.MENU, this.type);
        friendlyByteBuf.writeComponent(this.title);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleOpenScreen(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    @Nullable
    public MenuType<?> getType() {
        return this.type;
    }

    public Component getTitle() {
        return this.title;
    }
}

