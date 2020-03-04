package com.mojang.realmsclient.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.util.RealmsPersistence;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsDataFetcher {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
	private volatile boolean stopped = true;
	private final Runnable serverListUpdateTask = new RealmsDataFetcher.ServerListUpdateTask();
	private final Runnable pendingInviteUpdateTask = new RealmsDataFetcher.PendingInviteUpdateTask();
	private final Runnable trialAvailabilityTask = new RealmsDataFetcher.TrialAvailabilityTask();
	private final Runnable liveStatsTask = new RealmsDataFetcher.LiveStatsTask();
	private final Runnable unreadNewsTask = new RealmsDataFetcher.UnreadNewsTask();
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
			this.pendingInviteScheduledFuture = this.scheduler.scheduleAtFixedRate(this.pendingInviteUpdateTask, 0L, 10L, TimeUnit.SECONDS);
			this.fetchStatus.put(RealmsDataFetcher.Task.TRIAL_AVAILABLE, false);
			this.trialAvailableScheduledFuture = this.scheduler.scheduleAtFixedRate(this.trialAvailabilityTask, 0L, 60L, TimeUnit.SECONDS);
			this.fetchStatus.put(RealmsDataFetcher.Task.UNREAD_NEWS, false);
			this.unreadNewsScheduledFuture = this.scheduler.scheduleAtFixedRate(this.unreadNewsTask, 0L, 300L, TimeUnit.SECONDS);
		}
	}

	public boolean isFetchedSinceLastTry(RealmsDataFetcher.Task task) {
		Boolean boolean_ = (Boolean)this.fetchStatus.get(task);
		return boolean_ == null ? false : boolean_;
	}

	public void markClean() {
		for (RealmsDataFetcher.Task task : this.fetchStatus.keySet()) {
			this.fetchStatus.put(task, false);
		}
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

		this.serverListScheduledFuture = this.scheduler.scheduleAtFixedRate(this.serverListUpdateTask, 0L, 60L, TimeUnit.SECONDS);
		this.pendingInviteScheduledFuture = this.scheduler.scheduleAtFixedRate(this.pendingInviteUpdateTask, 0L, 10L, TimeUnit.SECONDS);
		this.trialAvailableScheduledFuture = this.scheduler.scheduleAtFixedRate(this.trialAvailabilityTask, 0L, 60L, TimeUnit.SECONDS);
		this.liveStatsScheduledFuture = this.scheduler.scheduleAtFixedRate(this.liveStatsTask, 0L, 10L, TimeUnit.SECONDS);
		this.unreadNewsScheduledFuture = this.scheduler.scheduleAtFixedRate(this.unreadNewsTask, 0L, 300L, TimeUnit.SECONDS);
	}

	private void cancelTasks() {
		try {
			if (this.serverListScheduledFuture != null) {
				this.serverListScheduledFuture.cancel(false);
			}

			if (this.pendingInviteScheduledFuture != null) {
				this.pendingInviteScheduledFuture.cancel(false);
			}

			if (this.trialAvailableScheduledFuture != null) {
				this.trialAvailableScheduledFuture.cancel(false);
			}

			if (this.liveStatsScheduledFuture != null) {
				this.liveStatsScheduledFuture.cancel(false);
			}

			if (this.unreadNewsScheduledFuture != null) {
				this.unreadNewsScheduledFuture.cancel(false);
			}
		} catch (Exception var2) {
			LOGGER.error("Failed to cancel Realms tasks", (Throwable)var2);
		}
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

	@Environment(EnvType.CLIENT)
	class LiveStatsTask implements Runnable {
		private LiveStatsTask() {
		}

		public void run() {
			if (RealmsDataFetcher.this.isActive()) {
				this.getLiveStats();
			}
		}

		private void getLiveStats() {
			try {
				RealmsClient realmsClient = RealmsClient.create();
				RealmsDataFetcher.this.livestats = realmsClient.getLiveStats();
				RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.LIVE_STATS, true);
			} catch (Exception var2) {
				RealmsDataFetcher.LOGGER.error("Couldn't get live stats", (Throwable)var2);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class PendingInviteUpdateTask implements Runnable {
		private PendingInviteUpdateTask() {
		}

		public void run() {
			if (RealmsDataFetcher.this.isActive()) {
				this.updatePendingInvites();
			}
		}

		private void updatePendingInvites() {
			try {
				RealmsClient realmsClient = RealmsClient.create();
				RealmsDataFetcher.this.pendingInvitesCount = realmsClient.pendingInvitesCount();
				RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.PENDING_INVITE, true);
			} catch (Exception var2) {
				RealmsDataFetcher.LOGGER.error("Couldn't get pending invite count", (Throwable)var2);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class ServerListUpdateTask implements Runnable {
		private ServerListUpdateTask() {
		}

		public void run() {
			if (RealmsDataFetcher.this.isActive()) {
				this.updateServersList();
			}
		}

		private void updateServersList() {
			try {
				RealmsClient realmsClient = RealmsClient.create();
				List<RealmsServer> list = realmsClient.listWorlds().servers;
				if (list != null) {
					list.sort(new RealmsServer.McoServerComparator(Minecraft.getInstance().getUser().getName()));
					RealmsDataFetcher.this.setServers(list);
					RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.SERVER_LIST, true);
				} else {
					RealmsDataFetcher.LOGGER.warn("Realms server list was null or empty");
				}
			} catch (Exception var3) {
				RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.SERVER_LIST, true);
				RealmsDataFetcher.LOGGER.error("Couldn't get server list", (Throwable)var3);
			}
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

	@Environment(EnvType.CLIENT)
	class TrialAvailabilityTask implements Runnable {
		private TrialAvailabilityTask() {
		}

		public void run() {
			if (RealmsDataFetcher.this.isActive()) {
				this.getTrialAvailable();
			}
		}

		private void getTrialAvailable() {
			try {
				RealmsClient realmsClient = RealmsClient.create();
				RealmsDataFetcher.this.trialAvailable = realmsClient.trialAvailable();
				RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.TRIAL_AVAILABLE, true);
			} catch (Exception var2) {
				RealmsDataFetcher.LOGGER.error("Couldn't get trial availability", (Throwable)var2);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class UnreadNewsTask implements Runnable {
		private UnreadNewsTask() {
		}

		public void run() {
			if (RealmsDataFetcher.this.isActive()) {
				this.getUnreadNews();
			}
		}

		private void getUnreadNews() {
			try {
				RealmsClient realmsClient = RealmsClient.create();
				RealmsNews realmsNews = null;

				try {
					realmsNews = realmsClient.getNews();
				} catch (Exception var5) {
				}

				RealmsPersistence.RealmsPersistenceData realmsPersistenceData = RealmsPersistence.readFile();
				if (realmsNews != null) {
					String string = realmsNews.newsLink;
					if (string != null && !string.equals(realmsPersistenceData.newsLink)) {
						realmsPersistenceData.hasUnreadNews = true;
						realmsPersistenceData.newsLink = string;
						RealmsPersistence.writeFile(realmsPersistenceData);
					}
				}

				RealmsDataFetcher.this.hasUnreadNews = realmsPersistenceData.hasUnreadNews;
				RealmsDataFetcher.this.newsLink = realmsPersistenceData.newsLink;
				RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.UNREAD_NEWS, true);
			} catch (Exception var6) {
				RealmsDataFetcher.LOGGER.error("Couldn't get unread news", (Throwable)var6);
			}
		}
	}
}
