package com.mojang.realmsclient.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.gui.task.RepeatableTask;
import com.mojang.realmsclient.util.RealmsPersistence;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsDataFetcher {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft minecraft;
	private final RealmsClient realmsClient;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
	private volatile boolean stopped = true;
	private final RepeatableTask serverListUpdateTask = RepeatableTask.withImmediateRestart(this::updateServersList, Duration.ofSeconds(60L), this::isActive);
	private final RepeatableTask liveStatsTask = RepeatableTask.withImmediateRestart(this::updateLiveStats, Duration.ofSeconds(10L), this::isActive);
	private final RepeatableTask pendingInviteUpdateTask = RepeatableTask.withRestartDelayAccountingForInterval(
		this::updatePendingInvites, Duration.ofSeconds(10L), this::isActive
	);
	private final RepeatableTask trialAvailabilityTask = RepeatableTask.withRestartDelayAccountingForInterval(
		this::updateTrialAvailable, Duration.ofSeconds(60L), this::isActive
	);
	private final RepeatableTask unreadNewsTask = RepeatableTask.withRestartDelayAccountingForInterval(
		this::updateUnreadNews, Duration.ofMinutes(5L), this::isActive
	);
	private final RealmsPersistence newsLocalStorage;
	private final Set<RealmsServer> removedServers = Sets.<RealmsServer>newHashSet();
	private List<RealmsServer> servers = Lists.<RealmsServer>newArrayList();
	private RealmsServerPlayerLists livestats;
	private int pendingInvitesCount;
	private boolean trialAvailable;
	private boolean hasUnreadNews;
	private String newsLink;
	private ScheduledFuture<?> serverListScheduledFuture;
	private ScheduledFuture<?> pendingInviteScheduledFuture;
	private ScheduledFuture<?> trialAvailableScheduledFuture;
	private ScheduledFuture<?> liveStatsScheduledFuture;
	private ScheduledFuture<?> unreadNewsScheduledFuture;
	private final Map<RealmsDataFetcher.Task, Boolean> fetchStatus = new ConcurrentHashMap(RealmsDataFetcher.Task.values().length);

	public RealmsDataFetcher(Minecraft minecraft, RealmsClient realmsClient) {
		this.minecraft = minecraft;
		this.realmsClient = realmsClient;
		this.newsLocalStorage = new RealmsPersistence();
	}

	public boolean isStopped() {
		return this.stopped;
	}

	public synchronized void init() {
		if (this.stopped) {
			this.stopped = false;
			this.cancelTasks();
			this.scheduleTasks();
		}
	}

	public synchronized void initWithSpecificTaskList() {
		if (this.stopped) {
			this.stopped = false;
			this.cancelTasks();
			this.fetchStatus.put(RealmsDataFetcher.Task.PENDING_INVITE, false);
			this.pendingInviteScheduledFuture = this.pendingInviteUpdateTask.schedule(this.scheduler);
			this.fetchStatus.put(RealmsDataFetcher.Task.TRIAL_AVAILABLE, false);
			this.trialAvailableScheduledFuture = this.trialAvailabilityTask.schedule(this.scheduler);
			this.fetchStatus.put(RealmsDataFetcher.Task.UNREAD_NEWS, false);
			this.unreadNewsScheduledFuture = this.unreadNewsTask.schedule(this.scheduler);
		}
	}

	public boolean isFetchedSinceLastTry(RealmsDataFetcher.Task task) {
		Boolean boolean_ = (Boolean)this.fetchStatus.get(task);
		return boolean_ != null && boolean_;
	}

	public void markClean() {
		this.fetchStatus.replaceAll((task, boolean_) -> false);
	}

	public synchronized void forceUpdate() {
		this.stop();
		this.init();
	}

	public synchronized List<RealmsServer> getServers() {
		return Lists.<RealmsServer>newArrayList(this.servers);
	}

	public synchronized int getPendingInvitesCount() {
		return this.pendingInvitesCount;
	}

	public synchronized boolean isTrialAvailable() {
		return this.trialAvailable;
	}

	public synchronized RealmsServerPlayerLists getLivestats() {
		return this.livestats;
	}

	public synchronized boolean hasUnreadNews() {
		return this.hasUnreadNews;
	}

	public synchronized String newsLink() {
		return this.newsLink;
	}

	public synchronized void stop() {
		this.stopped = true;
		this.cancelTasks();
	}

	private void scheduleTasks() {
		for (RealmsDataFetcher.Task task : RealmsDataFetcher.Task.values()) {
			this.fetchStatus.put(task, false);
		}

		this.serverListScheduledFuture = this.serverListUpdateTask.schedule(this.scheduler);
		this.pendingInviteScheduledFuture = this.pendingInviteUpdateTask.schedule(this.scheduler);
		this.trialAvailableScheduledFuture = this.trialAvailabilityTask.schedule(this.scheduler);
		this.liveStatsScheduledFuture = this.liveStatsTask.schedule(this.scheduler);
		this.unreadNewsScheduledFuture = this.unreadNewsTask.schedule(this.scheduler);
	}

	private void cancelTasks() {
		Stream.of(
				this.serverListScheduledFuture,
				this.pendingInviteScheduledFuture,
				this.trialAvailableScheduledFuture,
				this.liveStatsScheduledFuture,
				this.unreadNewsScheduledFuture
			)
			.filter(Objects::nonNull)
			.forEach(scheduledFuture -> {
				try {
					scheduledFuture.cancel(false);
				} catch (Exception var2) {
					LOGGER.error("Failed to cancel Realms task", (Throwable)var2);
				}
			});
	}

	private synchronized void setServers(List<RealmsServer> list) {
		int i = 0;

		for (RealmsServer realmsServer : this.removedServers) {
			if (list.remove(realmsServer)) {
				i++;
			}
		}

		if (i == 0) {
			this.removedServers.clear();
		}

		this.servers = list;
	}

	public synchronized void removeItem(RealmsServer realmsServer) {
		this.servers.remove(realmsServer);
		this.removedServers.add(realmsServer);
	}

	private boolean isActive() {
		return !this.stopped;
	}

	private void updateServersList() {
		try {
			List<RealmsServer> list = this.realmsClient.listWorlds().servers;
			if (list != null) {
				list.sort(new RealmsServer.McoServerComparator(this.minecraft.getUser().getName()));
				this.setServers(list);
				this.fetchStatus.put(RealmsDataFetcher.Task.SERVER_LIST, true);
			} else {
				LOGGER.warn("Realms server list was null");
			}
		} catch (Exception var2) {
			this.fetchStatus.put(RealmsDataFetcher.Task.SERVER_LIST, true);
			LOGGER.error("Couldn't get server list", (Throwable)var2);
		}
	}

	private void updatePendingInvites() {
		try {
			this.pendingInvitesCount = this.realmsClient.pendingInvitesCount();
			this.fetchStatus.put(RealmsDataFetcher.Task.PENDING_INVITE, true);
		} catch (Exception var2) {
			LOGGER.error("Couldn't get pending invite count", (Throwable)var2);
		}
	}

	private void updateTrialAvailable() {
		try {
			this.trialAvailable = this.realmsClient.trialAvailable();
			this.fetchStatus.put(RealmsDataFetcher.Task.TRIAL_AVAILABLE, true);
		} catch (Exception var2) {
			LOGGER.error("Couldn't get trial availability", (Throwable)var2);
		}
	}

	private void updateLiveStats() {
		try {
			this.livestats = this.realmsClient.getLiveStats();
			this.fetchStatus.put(RealmsDataFetcher.Task.LIVE_STATS, true);
		} catch (Exception var2) {
			LOGGER.error("Couldn't get live stats", (Throwable)var2);
		}
	}

	private void updateUnreadNews() {
		try {
			RealmsPersistence.RealmsPersistenceData realmsPersistenceData = this.fetchAndUpdateNewsStorage();
			this.hasUnreadNews = realmsPersistenceData.hasUnreadNews;
			this.newsLink = realmsPersistenceData.newsLink;
			this.fetchStatus.put(RealmsDataFetcher.Task.UNREAD_NEWS, true);
		} catch (Exception var2) {
			LOGGER.error("Couldn't update unread news", (Throwable)var2);
		}
	}

	private RealmsPersistence.RealmsPersistenceData fetchAndUpdateNewsStorage() {
		RealmsPersistence.RealmsPersistenceData realmsPersistenceData;
		try {
			RealmsNews realmsNews = this.realmsClient.getNews();
			realmsPersistenceData = new RealmsPersistence.RealmsPersistenceData();
			realmsPersistenceData.newsLink = realmsNews.newsLink;
		} catch (Exception var4) {
			LOGGER.warn("Failed fetching news from Realms, falling back to local cache", (Throwable)var4);
			return this.newsLocalStorage.read();
		}

		RealmsPersistence.RealmsPersistenceData realmsPersistenceData2 = this.newsLocalStorage.read();
		boolean bl = realmsPersistenceData.newsLink == null || realmsPersistenceData.newsLink.equals(realmsPersistenceData2.newsLink);
		if (bl) {
			return realmsPersistenceData2;
		} else {
			realmsPersistenceData.hasUnreadNews = true;
			this.newsLocalStorage.save(realmsPersistenceData);
			return realmsPersistenceData;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Task {
		SERVER_LIST,
		PENDING_INVITE,
		TRIAL_AVAILABLE,
		LIVE_STATS,
		UNREAD_NEWS;
	}
}
