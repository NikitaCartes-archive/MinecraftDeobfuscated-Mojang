package net.minecraft.server.dedicated;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.slf4j.Logger;

public class DedicatedPlayerList extends PlayerList {
	private static final Logger LOGGER = LogUtils.getLogger();

	public DedicatedPlayerList(DedicatedServer dedicatedServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage) {
		super(dedicatedServer, layeredRegistryAccess, playerDataStorage, dedicatedServer.getProperties().maxPlayers);
		DedicatedServerProperties dedicatedServerProperties = dedicatedServer.getProperties();
		this.setViewDistance(dedicatedServerProperties.viewDistance);
		this.setSimulationDistance(dedicatedServerProperties.simulationDistance);
		super.setUsingWhiteList(dedicatedServerProperties.whiteList.get());
		this.loadUserBanList();
		this.saveUserBanList();
		this.loadIpBanList();
		this.saveIpBanList();
		this.loadOps();
		this.loadWhiteList();
		this.saveOps();
		if (!this.getWhiteList().getFile().exists()) {
			this.saveWhiteList();
		}
	}

	@Override
	public void setUsingWhiteList(boolean bl) {
		super.setUsingWhiteList(bl);
		this.getServer().storeUsingWhiteList(bl);
	}

	@Override
	public void op(GameProfile gameProfile) {
		super.op(gameProfile);
		this.saveOps();
	}

	@Override
	public void deop(GameProfile gameProfile) {
		super.deop(gameProfile);
		this.saveOps();
	}

	@Override
	public void reloadWhiteList() {
		this.loadWhiteList();
	}

	private void saveIpBanList() {
		try {
			this.getIpBans().save();
		} catch (IOException var2) {
			LOGGER.warn("Failed to save ip banlist: ", (Throwable)var2);
		}
	}

	private void saveUserBanList() {
		try {
			this.getBans().save();
		} catch (IOException var2) {
			LOGGER.warn("Failed to save user banlist: ", (Throwable)var2);
		}
	}

	private void loadIpBanList() {
		try {
			this.getIpBans().load();
		} catch (IOException var2) {
			LOGGER.warn("Failed to load ip banlist: ", (Throwable)var2);
		}
	}

	private void loadUserBanList() {
		try {
			this.getBans().load();
		} catch (IOException var2) {
			LOGGER.warn("Failed to load user banlist: ", (Throwable)var2);
		}
	}

	private void loadOps() {
		try {
			this.getOps().load();
		} catch (Exception var2) {
			LOGGER.warn("Failed to load operators list: ", (Throwable)var2);
		}
	}

	private void saveOps() {
		try {
			this.getOps().save();
		} catch (Exception var2) {
			LOGGER.warn("Failed to save operators list: ", (Throwable)var2);
		}
	}

	private void loadWhiteList() {
		try {
			this.getWhiteList().load();
		} catch (Exception var2) {
			LOGGER.warn("Failed to load white-list: ", (Throwable)var2);
		}
	}

	private void saveWhiteList() {
		try {
			this.getWhiteList().save();
		} catch (Exception var2) {
			LOGGER.warn("Failed to save white-list: ", (Throwable)var2);
		}
	}

	@Override
	public boolean isWhiteListed(GameProfile gameProfile) {
		return !this.isUsingWhitelist() || this.isOp(gameProfile) || this.getWhiteList().isWhiteListed(gameProfile);
	}

	public DedicatedServer getServer() {
		return (DedicatedServer)super.getServer();
	}

	@Override
	public boolean canBypassPlayerLimit(GameProfile gameProfile) {
		return this.getOps().canBypassPlayerLimit(gameProfile);
	}
}
