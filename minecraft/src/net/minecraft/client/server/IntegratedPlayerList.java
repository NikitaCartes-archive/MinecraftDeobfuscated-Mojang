package net.minecraft.client.server;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;

@Environment(EnvType.CLIENT)
public class IntegratedPlayerList extends PlayerList {
	private CompoundTag playerData;

	public IntegratedPlayerList(IntegratedServer integratedServer, RegistryAccess.Frozen frozen, PlayerDataStorage playerDataStorage) {
		super(integratedServer, frozen, playerDataStorage, 8);
		this.setViewDistance(10);
	}

	@Override
	protected void save(ServerPlayer serverPlayer) {
		if (serverPlayer.getName().getString().equals(this.getServer().getSingleplayerName())) {
			this.playerData = serverPlayer.saveWithoutId(new CompoundTag());
		}

		super.save(serverPlayer);
	}

	@Override
	public Component canPlayerLogin(SocketAddress socketAddress, GameProfile gameProfile) {
		return (Component)(gameProfile.getName().equalsIgnoreCase(this.getServer().getSingleplayerName()) && this.getPlayerByName(gameProfile.getName()) != null
			? new TranslatableComponent("multiplayer.disconnect.name_taken")
			: super.canPlayerLogin(socketAddress, gameProfile));
	}

	public IntegratedServer getServer() {
		return (IntegratedServer)super.getServer();
	}

	@Override
	public CompoundTag getSingleplayerData() {
		return this.playerData;
	}
}
