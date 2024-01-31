package net.minecraft.util.debugchart;

import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import net.minecraft.Util;
import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class DebugSampleSubscriptionTracker {
	public static final int STOP_SENDING_AFTER_TICKS = 200;
	public static final int STOP_SENDING_AFTER_MS = 10000;
	private final PlayerList playerList;
	private final EnumMap<RemoteDebugSampleType, Map<ServerPlayer, DebugSampleSubscriptionTracker.SubscriptionStartedAt>> subscriptions;
	private final Queue<DebugSampleSubscriptionTracker.SubscriptionRequest> subscriptionRequestQueue = new LinkedList();

	public DebugSampleSubscriptionTracker(PlayerList playerList) {
		this.playerList = playerList;
		this.subscriptions = new EnumMap(RemoteDebugSampleType.class);

		for (RemoteDebugSampleType remoteDebugSampleType : RemoteDebugSampleType.values()) {
			this.subscriptions.put(remoteDebugSampleType, Maps.newHashMap());
		}
	}

	public boolean shouldLogSamples(RemoteDebugSampleType remoteDebugSampleType) {
		return !((Map)this.subscriptions.get(remoteDebugSampleType)).isEmpty();
	}

	public void broadcast(ClientboundDebugSamplePacket clientboundDebugSamplePacket) {
		for (ServerPlayer serverPlayer : ((Map)this.subscriptions.get(clientboundDebugSamplePacket.debugSampleType())).keySet()) {
			serverPlayer.connection.send(clientboundDebugSamplePacket);
		}
	}

	public void subscribe(ServerPlayer serverPlayer, RemoteDebugSampleType remoteDebugSampleType) {
		if (this.playerList.isOp(serverPlayer.getGameProfile())) {
			this.subscriptionRequestQueue.add(new DebugSampleSubscriptionTracker.SubscriptionRequest(serverPlayer, remoteDebugSampleType));
		}
	}

	public void tick(int i) {
		long l = Util.getMillis();
		this.handleSubscriptions(l, i);
		this.handleUnsubscriptions(l, i);
	}

	private void handleSubscriptions(long l, int i) {
		for (DebugSampleSubscriptionTracker.SubscriptionRequest subscriptionRequest : this.subscriptionRequestQueue) {
			((Map)this.subscriptions.get(subscriptionRequest.sampleType()))
				.put(subscriptionRequest.player(), new DebugSampleSubscriptionTracker.SubscriptionStartedAt(l, i));
		}
	}

	private void handleUnsubscriptions(long l, int i) {
		for (Map<ServerPlayer, DebugSampleSubscriptionTracker.SubscriptionStartedAt> map : this.subscriptions.values()) {
			map.entrySet().removeIf(entry -> {
				boolean bl = !this.playerList.isOp(((ServerPlayer)entry.getKey()).getGameProfile());
				DebugSampleSubscriptionTracker.SubscriptionStartedAt subscriptionStartedAt = (DebugSampleSubscriptionTracker.SubscriptionStartedAt)entry.getValue();
				return bl || i > subscriptionStartedAt.tick() + 200 && l > subscriptionStartedAt.millis() + 10000L;
			});
		}
	}

	static record SubscriptionRequest(ServerPlayer player, RemoteDebugSampleType sampleType) {
	}

	static record SubscriptionStartedAt(long millis, int tick) {
	}
}
