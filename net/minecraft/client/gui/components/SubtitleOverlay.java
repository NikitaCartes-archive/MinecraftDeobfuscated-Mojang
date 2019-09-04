/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class SubtitleOverlay
extends GuiComponent
implements SoundEventListener {
    private final Minecraft minecraft;
    private final List<Subtitle> subtitles = Lists.newArrayList();
    private boolean isListening;

    public SubtitleOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render() {
        if (!this.isListening && this.minecraft.options.showSubtitles) {
            this.minecraft.getSoundManager().addListener(this);
            this.isListening = true;
        } else if (this.isListening && !this.minecraft.options.showSubtitles) {
            this.minecraft.getSoundManager().removeListener(this);
            this.isListening = false;
        }
        if (!this.isListening || this.subtitles.isEmpty()) {
            return;
        }
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Vec3 vec3 = new Vec3(this.minecraft.player.x, this.minecraft.player.y + (double)this.minecraft.player.getEyeHeight(), this.minecraft.player.z);
        Vec3 vec32 = new Vec3(0.0, 0.0, -1.0).xRot(-this.minecraft.player.xRot * ((float)Math.PI / 180)).yRot(-this.minecraft.player.yRot * ((float)Math.PI / 180));
        Vec3 vec33 = new Vec3(0.0, 1.0, 0.0).xRot(-this.minecraft.player.xRot * ((float)Math.PI / 180)).yRot(-this.minecraft.player.yRot * ((float)Math.PI / 180));
        Vec3 vec34 = vec32.cross(vec33);
        int i = 0;
        int j = 0;
        Iterator<Subtitle> iterator = this.subtitles.iterator();
        while (iterator.hasNext()) {
            Subtitle subtitle = iterator.next();
            if (subtitle.getTime() + 3000L <= Util.getMillis()) {
                iterator.remove();
                continue;
            }
            j = Math.max(j, this.minecraft.font.width(subtitle.getText()));
        }
        j += this.minecraft.font.width("<") + this.minecraft.font.width(" ") + this.minecraft.font.width(">") + this.minecraft.font.width(" ");
        for (Subtitle subtitle : this.subtitles) {
            int k = 255;
            String string = subtitle.getText();
            Vec3 vec35 = subtitle.getLocation().subtract(vec3).normalize();
            double d = -vec34.dot(vec35);
            double e = -vec32.dot(vec35);
            boolean bl = e > 0.5;
            int l = j / 2;
            int m = this.minecraft.font.lineHeight;
            int n = m / 2;
            float f = 1.0f;
            int o = this.minecraft.font.width(string);
            int p = Mth.floor(Mth.clampedLerp(255.0, 75.0, (float)(Util.getMillis() - subtitle.getTime()) / 3000.0f));
            int q = p << 16 | p << 8 | p;
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)this.minecraft.window.getGuiScaledWidth() - (float)l * 1.0f - 2.0f, (float)(this.minecraft.window.getGuiScaledHeight() - 30) - (float)(i * (m + 1)) * 1.0f, 0.0f);
            RenderSystem.scalef(1.0f, 1.0f, 1.0f);
            SubtitleOverlay.fill(-l - 1, -n - 1, l + 1, n + 1, this.minecraft.options.getBackgroundColor(0.8f));
            RenderSystem.enableBlend();
            if (!bl) {
                if (d > 0.0) {
                    this.minecraft.font.draw(">", l - this.minecraft.font.width(">"), -n, q + -16777216);
                } else if (d < 0.0) {
                    this.minecraft.font.draw("<", -l, -n, q + -16777216);
                }
            }
            this.minecraft.font.draw(string, -o / 2, -n, q + -16777216);
            RenderSystem.popMatrix();
            ++i;
        }
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    @Override
    public void onPlaySound(SoundInstance soundInstance, WeighedSoundEvents weighedSoundEvents) {
        if (weighedSoundEvents.getSubtitle() == null) {
            return;
        }
        String string = weighedSoundEvents.getSubtitle().getColoredString();
        if (!this.subtitles.isEmpty()) {
            for (Subtitle subtitle : this.subtitles) {
                if (!subtitle.getText().equals(string)) continue;
                subtitle.refresh(new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ()));
                return;
            }
        }
        this.subtitles.add(new Subtitle(string, new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ())));
    }

    @Environment(value=EnvType.CLIENT)
    public class Subtitle {
        private final String text;
        private long time;
        private Vec3 location;

        public Subtitle(String string, Vec3 vec3) {
            this.text = string;
            this.location = vec3;
            this.time = Util.getMillis();
        }

        public String getText() {
            return this.text;
        }

        public long getTime() {
            return this.time;
        }

        public Vec3 getLocation() {
            return this.location;
        }

        public void refresh(Vec3 vec3) {
            this.location = vec3;
            this.time = Util.getMillis();
        }
    }
}

