/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public abstract class BossEvent {
    private final UUID id;
    protected Component name;
    protected float progress;
    protected BossBarColor color;
    protected BossBarOverlay overlay;
    protected boolean darkenScreen;
    protected boolean playBossMusic;
    protected boolean createWorldFog;

    public BossEvent(UUID uUID, Component component, BossBarColor bossBarColor, BossBarOverlay bossBarOverlay) {
        this.id = uUID;
        this.name = component;
        this.color = bossBarColor;
        this.overlay = bossBarOverlay;
        this.progress = 1.0f;
    }

    public UUID getId() {
        return this.id;
    }

    public Component getName() {
        return this.name;
    }

    public void setName(Component component) {
        this.name = component;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float f) {
        this.progress = f;
    }

    public BossBarColor getColor() {
        return this.color;
    }

    public void setColor(BossBarColor bossBarColor) {
        this.color = bossBarColor;
    }

    public BossBarOverlay getOverlay() {
        return this.overlay;
    }

    public void setOverlay(BossBarOverlay bossBarOverlay) {
        this.overlay = bossBarOverlay;
    }

    public boolean shouldDarkenScreen() {
        return this.darkenScreen;
    }

    public BossEvent setDarkenScreen(boolean bl) {
        this.darkenScreen = bl;
        return this;
    }

    public boolean shouldPlayBossMusic() {
        return this.playBossMusic;
    }

    public BossEvent setPlayBossMusic(boolean bl) {
        this.playBossMusic = bl;
        return this;
    }

    public BossEvent setCreateWorldFog(boolean bl) {
        this.createWorldFog = bl;
        return this;
    }

    public boolean shouldCreateWorldFog() {
        return this.createWorldFog;
    }

    public static enum BossBarOverlay {
        PROGRESS("progress"),
        NOTCHED_6("notched_6"),
        NOTCHED_10("notched_10"),
        NOTCHED_12("notched_12"),
        NOTCHED_20("notched_20");

        private final String name;

        private BossBarOverlay(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        public static BossBarOverlay byName(String string) {
            for (BossBarOverlay bossBarOverlay : BossBarOverlay.values()) {
                if (!bossBarOverlay.name.equals(string)) continue;
                return bossBarOverlay;
            }
            return PROGRESS;
        }
    }

    public static enum BossBarColor {
        PINK("pink", ChatFormatting.RED),
        BLUE("blue", ChatFormatting.BLUE),
        RED("red", ChatFormatting.DARK_RED),
        GREEN("green", ChatFormatting.GREEN),
        YELLOW("yellow", ChatFormatting.YELLOW),
        PURPLE("purple", ChatFormatting.DARK_BLUE),
        WHITE("white", ChatFormatting.WHITE);

        private final String name;
        private final ChatFormatting formatting;

        private BossBarColor(String string2, ChatFormatting chatFormatting) {
            this.name = string2;
            this.formatting = chatFormatting;
        }

        public ChatFormatting getFormatting() {
            return this.formatting;
        }

        public String getName() {
            return this.name;
        }

        public static BossBarColor byName(String string) {
            for (BossBarColor bossBarColor : BossBarColor.values()) {
                if (!bossBarColor.name.equals(string)) continue;
                return bossBarColor;
            }
            return WHITE;
        }
    }
}

