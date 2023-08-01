package net.minecraft.client.gui.components;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

@Environment(EnvType.CLIENT)
public class PlayerTabOverlay {
	private static final ResourceLocation PING_UNKNOWN_SPRITE = new ResourceLocation("icon/ping_unknown");
	private static final ResourceLocation PING_1_SPRITE = new ResourceLocation("icon/ping_1");
	private static final ResourceLocation PING_2_SPRITE = new ResourceLocation("icon/ping_2");
	private static final ResourceLocation PING_3_SPRITE = new ResourceLocation("icon/ping_3");
	private static final ResourceLocation PING_4_SPRITE = new ResourceLocation("icon/ping_4");
	private static final ResourceLocation PING_5_SPRITE = new ResourceLocation("icon/ping_5");
	private static final ResourceLocation HEART_CONTAINER_BLINKING_SPRITE = new ResourceLocation("hud/heart/container_blinking");
	private static final ResourceLocation HEART_CONTAINER_SPRITE = new ResourceLocation("hud/heart/container");
	private static final ResourceLocation HEART_FULL_BLINKING_SPRITE = new ResourceLocation("hud/heart/full_blinking");
	private static final ResourceLocation HEART_HALF_BLINKING_SPRITE = new ResourceLocation("hud/heart/half_blinking");
	private static final ResourceLocation HEART_ABSORBING_FULL_BLINKING_SPRITE = new ResourceLocation("hud/heart/absorbing_full_blinking");
	private static final ResourceLocation HEART_FULL_SPRITE = new ResourceLocation("hud/heart/full");
	private static final ResourceLocation HEART_ABSORBING_HALF_BLINKING_SPRITE = new ResourceLocation("hud/heart/absorbing_half_blinking");
	private static final ResourceLocation HEART_HALF_SPRITE = new ResourceLocation("hud/heart/half");
	private static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.comparingInt(playerInfo -> playerInfo.getGameMode() == GameType.SPECTATOR ? 1 : 0)
		.thenComparing(playerInfo -> Optionull.mapOrDefault(playerInfo.getTeam(), PlayerTeam::getName, ""))
		.thenComparing(playerInfo -> playerInfo.getProfile().getName(), String::compareToIgnoreCase);
	public static final int MAX_ROWS_PER_COL = 20;
	private final Minecraft minecraft;
	private final Gui gui;
	@Nullable
	private Component footer;
	@Nullable
	private Component header;
	private boolean visible;
	private final Map<UUID, PlayerTabOverlay.HealthState> healthStates = new Object2ObjectOpenHashMap<>();

	public PlayerTabOverlay(Minecraft minecraft, Gui gui) {
		this.minecraft = minecraft;
		this.gui = gui;
	}

	public Component getNameForDisplay(PlayerInfo playerInfo) {
		return playerInfo.getTabListDisplayName() != null
			? this.decorateName(playerInfo, playerInfo.getTabListDisplayName().copy())
			: this.decorateName(playerInfo, PlayerTeam.formatNameForTeam(playerInfo.getTeam(), Component.literal(playerInfo.getProfile().getName())));
	}

	private Component decorateName(PlayerInfo playerInfo, MutableComponent mutableComponent) {
		return playerInfo.getGameMode() == GameType.SPECTATOR ? mutableComponent.withStyle(ChatFormatting.ITALIC) : mutableComponent;
	}

	public void setVisible(boolean bl) {
		if (this.visible != bl) {
			this.healthStates.clear();
			this.visible = bl;
			if (bl) {
				Component component = ComponentUtils.formatList(this.getPlayerInfos(), Component.literal(", "), this::getNameForDisplay);
				this.minecraft.getNarrator().sayNow(Component.translatable("multiplayer.player.list.narration", component));
			}
		}
	}

	private List<PlayerInfo> getPlayerInfos() {
		return this.minecraft.player.connection.getListedOnlinePlayers().stream().sorted(PLAYER_COMPARATOR).limit(80L).toList();
	}

	public void render(GuiGraphics guiGraphics, int i, Scoreboard scoreboard, @Nullable Objective objective) {
		List<PlayerInfo> list = this.getPlayerInfos();
		int j = 0;
		int k = 0;

		for (PlayerInfo playerInfo : list) {
			int l = this.minecraft.font.width(this.getNameForDisplay(playerInfo));
			j = Math.max(j, l);
			if (objective != null && objective.getRenderType() != ObjectiveCriteria.RenderType.HEARTS) {
				l = this.minecraft.font.width(" " + scoreboard.getOrCreatePlayerScore(playerInfo.getProfile().getName(), objective).getScore());
				k = Math.max(k, l);
			}
		}

		if (!this.healthStates.isEmpty()) {
			Set<UUID> set = (Set<UUID>)list.stream().map(playerInfox -> playerInfox.getProfile().getId()).collect(Collectors.toSet());
			this.healthStates.keySet().removeIf(uUID -> !set.contains(uUID));
		}

		int m = list.size();
		int n = m;

		int l;
		for (l = 1; n > 20; n = (m + l - 1) / l) {
			l++;
		}

		boolean bl = this.minecraft.isLocalServer() || this.minecraft.getConnection().getConnection().isEncrypted();
		int o;
		if (objective != null) {
			if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
				o = 90;
			} else {
				o = k;
			}
		} else {
			o = 0;
		}

		int p = Math.min(l * ((bl ? 9 : 0) + j + o + 13), i - 50) / l;
		int q = i / 2 - (p * l + (l - 1) * 5) / 2;
		int r = 10;
		int s = p * l + (l - 1) * 5;
		List<FormattedCharSequence> list2 = null;
		if (this.header != null) {
			list2 = this.minecraft.font.split(this.header, i - 50);

			for (FormattedCharSequence formattedCharSequence : list2) {
				s = Math.max(s, this.minecraft.font.width(formattedCharSequence));
			}
		}

		List<FormattedCharSequence> list3 = null;
		if (this.footer != null) {
			list3 = this.minecraft.font.split(this.footer, i - 50);

			for (FormattedCharSequence formattedCharSequence2 : list3) {
				s = Math.max(s, this.minecraft.font.width(formattedCharSequence2));
			}
		}

		if (list2 != null) {
			guiGraphics.fill(i / 2 - s / 2 - 1, r - 1, i / 2 + s / 2 + 1, r + list2.size() * 9, Integer.MIN_VALUE);

			for (FormattedCharSequence formattedCharSequence2 : list2) {
				int t = this.minecraft.font.width(formattedCharSequence2);
				guiGraphics.drawString(this.minecraft.font, formattedCharSequence2, i / 2 - t / 2, r, -1);
				r += 9;
			}

			r++;
		}

		guiGraphics.fill(i / 2 - s / 2 - 1, r - 1, i / 2 + s / 2 + 1, r + n * 9, Integer.MIN_VALUE);
		int u = this.minecraft.options.getBackgroundColor(553648127);

		for (int v = 0; v < m; v++) {
			int t = v / n;
			int w = v % n;
			int x = q + t * p + t * 5;
			int y = r + w * 9;
			guiGraphics.fill(x, y, x + p, y + 8, u);
			RenderSystem.enableBlend();
			if (v < list.size()) {
				PlayerInfo playerInfo2 = (PlayerInfo)list.get(v);
				GameProfile gameProfile = playerInfo2.getProfile();
				if (bl) {
					Player player = this.minecraft.level.getPlayerByUUID(gameProfile.getId());
					boolean bl2 = player != null && LivingEntityRenderer.isEntityUpsideDown(player);
					boolean bl3 = player != null && player.isModelPartShown(PlayerModelPart.HAT);
					PlayerFaceRenderer.draw(guiGraphics, playerInfo2.getSkin().texture(), x, y, 8, bl3, bl2);
					x += 9;
				}

				guiGraphics.drawString(this.minecraft.font, this.getNameForDisplay(playerInfo2), x, y, playerInfo2.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1);
				if (objective != null && playerInfo2.getGameMode() != GameType.SPECTATOR) {
					int z = x + j + 1;
					int aa = z + o;
					if (aa - z > 5) {
						this.renderTablistScore(objective, y, gameProfile.getName(), z, aa, gameProfile.getId(), guiGraphics);
					}
				}

				this.renderPingIcon(guiGraphics, p, x - (bl ? 9 : 0), y, playerInfo2);
			}
		}

		if (list3 != null) {
			r += n * 9 + 1;
			guiGraphics.fill(i / 2 - s / 2 - 1, r - 1, i / 2 + s / 2 + 1, r + list3.size() * 9, Integer.MIN_VALUE);

			for (FormattedCharSequence formattedCharSequence3 : list3) {
				int w = this.minecraft.font.width(formattedCharSequence3);
				guiGraphics.drawString(this.minecraft.font, formattedCharSequence3, i / 2 - w / 2, r, -1);
				r += 9;
			}
		}
	}

	protected void renderPingIcon(GuiGraphics guiGraphics, int i, int j, int k, PlayerInfo playerInfo) {
		ResourceLocation resourceLocation;
		if (playerInfo.getLatency() < 0) {
			resourceLocation = PING_UNKNOWN_SPRITE;
		} else if (playerInfo.getLatency() < 150) {
			resourceLocation = PING_5_SPRITE;
		} else if (playerInfo.getLatency() < 300) {
			resourceLocation = PING_4_SPRITE;
		} else if (playerInfo.getLatency() < 600) {
			resourceLocation = PING_3_SPRITE;
		} else if (playerInfo.getLatency() < 1000) {
			resourceLocation = PING_2_SPRITE;
		} else {
			resourceLocation = PING_1_SPRITE;
		}

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
		guiGraphics.blitSprite(resourceLocation, j + i - 11, k, 10, 8);
		guiGraphics.pose().popPose();
	}

	private void renderTablistScore(Objective objective, int i, String string, int j, int k, UUID uUID, GuiGraphics guiGraphics) {
		int l = objective.getScoreboard().getOrCreatePlayerScore(string, objective).getScore();
		if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
			this.renderTablistHearts(i, j, k, uUID, guiGraphics, l);
		} else {
			String string2 = "" + ChatFormatting.YELLOW + l;
			guiGraphics.drawString(this.minecraft.font, string2, k - this.minecraft.font.width(string2), i, 16777215);
		}
	}

	private void renderTablistHearts(int i, int j, int k, UUID uUID, GuiGraphics guiGraphics, int l) {
		PlayerTabOverlay.HealthState healthState = (PlayerTabOverlay.HealthState)this.healthStates
			.computeIfAbsent(uUID, uUIDx -> new PlayerTabOverlay.HealthState(l));
		healthState.update(l, (long)this.gui.getGuiTicks());
		int m = Mth.positiveCeilDiv(Math.max(l, healthState.displayedValue()), 2);
		int n = Math.max(l, Math.max(healthState.displayedValue(), 20)) / 2;
		boolean bl = healthState.isBlinking((long)this.gui.getGuiTicks());
		if (m > 0) {
			int o = Mth.floor(Math.min((float)(k - j - 4) / (float)n, 9.0F));
			if (o <= 3) {
				float f = Mth.clamp((float)l / 20.0F, 0.0F, 1.0F);
				int p = (int)((1.0F - f) * 255.0F) << 16 | (int)(f * 255.0F) << 8;
				String string = (float)l / 2.0F + "";
				if (k - this.minecraft.font.width(string + "hp") >= j) {
					string = string + "hp";
				}

				guiGraphics.drawString(this.minecraft.font, string, (k + j - this.minecraft.font.width(string)) / 2, i, p);
			} else {
				ResourceLocation resourceLocation = bl ? HEART_CONTAINER_BLINKING_SPRITE : HEART_CONTAINER_SPRITE;

				for (int p = m; p < n; p++) {
					guiGraphics.blitSprite(resourceLocation, j + p * o, i, 9, 9);
				}

				for (int p = 0; p < m; p++) {
					guiGraphics.blitSprite(resourceLocation, j + p * o, i, 9, 9);
					if (bl) {
						if (p * 2 + 1 < healthState.displayedValue()) {
							guiGraphics.blitSprite(HEART_FULL_BLINKING_SPRITE, j + p * o, i, 9, 9);
						}

						if (p * 2 + 1 == healthState.displayedValue()) {
							guiGraphics.blitSprite(HEART_HALF_BLINKING_SPRITE, j + p * o, i, 9, 9);
						}
					}

					if (p * 2 + 1 < l) {
						guiGraphics.blitSprite(p >= 10 ? HEART_ABSORBING_FULL_BLINKING_SPRITE : HEART_FULL_SPRITE, j + p * o, i, 9, 9);
					}

					if (p * 2 + 1 == l) {
						guiGraphics.blitSprite(p >= 10 ? HEART_ABSORBING_HALF_BLINKING_SPRITE : HEART_HALF_SPRITE, j + p * o, i, 9, 9);
					}
				}
			}
		}
	}

	public void setFooter(@Nullable Component component) {
		this.footer = component;
	}

	public void setHeader(@Nullable Component component) {
		this.header = component;
	}

	public void reset() {
		this.header = null;
		this.footer = null;
	}

	@Environment(EnvType.CLIENT)
	static class HealthState {
		private static final long DISPLAY_UPDATE_DELAY = 20L;
		private static final long DECREASE_BLINK_DURATION = 20L;
		private static final long INCREASE_BLINK_DURATION = 10L;
		private int lastValue;
		private int displayedValue;
		private long lastUpdateTick;
		private long blinkUntilTick;

		public HealthState(int i) {
			this.displayedValue = i;
			this.lastValue = i;
		}

		public void update(int i, long l) {
			if (i != this.lastValue) {
				long m = i < this.lastValue ? 20L : 10L;
				this.blinkUntilTick = l + m;
				this.lastValue = i;
				this.lastUpdateTick = l;
			}

			if (l - this.lastUpdateTick > 20L) {
				this.displayedValue = i;
			}
		}

		public int displayedValue() {
			return this.displayedValue;
		}

		public boolean isBlinking(long l) {
			return this.blinkUntilTick > l && (this.blinkUntilTick - l) % 6L >= 3L;
		}
	}
}
