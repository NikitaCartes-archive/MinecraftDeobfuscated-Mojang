package net.minecraft.client.gui.components;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;

@Environment(EnvType.CLIENT)
public class BossHealthOverlay extends GuiComponent {
	private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");
	private final Minecraft minecraft;
	private final Map<UUID, LerpingBossEvent> events = Maps.<UUID, LerpingBossEvent>newLinkedHashMap();

	public BossHealthOverlay(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void render() {
		if (!this.events.isEmpty()) {
			int i = this.minecraft.window.getGuiScaledWidth();
			int j = 12;

			for (LerpingBossEvent lerpingBossEvent : this.events.values()) {
				int k = i / 2 - 91;
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.minecraft.getTextureManager().bind(GUI_BARS_LOCATION);
				this.drawBar(k, j, lerpingBossEvent);
				String string = lerpingBossEvent.getName().getColoredString();
				int m = this.minecraft.font.width(string);
				int n = i / 2 - m / 2;
				int o = j - 9;
				this.minecraft.font.drawShadow(string, (float)n, (float)o, 16777215);
				j += 10 + 9;
				if (j >= this.minecraft.window.getGuiScaledHeight() / 3) {
					break;
				}
			}
		}
	}

	private void drawBar(int i, int j, BossEvent bossEvent) {
		this.blit(i, j, 0, bossEvent.getColor().ordinal() * 5 * 2, 182, 5);
		if (bossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
			this.blit(i, j, 0, 80 + (bossEvent.getOverlay().ordinal() - 1) * 5 * 2, 182, 5);
		}

		int k = (int)(bossEvent.getPercent() * 183.0F);
		if (k > 0) {
			this.blit(i, j, 0, bossEvent.getColor().ordinal() * 5 * 2 + 5, k, 5);
			if (bossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
				this.blit(i, j, 0, 80 + (bossEvent.getOverlay().ordinal() - 1) * 5 * 2 + 5, k, 5);
			}
		}
	}

	public void update(ClientboundBossEventPacket clientboundBossEventPacket) {
		if (clientboundBossEventPacket.getOperation() == ClientboundBossEventPacket.Operation.ADD) {
			this.events.put(clientboundBossEventPacket.getId(), new LerpingBossEvent(clientboundBossEventPacket));
		} else if (clientboundBossEventPacket.getOperation() == ClientboundBossEventPacket.Operation.REMOVE) {
			this.events.remove(clientboundBossEventPacket.getId());
		} else {
			((LerpingBossEvent)this.events.get(clientboundBossEventPacket.getId())).update(clientboundBossEventPacket);
		}
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
