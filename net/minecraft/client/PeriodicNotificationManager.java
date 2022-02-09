/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.math.LongMath;
import com.google.gson.JsonParser;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PeriodicNotificationManager
extends SimplePreparableReloadListener<Map<String, List<Notification>>>
implements AutoCloseable {
    private static final Codec<Map<String, List<Notification>>> CODEC = Codec.unboundedMap(Codec.STRING, RecordCodecBuilder.create(instance -> instance.group(Codec.LONG.optionalFieldOf("delay", 0L).forGetter(Notification::delay), ((MapCodec)Codec.LONG.fieldOf("period")).forGetter(Notification::period), ((MapCodec)Codec.STRING.fieldOf("title")).forGetter(Notification::title), ((MapCodec)Codec.STRING.fieldOf("message")).forGetter(Notification::message)).apply((Applicative<Notification, ?>)instance, Notification::new)).listOf());
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation notifications;
    private final Object2BooleanFunction<String> selector;
    @Nullable
    private Timer timer;
    @Nullable
    private NotificationTask notificationTask;

    public PeriodicNotificationManager(ResourceLocation resourceLocation, Object2BooleanFunction<String> object2BooleanFunction) {
        this.notifications = resourceLocation;
        this.selector = object2BooleanFunction;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Override
    protected Map<String, List<Notification>> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        try (Resource resource = resourceManager.getResource(this.notifications);){
            Map map;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));){
                map = (Map)CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(bufferedReader)).result().orElseThrow();
            }
            return map;
        } catch (Exception exception) {
            LOGGER.warn("Failed to load {}", (Object)this.notifications, (Object)exception);
            return ImmutableMap.of();
        }
    }

    @Override
    protected void apply(Map<String, List<Notification>> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        List<Notification> list = map.entrySet().stream().filter(entry -> (Boolean)this.selector.apply((String)entry.getKey())).map(Map.Entry::getValue).flatMap(Collection::stream).collect(Collectors.toList());
        if (list.isEmpty()) {
            this.stopTimer();
            return;
        }
        if (list.stream().anyMatch(notification -> notification.period == 0L)) {
            Util.logAndPauseIfInIde("A periodic notification in " + this.notifications + " has a period of zero minutes");
            this.stopTimer();
            return;
        }
        long l = this.calculateInitialDelay(list);
        long m = this.calculateOptimalPeriod(list, l);
        if (this.timer == null) {
            this.timer = new Timer();
        }
        this.notificationTask = this.notificationTask == null ? new NotificationTask(list, l, m) : this.notificationTask.reset(list, m);
        this.timer.scheduleAtFixedRate((TimerTask)this.notificationTask, TimeUnit.MINUTES.toMillis(l), TimeUnit.MINUTES.toMillis(m));
    }

    @Override
    public void close() {
        this.stopTimer();
    }

    private void stopTimer() {
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    private long calculateOptimalPeriod(List<Notification> list, long l) {
        return list.stream().mapToLong(notification -> {
            long m = notification.delay - l;
            return LongMath.gcd(m, notification.period);
        }).reduce(LongMath::gcd).orElseThrow(() -> new IllegalStateException("Empty notifications from: " + this.notifications));
    }

    private long calculateInitialDelay(List<Notification> list) {
        return list.stream().mapToLong(notification -> notification.delay).min().orElse(0L);
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    @Environment(value=EnvType.CLIENT)
    static class NotificationTask
    extends TimerTask {
        private final Minecraft minecraft = Minecraft.getInstance();
        private final List<Notification> notifications;
        private final long period;
        private final AtomicLong elapsed;

        public NotificationTask(List<Notification> list, long l, long m) {
            this.notifications = list;
            this.period = m;
            this.elapsed = new AtomicLong(l);
        }

        public NotificationTask reset(List<Notification> list, long l) {
            this.cancel();
            return new NotificationTask(list, this.elapsed.get(), l);
        }

        @Override
        public void run() {
            long l = this.elapsed.getAndAdd(this.period);
            long m = this.elapsed.get();
            for (Notification notification : this.notifications) {
                long o;
                long n;
                if (m < notification.delay || (n = l / notification.period) == (o = m / notification.period)) continue;
                this.minecraft.execute(() -> SystemToast.add(Minecraft.getInstance().getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent(notification.title, o), new TranslatableComponent(notification.message, o)));
                return;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Notification(long delay, long period, String title, String message) {
    }
}

