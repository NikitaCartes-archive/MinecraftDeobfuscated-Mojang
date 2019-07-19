/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ServerboundSeenAdvancementsPacket
implements Packet<ServerGamePacketListener> {
    private Action action;
    private ResourceLocation tab;

    public ServerboundSeenAdvancementsPacket() {
    }

    @Environment(value=EnvType.CLIENT)
    public ServerboundSeenAdvancementsPacket(Action action, @Nullable ResourceLocation resourceLocation) {
        this.action = action;
        this.tab = resourceLocation;
    }

    @Environment(value=EnvType.CLIENT)
    public static ServerboundSeenAdvancementsPacket openedTab(Advancement advancement) {
        return new ServerboundSeenAdvancementsPacket(Action.OPENED_TAB, advancement.getId());
    }

    @Environment(value=EnvType.CLIENT)
    public static ServerboundSeenAdvancementsPacket closedScreen() {
        return new ServerboundSeenAdvancementsPacket(Action.CLOSED_SCREEN, null);
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.action = friendlyByteBuf.readEnum(Action.class);
        if (this.action == Action.OPENED_TAB) {
            this.tab = friendlyByteBuf.readResourceLocation();
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeEnum(this.action);
        if (this.action == Action.OPENED_TAB) {
            friendlyByteBuf.writeResourceLocation(this.tab);
        }
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleSeenAdvancements(this);
    }

    public Action getAction() {
        return this.action;
    }

    public ResourceLocation getTab() {
        return this.tab;
    }

    public static enum Action {
        OPENED_TAB,
        CLOSED_SCREEN;

    }
}

