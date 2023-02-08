package net.minecraft.client.gui.components;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
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
public class PlayerTabOverlay extends GuiComponent {
	private static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.comparingInt(playerInfo -> playerInfo.getGameMode() == GameType.SPECTATOR ? 1 : 0)
		.thenComparing(playerInfo -> Optionull.mapOrDefault(playerInfo.getTeam(), PlayerTeam::getName, ""))
		.thenComparing(playerInfo -> playerInfo.getProfile().getName(), String::compareToIgnoreCase);
	public static final int MAX_ROWS_PER_COL = 20;
	public static final int HEART_EMPTY_CONTAINER = 16;
	public static final int HEART_EMPTY_CONTAINER_BLINKING = 25;
	public static final int HEART_FULL = 52;
	public static final int HEART_HALF_FULL = 61;
	public static final int HEART_GOLDEN_FULL = 160;
	public static final int HEART_GOLDEN_HALF_FULL = 169;
	public static final int HEART_GHOST_FULL = 70;
	public static final int HEART_GHOST_HALF_FULL = 79;
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

	public void render(PoseStack poseStack, int i, Scoreboard scoreboard, @Nullable Objective objective) {
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
			fill(poseStack, i / 2 - s / 2 - 1, r - 1, i / 2 + s / 2 + 1, r + list2.size() * 9, Integer.MIN_VALUE);

			for (FormattedCharSequence formattedCharSequence2 : list2) {
				int t = this.minecraft.font.width(formattedCharSequence2);
				this.minecraft.font.drawShadow(poseStack, formattedCharSequence2, (float)(i / 2 - t / 2), (float)r, -1);
				r += 9;
			}

			r++;
		}

		fill(poseStack, i / 2 - s / 2 - 1, r - 1, i / 2 + s / 2 + 1, r + n * 9, Integer.MIN_VALUE);
		int u = this.minecraft.options.getBackgroundColor(553648127);

		for (int v = 0; v < m; v++) {
			int t = v / n;
			int w = v % n;
			int x = q + t * p + t * 5;
			int y = r + w * 9;
			fill(poseStack, x, y, x + p, y + 8, u);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			if (v < list.size()) {
				PlayerInfo playerInfo2 = (PlayerInfo)list.get(v);
				GameProfile gameProfile = playerInfo2.getProfile();
				if (bl) {
					Player player = this.minecraft.level.getPlayerByUUID(gameProfile.getId());
					boolean bl2 = player != null && LivingEntityRenderer.isEntityUpsideDown(player);
					boolean bl3 = player != null && player.isModelPartShown(PlayerModelPart.HAT);
					RenderSystem.setShaderTexture(0, playerInfo2.getSkinLocation());
					PlayerFaceRenderer.draw(poseStack, x, y, 8, bl3, bl2);
					x += 9;
				}

				this.minecraft
					.font
					.drawShadow(poseStack, this.getNameForDisplay(playerInfo2), (float)x, (float)y, playerInfo2.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1);
				if (objective != null && playerInfo2.getGameMode() != GameType.SPECTATOR) {
					int z = x + j + 1;
					int aa = z + o;
					if (aa - z > 5) {
						this.renderTablistScore(objective, y, gameProfile.getName(), z, aa, gameProfile.getId(), poseStack);
					}
				}

				this.renderPingIcon(poseStack, p, x - (bl ? 9 : 0), y, playerInfo2);
			}
		}

		if (list3 != null) {
			r += n * 9 + 1;
			fill(poseStack, i / 2 - s / 2 - 1, r - 1, i / 2 + s / 2 + 1, r + list3.size() * 9, Integer.MIN_VALUE);

			for (FormattedCharSequence formattedCharSequence3 : list3) {
				int w = this.minecraft.font.width(formattedCharSequence3);
				this.minecraft.font.drawShadow(poseStack, formattedCharSequence3, (float)(i / 2 - w / 2), (float)r, -1);
				r += 9;
			}
		}
	}

	protected void renderPingIcon(PoseStack poseStack, int i, int j, int k, PlayerInfo playerInfo) {
		RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
		int l = 0;
		int m;
		if (playerInfo.getLatency() < 0) {
			m = 5;
		} else if (playerInfo.getLatency() < 150) {
			m = 0;
		} else if (playerInfo.getLatency() < 300) {
			m = 1;
		} else if (playerInfo.getLatency() < 600) {
			m = 2;
		} else if (playerInfo.getLatency() < 1000) {
			m = 3;
		} else {
			m = 4;
		}

		this.setBlitOffset(this.getBlitOffset() + 100);
		this.blit(poseStack, j + i - 11, k, 0, 176 + m * 8, 10, 8);
		this.setBlitOffset(this.getBlitOffset() - 100);
	}

	private void renderTablistScore(Objective objective, int i, String string, int j, int k, UUID uUID, PoseStack poseStack) {
		int l = objective.getScoreboard().getOrCreatePlayerScore(string, objective).getScore();
		if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
			this.renderTablistHearts(i, j, k, uUID, poseStack, l);
		} else {
			String string2 = "" + ChatFormatting.YELLOW + l;
			this.minecraft.font.drawShadow(poseStack, string2, (float)(k - this.minecraft.font.width(string2)), (float)i, 16777215);
		}
	}

	private void renderTablistHearts(int i, int j, int k, UUID uUID, PoseStack poseStack, int l) {
		PlayerTabOverlay.HealthState healthState = (PlayerTabOverlay.HealthState)this.healthStates
			.computeIfAbsent(uUID, uUIDx -> new PlayerTabOverlay.HealthState(l));
		healthState.update(l, (long)this.gui.getGuiTicks());
		int m = Mth.positiveCeilDiv(Math.max(l, healthState.displayedValue()), 2);
		int n = Math.max(l, Math.max(healthState.displayedValue(), 20)) / 2;
		boolean bl = healthState.isBlinking((long)this.gui.getGuiTicks());
		if (m > 0) {
			RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
			int o = Mth.floor(Math.min((float)(k - j - 4) / (float)n, 9.0F));
			if (o <= 3) {
				float f = Mth.clamp((float)l / 20.0F, 0.0F, 1.0F);
				int p = (int)((1.0F - f) * 255.0F) << 16 | (int)(f * 255.0F) << 8;
				String string = (float)l / 2.0F + "";
				if (k - this.minecraft.font.width(string + "hp") >= j) {
					string = string + "hp";
				}

				this.minecraft.font.drawShadow(poseStack, string, (float)((k + j - this.minecraft.font.width(string)) / 2), (float)i, p);
			} else {
				for (int q = m; q < n; q++) {
					this.blit(poseStack, j + q * o, i, bl ? 25 : 16, 0, 9, 9);
				}

				for (int q = 0; q < m; q++) {
					this.blit(poseStack, j + q * o, i, bl ? 25 : 16, 0, 9, 9);
					if (bl) {
						if (q * 2 + 1 < healthState.displayedValue()) {
							this.blit(poseStack, j + q * o, i, 70, 0, 9, 9);
						}

						if (q * 2 + 1 == healthState.displayedValue()) {
							this.blit(poseStack, j + q * o, i, 79, 0, 9, 9);
						}
					}

					if (q * 2 + 1 < l) {
						this.blit(poseStack, j + q * o, i, q >= 10 ? 160 : 52, 0, 9, 9);
					}

					if (q * 2 + 1 == l) {
						this.blit(poseStack, j + q * o, i, q >= 10 ? 169 : 61, 0, 9, 9);
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
