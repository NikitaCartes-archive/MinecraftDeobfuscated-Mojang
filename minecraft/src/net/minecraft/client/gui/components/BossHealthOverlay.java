package net.minecraft.client.gui.components;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

@Environment(EnvType.CLIENT)
public class BossHealthOverlay {
	private static final int BAR_WIDTH = 182;
	private static final int BAR_HEIGHT = 5;
	private static final ResourceLocation[] BAR_BACKGROUND_SPRITES = new ResourceLocation[]{
		new ResourceLocation("boss_bar/pink_background"),
		new ResourceLocation("boss_bar/blue_background"),
		new ResourceLocation("boss_bar/red_background"),
		new ResourceLocation("boss_bar/green_background"),
		new ResourceLocation("boss_bar/yellow_background"),
		new ResourceLocation("boss_bar/purple_background"),
		new ResourceLocation("boss_bar/white_background")
	};
	private static final ResourceLocation[] BAR_PROGRESS_SPRITES = new ResourceLocation[]{
		new ResourceLocation("boss_bar/pink_progress"),
		new ResourceLocation("boss_bar/blue_progress"),
		new ResourceLocation("boss_bar/red_progress"),
		new ResourceLocation("boss_bar/green_progress"),
		new ResourceLocation("boss_bar/yellow_progress"),
		new ResourceLocation("boss_bar/purple_progress"),
		new ResourceLocation("boss_bar/white_progress")
	};
	private static final ResourceLocation[] OVERLAY_BACKGROUND_SPRITES = new ResourceLocation[]{
		new ResourceLocation("boss_bar/notched_6_background"),
		new ResourceLocation("boss_bar/notched_10_background"),
		new ResourceLocation("boss_bar/notched_12_background"),
		new ResourceLocation("boss_bar/notched_20_background")
	};
	private static final ResourceLocation[] OVERLAY_PROGRESS_SPRITES = new ResourceLocation[]{
		new ResourceLocation("boss_bar/notched_6_progress"),
		new ResourceLocation("boss_bar/notched_10_progress"),
		new ResourceLocation("boss_bar/notched_12_progress"),
		new ResourceLocation("boss_bar/notched_20_progress")
	};
	private final Minecraft minecraft;
	final Map<UUID, LerpingBossEvent> events = Maps.<UUID, LerpingBossEvent>newLinkedHashMap();

	public BossHealthOverlay(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void render(GuiGraphics guiGraphics) {
		if (!this.events.isEmpty()) {
			int i = guiGraphics.guiWidth();
			int j = 12;

			for (LerpingBossEvent lerpingBossEvent : this.events.values()) {
				int k = i / 2 - 91;
				this.drawBar(guiGraphics, k, j, lerpingBossEvent);
				Component component = lerpingBossEvent.getName();
				int m = this.minecraft.font.width(component);
				int n = i / 2 - m / 2;
				int o = j - 9;
				guiGraphics.drawString(this.minecraft.font, component, n, o, 16777215);
				j += 10 + 9;
				if (j >= guiGraphics.guiHeight() / 3) {
					break;
				}
			}
		}
	}

	private void drawBar(GuiGraphics guiGraphics, int i, int j, BossEvent bossEvent) {
		this.drawBar(guiGraphics, i, j, bossEvent, 182, BAR_BACKGROUND_SPRITES, OVERLAY_BACKGROUND_SPRITES);
		int k = Mth.lerpDiscrete(bossEvent.getProgress(), 0, 182);
		if (k > 0) {
			this.drawBar(guiGraphics, i, j, bossEvent, k, BAR_PROGRESS_SPRITES, OVERLAY_PROGRESS_SPRITES);
		}
	}

	private void drawBar(
		GuiGraphics guiGraphics, int i, int j, BossEvent bossEvent, int k, ResourceLocation[] resourceLocations, ResourceLocation[] resourceLocations2
	) {
		guiGraphics.blitSprite(resourceLocations[bossEvent.getColor().ordinal()], 182, 5, 0, 0, i, j, k, 5);
		if (bossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
			RenderSystem.enableBlend();
			guiGraphics.blitSprite(resourceLocations2[bossEvent.getOverlay().ordinal() - 1], 182, 5, 0, 0, i, j, k, 5);
			RenderSystem.disableBlend();
		}
	}

	public void update(ClientboundBossEventPacket clientboundBossEventPacket) {
		clientboundBossEventPacket.dispatch(
			new ClientboundBossEventPacket.Handler() {
				@Override
				public void add(
					UUID uUID,
					Component component,
					float f,
					BossEvent.BossBarColor bossBarColor,
					BossEvent.BossBarOverlay bossBarOverlay,
					boolean bl,
					boolean bl2,
					boolean bl3
				) {
					BossHealthOverlay.this.events.put(uUID, new LerpingBossEvent(uUID, component, f, bossBarColor, bossBarOverlay, bl, bl2, bl3));
				}

				@Override
				public void remove(UUID uUID) {
					BossHealthOverlay.this.events.remove(uUID);
				}

				@Override
				public void updateProgress(UUID uUID, float f) {
					((LerpingBossEvent)BossHealthOverlay.this.events.get(uUID)).setProgress(f);
				}

				@Override
				public void updateName(UUID uUID, Component component) {
					((LerpingBossEvent)BossHealthOverlay.this.events.get(uUID)).setName(component);
				}

				@Override
				public void updateStyle(UUID uUID, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay) {
					LerpingBossEvent lerpingBossEvent = (LerpingBossEvent)BossHealthOverlay.this.events.get(uUID);
					lerpingBossEvent.setColor(bossBarColor);
					lerpingBossEvent.setOverlay(bossBarOverlay);
				}

				@Override
				public void updateProperties(UUID uUID, boolean bl, boolean bl2, boolean bl3) {
					LerpingBossEvent lerpingBossEvent = (LerpingBossEvent)BossHealthOverlay.this.events.get(uUID);
					lerpingBossEvent.setDarkenScreen(bl);
					lerpingBossEvent.setPlayBossMusic(bl2);
					lerpingBossEvent.setCreateWorldFog(bl3);
				}
			}
		);
	}

	public void reset() {
		this.events.clear();
	}

	public boolean shouldPlayMusic() {
		if (!this.events.isEmpty()) {
			for (BossEvent bossEvent : this.events.values()) {
				if (bossEvent.shouldPlayBossMusic()) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean shouldDarkenScreen() {
		if (!this.events.isEmpty()) {
			for (BossEvent bossEvent : this.events.values()) {
				if (bossEvent.shouldDarkenScreen()) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean shouldCreateWorldFog() {
		if (!this.events.isEmpty()) {
			for (BossEvent bossEvent : this.events.values()) {
				if (bossEvent.shouldCreateWorldFog()) {
					return true;
				}
			}
		}

		return false;
	}
}
