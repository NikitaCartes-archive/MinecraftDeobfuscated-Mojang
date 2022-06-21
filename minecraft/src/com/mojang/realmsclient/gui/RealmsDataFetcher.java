package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.gui.task.DataFetcher;
import com.mojang.realmsclient.gui.task.RepeatedDelayStrategy;
import com.mojang.realmsclient.util.RealmsPersistence;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;

@Environment(EnvType.CLIENT)
public class RealmsDataFetcher {
	public final DataFetcher dataFetcher = new DataFetcher(Util.ioPool(), TimeUnit.MILLISECONDS, Util.timeSource);
	public final DataFetcher.Task<List<RealmsServer>> serverListUpdateTask;
	public final DataFetcher.Task<RealmsServerPlayerLists> liveStatsTask;
	public final DataFetcher.Task<Integer> pendingInvitesTask;
	public final DataFetcher.Task<Boolean> trialAvailabilityTask;
	public final DataFetcher.Task<RealmsNews> newsTask;
	public final RealmsNewsManager newsManager = new RealmsNewsManager(new RealmsPersistence());

	public RealmsDataFetcher(RealmsClient realmsClient) {
		this.serverListUpdateTask = this.dataFetcher
			.createTask("server list", () -> realmsClient.listWorlds().servers, Duration.ofSeconds(60L), RepeatedDelayStrategy.CONSTANT);
		this.liveStatsTask = this.dataFetcher.createTask("live stats", realmsClient::getLiveStats, Duration.ofSeconds(10L), RepeatedDelayStrategy.CONSTANT);
		this.pendingInvitesTask = this.dataFetcher
			.createTask("pending invite count", realmsClient::pendingInvitesCount, Duration.ofSeconds(10L), RepeatedDelayStrategy.exponentialBackoff(360));
		this.trialAvailabilityTask = this.dataFetcher
			.createTask("trial availablity", realmsClient::trialAvailable, Duration.ofSeconds(60L), RepeatedDelayStrategy.exponentialBackoff(60));
		this.newsTask = this.dataFetcher.createTask("unread news", realmsClient::getNews, Duration.ofMinutes(5L), RepeatedDelayStrategy.CONSTANT);
	}
}
