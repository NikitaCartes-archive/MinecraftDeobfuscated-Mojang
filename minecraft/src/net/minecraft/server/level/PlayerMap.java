package net.minecraft.server.level;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Set;

public final class PlayerMap {
	private final Object2BooleanMap<ServerPlayer> players = new Object2BooleanOpenHashMap<>();

	public Set<ServerPlayer> getAllPlayers() {
		return this.players.keySet();
	}

	public void addPlayer(ServerPlayer serverPlayer, boolean bl) {
		this.players.put(serverPlayer, bl);
	}

	public void removePlayer(ServerPlayer serverPlayer) {
		this.players.removeBoolean(serverPlayer);
	}

	public void ignorePlayer(ServerPlayer serverPlayer) {
		this.players.replace(serverPlayer, true);
	}

	public void unIgnorePlayer(ServerPlayer serverPlayer) {
		this.players.replace(serverPlayer, false);
	}

	public boolean ignoredOrUnknown(ServerPlayer serverPlayer) {
		return this.players.getOrDefault(serverPlayer, true);
	}

	public boolean ignored(ServerPlayer serverPlayer) {
		return this.players.getBoolean(serverPlayer);
	}
}
