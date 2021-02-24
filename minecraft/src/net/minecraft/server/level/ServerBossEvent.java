package net.minecraft.server.level;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public class ServerBossEvent extends BossEvent {
	private final Set<ServerPlayer> players = Sets.<ServerPlayer>newHashSet();
	private final Set<ServerPlayer> unmodifiablePlayers = Collections.unmodifiableSet(this.players);
	private boolean visible = true;

	public ServerBossEvent(Component component, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay) {
		super(Mth.createInsecureUUID(), component, bossBarColor, bossBarOverlay);
	}

	@Override
	public void setProgress(float f) {
		if (f != this.progress) {
			super.setProgress(f);
			this.broadcast(ClientboundBossEventPacket::createUpdateProgressPacket);
		}
	}

	@Override
	public void setColor(BossEvent.BossBarColor bossBarColor) {
		if (bossBarColor != this.color) {
			super.setColor(bossBarColor);
			this.broadcast(ClientboundBossEventPacket::createUpdateStylePacket);
		}
	}

	@Override
	public void setOverlay(BossEvent.BossBarOverlay bossBarOverlay) {
		if (bossBarOverlay != this.overlay) {
			super.setOverlay(bossBarOverlay);
			this.broadcast(ClientboundBossEventPacket::createUpdateStylePacket);
		}
	}

	@Override
	public BossEvent setDarkenScreen(boolean bl) {
		if (bl != this.darkenScreen) {
			super.setDarkenScreen(bl);
			this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
		}

		return this;
	}

	@Override
	public BossEvent setPlayBossMusic(boolean bl) {
		if (bl != this.playBossMusic) {
			super.setPlayBossMusic(bl);
			this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
		}

		return this;
	}

	@Override
	public BossEvent setCreateWorldFog(boolean bl) {
		if (bl != this.createWorldFog) {
			super.setCreateWorldFog(bl);
			this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
		}

		return this;
	}

	@Override
	public void setName(Component component) {
		if (!Objects.equal(component, this.name)) {
			super.setName(component);
			this.broadcast(ClientboundBossEventPacket::createUpdateNamePacket);
		}
	}

	private void broadcast(Function<BossEvent, ClientboundBossEventPacket> function) {
		if (this.visible) {
			ClientboundBossEventPacket clientboundBossEventPacket = (ClientboundBossEventPacket)function.apply(this);

			for (ServerPlayer serverPlayer : this.players) {
				serverPlayer.connection.send(clientboundBossEventPacket);
			}
		}
	}

	public void addPlayer(ServerPlayer serverPlayer) {
		if (this.players.add(serverPlayer) && this.visible) {
			serverPlayer.connection.send(ClientboundBossEventPacket.createAddPacket(this));
		}
	}

	public void removePlayer(ServerPlayer serverPlayer) {
		if (this.players.remove(serverPlayer) && this.visible) {
			serverPlayer.connection.send(ClientboundBossEventPacket.createRemovePacket(this.getId()));
		}
	}

	public void removeAllPlayers() {
		if (!this.players.isEmpty()) {
			for (ServerPlayer serverPlayer : Lists.newArrayList(this.players)) {
				this.removePlayer(serverPlayer);
			}
		}
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(boolean bl) {
		if (bl != this.visible) {
			this.visible = bl;

			for (ServerPlayer serverPlayer : this.players) {
				serverPlayer.connection.send(bl ? ClientboundBossEventPacket.createAddPacket(this) : ClientboundBossEventPacket.createRemovePacket(this.getId()));
			}
		}
	}

	public Collection<ServerPlayer> getPlayers() {
		return this.unmodifiablePlayers;
	}
}
