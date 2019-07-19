/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.mojang.bridge.Bridge;
import com.mojang.bridge.game.GameSession;
import com.mojang.bridge.game.GameVersion;
import com.mojang.bridge.game.Language;
import com.mojang.bridge.game.PerformanceMetrics;
import com.mojang.bridge.game.RunningGame;
import com.mojang.bridge.launcher.Launcher;
import com.mojang.bridge.launcher.SessionEventListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Session;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.util.FrameTimer;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Game
implements RunningGame {
    private final Minecraft minecraft;
    @Nullable
    private final Launcher launcher;
    private SessionEventListener listener = SessionEventListener.NONE;

    public Game(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.launcher = Bridge.getLauncher();
        if (this.launcher != null) {
            this.launcher.registerGame(this);
        }
    }

    @Override
    public GameVersion getVersion() {
        return SharedConstants.getCurrentVersion();
    }

    @Override
    public Language getSelectedLanguage() {
        return this.minecraft.getLanguageManager().getSelected();
    }

    @Override
    @Nullable
    public GameSession getCurrentSession() {
        MultiPlayerLevel multiPlayerLevel = this.minecraft.level;
        return multiPlayerLevel == null ? null : new Session(multiPlayerLevel, this.minecraft.player, this.minecraft.player.connection);
    }

    @Override
    public PerformanceMetrics getPerformanceMetrics() {
        FrameTimer frameTimer = this.minecraft.getFrameTimer();
        long l = Integer.MAX_VALUE;
        long m = Integer.MIN_VALUE;
        long n = 0L;
        for (long o : frameTimer.getLog()) {
            l = Math.min(l, o);
            m = Math.max(m, o);
            n += o;
        }
        return new Metrics((int)l, (int)m, (int)(n / (long)frameTimer.getLog().length), frameTimer.getLog().length);
    }

    @Override
    public void setSessionEventListener(SessionEventListener sessionEventListener) {
        this.listener = sessionEventListener;
    }

    public void onStartGameSession() {
        this.listener.onStartGameSession(this.getCurrentSession());
    }

    public void onLeaveGameSession() {
        this.listener.onLeaveGameSession(this.getCurrentSession());
    }

    @Environment(value=EnvType.CLIENT)
    static class Metrics
    implements PerformanceMetrics {
        private final int min;
        private final int max;
        private final int average;
        private final int samples;

        public Metrics(int i, int j, int k, int l) {
            this.min = i;
            this.max = j;
            this.average = k;
            this.samples = l;
        }

        @Override
        public int getMinTime() {
            return this.min;
        }

        @Override
        public int getMaxTime() {
            return this.max;
        }

        @Override
        public int getAverageTime() {
            return this.average;
        }

        @Override
        public int getSampleCount() {
            return this.samples;
        }
    }
}

