package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class SubtitleOverlay extends GuiComponent implements SoundEventListener {
	private final Minecraft minecraft;
	private final List<SubtitleOverlay.Subtitle> subtitles = Lists.<SubtitleOverlay.Subtitle>newArrayList();
	private boolean isListening;

	public SubtitleOverlay(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void render(PoseStack poseStack) {
		if (!this.isListening && this.minecraft.options.showSubtitles) {
			this.minecraft.getSoundManager().addListener(this);
			this.isListening = true;
		} else if (this.isListening && !this.minecraft.options.showSubtitles) {
			this.minecraft.getSoundManager().removeListener(this);
			this.isListening = false;
		}

		if (this.isListening && !this.subtitles.isEmpty()) {
			RenderSystem.pushMatrix();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			Vec3 vec3 = new Vec3(this.minecraft.player.getX(), this.minecraft.player.getEyeY(), this.minecraft.player.getZ());
			Vec3 vec32 = new Vec3(0.0, 0.0, -1.0)
				.xRot(-this.minecraft.player.xRot * (float) (Math.PI / 180.0))
				.yRot(-this.minecraft.player.yRot * (float) (Math.PI / 180.0));
			Vec3 vec33 = new Vec3(0.0, 1.0, 0.0)
				.xRot(-this.minecraft.player.xRot * (float) (Math.PI / 180.0))
				.yRot(-this.minecraft.player.yRot * (float) (Math.PI / 180.0));
			Vec3 vec34 = vec32.cross(vec33);
			int i = 0;
			int j = 0;
			Iterator<SubtitleOverlay.Subtitle> iterator = this.subtitles.iterator();

			while (iterator.hasNext()) {
				SubtitleOverlay.Subtitle subtitle = (SubtitleOverlay.Subtitle)iterator.next();
				if (subtitle.getTime() + 3000L <= Util.getMillis()) {
					iterator.remove();
				} else {
					j = Math.max(j, this.minecraft.font.width(subtitle.getText()));
				}
			}

			j += this.minecraft.font.width("<") + this.minecraft.font.width(" ") + this.minecraft.font.width(">") + this.minecraft.font.width(" ");

			for (SubtitleOverlay.Subtitle subtitle : this.subtitles) {
				int k = 255;
				Component component = subtitle.getText();
				Vec3 vec35 = subtitle.getLocation().subtract(vec3).normalize();
				double d = -vec34.dot(vec35);
				double e = -vec32.dot(vec35);
				boolean bl = e > 0.5;
				int l = j / 2;
				int m = 9;
				int n = m / 2;
				float f = 1.0F;
				int o = this.minecraft.font.width(component);
				int p = Mth.floor(Mth.clampedLerp(255.0, 75.0, (double)((float)(Util.getMillis() - subtitle.getTime()) / 3000.0F)));
				int q = p << 16 | p << 8 | p;
				RenderSystem.pushMatrix();
				RenderSystem.translatef(
					(float)this.minecraft.getWindow().getGuiScaledWidth() - (float)l * 1.0F - 2.0F,
					(float)(this.minecraft.getWindow().getGuiScaledHeight() - 30) - (float)(i * (m + 1)) * 1.0F,
					0.0F
				);
				RenderSystem.scalef(1.0F, 1.0F, 1.0F);
				fill(poseStack, -l - 1, -n - 1, l + 1, n + 1, this.minecraft.options.getBackgroundColor(0.8F));
				RenderSystem.enableBlend();
				if (!bl) {
					if (d > 0.0) {
						this.minecraft.font.draw(poseStack, ">", (float)(l - this.minecraft.font.width(">")), (float)(-n), q + -16777216);
					} else if (d < 0.0) {
						this.minecraft.font.draw(poseStack, "<", (float)(-l), (float)(-n), q + -16777216);
					}
				}

				this.minecraft.font.draw(poseStack, component, (float)(-o / 2), (float)(-n), q + -16777216);
				RenderSystem.popMatrix();
				i++;
			}

			RenderSystem.disableBlend();
			RenderSystem.popMatrix();
		}
	}

	@Override
	public void onPlaySound(SoundInstance soundInstance, WeighedSoundEvents weighedSoundEvents) {
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

			this.subtitles.add(new SubtitleOverlay.Subtitle(component, new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ())));
		}
	}

	@Environment(EnvType.CLIENT)
	public class Subtitle {
		private final Component text;
		private long time;
		private Vec3 location;

		public Subtitle(Component component, Vec3 vec3) {
			this.text = component;
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
	}
}
