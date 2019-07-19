/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;

public class ServerItemCooldowns
extends ItemCooldowns {
    private final ServerPlayer player;

    public ServerItemCooldowns(ServerPlayer serverPlayer) {
        this.player = serverPlayer;
    }

    @Override
    protected void onCooldownStarted(Item item, int i) {
        super.onCooldownStarted(item, i);
        this.player.connection.send(new ClientboundCooldownPacket(item, i));
    }

    @Override
    protected void onCooldownEnded(Item item) {
        super.onCooldownEnded(item);
        this.player.connection.send(new ClientboundCooldownPacket(item, 0));
    }
}

