package net.minecraft.world.item;

import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ServerItemCooldowns extends ItemCooldowns {
	private final ServerPlayer player;

	public ServerItemCooldowns(ServerPlayer serverPlayer) {
		this.player = serverPlayer;
	}

	@Override
	protected void onCooldownStarted(ResourceLocation resourceLocation, int i) {
		super.onCooldownStarted(resourceLocation, i);
		this.player.connection.send(new ClientboundCooldownPacket(resourceLocation, i));
	}

	@Override
	protected void onCooldownEnded(ResourceLocation resourceLocation) {
		super.onCooldownEnded(resourceLocation);
		this.player.connection.send(new ClientboundCooldownPacket(resourceLocation, 0));
	}
}
