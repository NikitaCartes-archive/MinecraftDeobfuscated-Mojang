package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.audio.ListenerTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class SubtitleOverlay implements SoundEventListener {
	private static final long DISPLAY_TIME = 3000L;
	private final Minecraft minecraft;
	private final List<SubtitleOverlay.Subtitle> subtitles = Lists.<SubtitleOverlay.Subtitle>newArrayList();
	private boolean isListening;
	private final List<SubtitleOverlay.Subtitle> audibleSubtitles = new ArrayList();

	public SubtitleOverlay(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void render(GuiGraphics guiGraphics) {
		SoundManager soundManager = this.minecraft.getSoundManager();
		if (!this.isListening && this.minecraft.options.showSubtitles().get()) {
			soundManager.addListener(this);
			this.isListening = true;
		} else if (this.isListening && !this.minecraft.options.showSubtitles().get()) {
			soundManager.removeListener(this);
			this.isListening = false;
		}

		if (this.isListening) {
			ListenerTransform listenerTransform = soundManager.getListenerTransform();
			Vec3 vec3 = listenerTransform.position();
			Vec3 vec32 = listenerTransform.forward();
			Vec3 vec33 = listenerTransform.right();
			this.audibleSubtitles.clear();

			for (SubtitleOverlay.Subtitle subtitle : this.subtitles) {
				if (subtitle.isAudibleFrom(vec3)) {
					this.audibleSubtitles.add(subtitle);
				}
			}

			if (!this.audibleSubtitles.isEmpty()) {
				int i = 0;
				int j = 0;
				double d = this.minecraft.options.notificationDisplayTime().get();
				Iterator<SubtitleOverlay.Subtitle> iterator = this.audibleSubtitles.iterator();

				while (iterator.hasNext()) {
					SubtitleOverlay.Subtitle subtitle2 = (SubtitleOverlay.Subtitle)iterator.next();
					if ((double)subtitle2.getTime() + 3000.0 * d <= (double)Util.getMillis()) {
						iterator.remove();
					} else {
						j = Math.max(j, this.minecraft.font.width(subtitle2.getText()));
					}
				}

				j += this.minecraft.font.width("<") + this.minecraft.font.width(" ") + this.minecraft.font.width(">") + this.minecraft.font.width(" ");

				for (SubtitleOverlay.Subtitle subtitle2 : this.audibleSubtitles) {
					int k = 255;
					Component component = subtitle2.getText();
					Vec3 vec34 = subtitle2.getLocation().subtract(vec3).normalize();
					double e = vec33.dot(vec34);
					double f = vec32.dot(vec34);
					boolean bl = f > 0.5;
					int l = j / 2;
					int m = 9;
					int n = m / 2;
					float g = 1.0F;
					int o = this.minecraft.font.width(component);
					int p = Mth.floor(Mth.clampedLerp(255.0F, 75.0F, (float)(Util.getMillis() - subtitle2.getTime()) / (float)(3000.0 * d)));
					int q = p << 16 | p << 8 | p;
					guiGraphics.pose().pushPose();
					guiGraphics.pose()
						.translate((float)guiGraphics.guiWidth() - (float)l * 1.0F - 2.0F, (float)(guiGraphics.guiHeight() - 35) - (float)(i * (m + 1)) * 1.0F, 0.0F);
					guiGraphics.pose().scale(1.0F, 1.0F, 1.0F);
					guiGraphics.fill(-l - 1, -n - 1, l + 1, n + 1, this.minecraft.options.getBackgroundColor(0.8F));
					int r = q + -16777216;
					if (!bl) {
						if (e > 0.0) {
							guiGraphics.drawString(this.minecraft.font, ">", l - this.minecraft.font.width(">"), -n, r);
						} else if (e < 0.0) {
							guiGraphics.drawString(this.minecraft.font, "<", -l, -n, r);
						}
					}

					guiGraphics.drawString(this.minecraft.font, component, -o / 2, -n, r);
					guiGraphics.pose().popPose();
					i++;
				}
			}
		}
	}

	@Override
	public void onPlaySound(SoundInstance soundInstance, WeighedSoundEvents weighedSoundEvents, float f) {
		if (weighedSoundEvents.getSubtitle() != null) {
			Component component = weighedSoundEvents.getSubtitle();
			if (!this.subtitles.isEmpty()) {
				for (SubtitleOverlay.Subtitle subtitle : this.subtitles) {
					if (subtitle.getText().equals(component)) {
						subtitle.refresh(new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ()));
						return;
					}
				}
			}

			this.subtitles.add(new SubtitleOverlay.Subtitle(component, f, new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ())));
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Subtitle {
		private final Component text;
		private final float range;
		private long time;
		private Vec3 location;

		public Subtitle(Component component, float f, Vec3 vec3) {
			this.text = component;
			this.range = f;
			this.location = vec3;
			this.time = Util.getMillis();
		}

		public Component getText() {
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

		public boolean isAudibleFrom(Vec3 vec3) {
			return Float.isInfinite(this.range) || vec3.closerThan(this.location, (double)this.range);
		}
	}
}
