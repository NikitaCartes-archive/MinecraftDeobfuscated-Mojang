package net.minecraft.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.math.LongMath;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class PeriodicNotificationManager
	extends SimplePreparableReloadListener<Map<String, List<PeriodicNotificationManager.Notification>>>
	implements AutoCloseable {
	private static final Codec<Map<String, List<PeriodicNotificationManager.Notification>>> CODEC = Codec.unboundedMap(
		Codec.STRING,
		RecordCodecBuilder.<PeriodicNotificationManager.Notification>create(
				instance -> instance.group(
							Codec.LONG.optionalFieldOf("delay", Long.valueOf(0L)).forGetter(PeriodicNotificationManager.Notification::delay),
							Codec.LONG.fieldOf("period").forGetter(PeriodicNotificationManager.Notification::period),
							Codec.STRING.fieldOf("title").forGetter(PeriodicNotificationManager.Notification::title),
							Codec.STRING.fieldOf("message").forGetter(PeriodicNotificationManager.Notification::message)
						)
						.apply(instance, PeriodicNotificationManager.Notification::new)
			)
			.listOf()
	);
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ResourceLocation notifications;
	private final Object2BooleanFunction<String> selector;
	@Nullable
	private java.util.Timer timer;
	@Nullable
	private PeriodicNotificationManager.NotificationTask notificationTask;

	public PeriodicNotificationManager(ResourceLocation resourceLocation, Object2BooleanFunction<String> object2BooleanFunction) {
		this.notifications = resourceLocation;
		this.selector = object2BooleanFunction;
	}

	protected Map<String, List<PeriodicNotificationManager.Notification>> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		try {
			Resource resource = resourceManager.getResource(this.notifications);

			Map var5;
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

				try {
					var5 = (Map)CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(bufferedReader)).result().orElseThrow();
				} catch (Throwable var9) {
					try {
						bufferedReader.close();
					} catch (Throwable var8) {
						var9.addSuppressed(var8);
					}

					throw var9;
				}

				bufferedReader.close();
			} catch (Throwable var10) {
				if (resource != null) {
					try {
						resource.close();
					} catch (Throwable var7) {
						var10.addSuppressed(var7);
					}
				}

				throw var10;
			}

			if (resource != null) {
				resource.close();
			}

			return var5;
		} catch (Exception var11) {
			LOGGER.warn("Failed to load {}", this.notifications, var11);
			return ImmutableMap.of();
		}
	}

	protected void apply(Map<String, List<PeriodicNotificationManager.Notification>> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		List<PeriodicNotificationManager.Notification> list = (List<PeriodicNotificationManager.Notification>)map.entrySet()
			.stream()
			.filter(entry -> this.selector.apply((String)entry.getKey()))
			.map(Entry::getValue)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
		if (list.isEmpty()) {
			this.stopTimer();
		} else if (list.stream().anyMatch(notification -> notification.period == 0L)) {
			Util.logAndPauseIfInIde("A periodic notification in " + this.notifications + " has a period of zero minutes");
			this.stopTimer();
		} else {
			long l = this.calculateInitialDelay(list);
			long m = this.calculateOptimalPeriod(list, l);
			if (this.timer == null) {
				this.timer = new java.util.Timer();
			}

			if (this.notificationTask == null) {
				this.notificationTask = new PeriodicNotificationManager.NotificationTask(list, l, m);
			} else {
				this.notificationTask = this.notificationTask.reset(list, m);
			}

			this.timer.scheduleAtFixedRate(this.notificationTask, TimeUnit.MINUTES.toMillis(l), TimeUnit.MINUTES.toMillis(m));
		}
	}

	public void close() {
		this.stopTimer();
	}

	private void stopTimer() {
		if (this.timer != null) {
			this.timer.cancel();
		}
	}

	private long calculateOptimalPeriod(List<PeriodicNotificationManager.Notification> list, long l) {
		return list.stream().mapToLong(notification -> {
			long m = notification.delay - l;
			return LongMath.gcd(m, notification.period);
		}).reduce(LongMath::gcd).orElseThrow(() -> new IllegalStateException("Empty notifications from: " + this.notifications));
	}

	private long calculateInitialDelay(List<PeriodicNotificationManager.Notification> list) {
		return list.stream().mapToLong(notification -> notification.delay).min().orElse(0L);
	}

	@Environment(EnvType.CLIENT)
	public static record Notification(long delay, long period, String title, String message) {
	}

	@Environment(EnvType.CLIENT)
	static class NotificationTask extends TimerTask {
		private final Minecraft minecraft = Minecraft.getInstance();
		private final List<PeriodicNotificationManager.Notification> notifications;
		private final long period;
		private final AtomicLong elapsed;

		public NotificationTask(List<PeriodicNotificationManager.Notification> list, long l, long m) {
			this.notifications = list;
			this.period = m;
			this.elapsed = new AtomicLong(l);
		}

		public PeriodicNotificationManager.NotificationTask reset(List<PeriodicNotificationManager.Notification> list, long l) {
			this.cancel();
			return new PeriodicNotificationManager.NotificationTask(list, this.elapsed.get(), l);
		}

		public void run() {
			long l = this.elapsed.getAndAdd(this.period);
			long m = this.elapsed.get();

			for (PeriodicNotificationManager.Notification notification : this.notifications) {
				if (m >= notification.delay) {
					long n = l / notification.period;
					long o = m / notification.period;
					if (n != o) {
						this.minecraft
							.execute(
								() -> SystemToast.add(
										Minecraft.getInstance().getToasts(),
										SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
										new TranslatableComponent(notification.title, o),
										new TranslatableComponent(notification.message, o)
									)
							);
						return;
					}
				}
			}
		}
	}
}
