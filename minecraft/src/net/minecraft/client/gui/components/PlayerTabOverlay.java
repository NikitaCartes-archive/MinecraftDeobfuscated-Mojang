package net.minecraft.client.gui.components;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
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
	private static final Ordering<PlayerInfo> PLAYER_ORDERING = Ordering.from(new PlayerTabOverlay.PlayerInfoComparator());
	private final Minecraft minecraft;
	private final Gui gui;
	private Component footer;
	private Component header;
	private long visibilityId;
	private boolean visible;

	public PlayerTabOverlay(Minecraft minecraft, Gui gui) {
		this.minecraft = minecraft;
		this.gui = gui;
	}

	public Component getNameForDisplay(PlayerInfo playerInfo) {
		return playerInfo.getTabListDisplayName() != null
			? this.decorateName(playerInfo, playerInfo.getTabListDisplayName().copy())
			: this.decorateName(playerInfo, PlayerTeam.formatNameForTeam(playerInfo.getTeam(), new TextComponent(playerInfo.getProfile().getName())));
	}

	private Component decorateName(PlayerInfo playerInfo, MutableComponent mutableComponent) {
		return playerInfo.getGameMode() == GameType.SPECTATOR ? mutableComponent.withStyle(ChatFormatting.ITALIC) : mutableComponent;
	}

	public void setVisible(boolean bl) {
		if (bl && !this.visible) {
			this.visibilityId = Util.getMillis();
		}

		this.visible = bl;
	}

	public void render(PoseStack poseStack, int i, Scoreboard scoreboard, @Nullable Objective objective) {
		ClientPacketListener clientPacketListener = this.minecraft.player.connection;
		List<PlayerInfo> list = PLAYER_ORDERING.sortedCopy(clientPacketListener.getOnlinePlayers());
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

		list = list.subList(0, Math.min(list.size(), 80));
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
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableAlphaTest();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			if (v < list.size()) {
				PlayerInfo playerInfo2 = (PlayerInfo)list.get(v);
				GameProfile gameProfile = playerInfo2.getProfile();
				if (bl) {
					Player player = this.minecraft.level.getPlayerByUUID(gameProfile.getId());
					boolean bl2 = player != null
						&& player.isModelPartShown(PlayerModelPart.CAPE)
						&& ("Dinnerbone".equals(gameProfile.getName()) || "Grumm".equals(gameProfile.getName()));
					this.minecraft.getTextureManager().bind(playerInfo2.getSkinLocation());
					int z = 8 + (bl2 ? 8 : 0);
					int aa = 8 * (bl2 ? -1 : 1);
					GuiComponent.blit(poseStack, x, y, 8, 8, 8.0F, (float)z, 8, aa, 64, 64);
					if (player != null && player.isModelPartShown(PlayerModelPart.HAT)) {
						int ab = 8 + (bl2 ? 8 : 0);
						int ac = 8 * (bl2 ? -1 : 1);
						GuiComponent.blit(poseStack, x, y, 8, 8, 40.0F, (float)ab, 8, ac, 64, 64);
					}

					x += 9;
				}

				this.minecraft
					.font
					.drawShadow(poseStack, this.getNameForDisplay(playerInfo2), (float)x, (float)y, playerInfo2.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1);
				if (objective != null && playerInfo2.getGameMode() != GameType.SPECTATOR) {
					int ad = x + j + 1;
					int ae = ad + o;
					if (ae - ad > 5) {
						this.renderTablistScore(objective, y, gameProfile.getName(), ad, ae, playerInfo2, poseStack);
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
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
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

	private void renderTablistScore(Objective objective, int i, String string, int j, int k, PlayerInfo playerInfo, PoseStack poseStack) {
		int l = objective.getScoreboard().getOrCreatePlayerScore(string, objective).getScore();
		if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
			this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
			long m = Util.getMillis();
			if (this.visibilityId == playerInfo.getRenderVisibilityId()) {
				if (l < playerInfo.getLastHealth()) {
					playerInfo.setLastHealthTime(m);
					playerInfo.setHealthBlinkTime((long)(this.gui.getGuiTicks() + 20));
				} else if (l > playerInfo.getLastHealth()) {
					playerInfo.setLastHealthTime(m);
					playerInfo.setHealthBlinkTime((long)(this.gui.getGuiTicks() + 10));
				}
			}

			if (m - playerInfo.getLastHealthTime() > 1000L || this.visibilityId != playerInfo.getRenderVisibilityId()) {
				playerInfo.setLastHealth(l);
				playerInfo.setDisplayHealth(l);
				playerInfo.setLastHealthTime(m);
			}

			playerInfo.setRenderVisibilityId(this.visibilityId);
			playerInfo.setLastHealth(l);
			int n = Mth.ceil((float)Math.max(l, playerInfo.getDisplayHealth()) / 2.0F);
			int o = Math.max(Mth.ceil((float)(l / 2)), Math.max(Mth.ceil((float)(playerInfo.getDisplayHealth() / 2)), 10));
			boolean bl = playerInfo.getHealthBlinkTime() > (long)this.gui.getGuiTicks()
				&& (playerInfo.getHealthBlinkTime() - (long)this.gui.getGuiTicks()) / 3L % 2L == 1L;
			if (n > 0) {
				int p = Mth.floor(Math.min((float)(k - j - 4) / (float)o, 9.0F));
				if (p > 3) {
					for (int q = n; q < o; q++) {
						this.blit(poseStack, j + q * p, i, bl ? 25 : 16, 0, 9, 9);
					}

					for (int q = 0; q < n; q++) {
						this.blit(poseStack, j + q * p, i, bl ? 25 : 16, 0, 9, 9);
						if (bl) {
							if (q * 2 + 1 < playerInfo.getDisplayHealth()) {
								this.blit(poseStack, j + q * p, i, 70, 0, 9, 9);
							}

							if (q * 2 + 1 == playerInfo.getDisplayHealth()) {
								this.blit(poseStack, j + q * p, i, 79, 0, 9, 9);
							}
						}

						if (q * 2 + 1 < l) {
							this.blit(poseStack, j + q * p, i, q >= 10 ? 160 : 52, 0, 9, 9);
						}

						if (q * 2 + 1 == l) {
							this.blit(poseStack, j + q * p, i, q >= 10 ? 169 : 61, 0, 9, 9);
						}
					}
				} else {
					float f = Mth.clamp((float)l / 20.0F, 0.0F, 1.0F);
					int r = (int)((1.0F - f) * 255.0F) << 16 | (int)(f * 255.0F) << 8;
					String string2 = "" + (float)l / 2.0F;
					if (k - this.minecraft.font.width(string2 + "hp") >= j) {
						string2 = string2 + "hp";
					}

					this.minecraft.font.drawShadow(poseStack, string2, (float)((k + j) / 2 - this.minecraft.font.width(string2) / 2), (float)i, r);
				}
			}
		} else {
			String string3 = ChatFormatting.YELLOW + "" + l;
			this.minecraft.font.drawShadow(poseStack, string3, (float)(k - this.minecraft.font.width(string3)), (float)i, 16777215);
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
	static class PlayerInfoComparator implements Comparator<PlayerInfo> {
		private PlayerInfoComparator() {
		}

		public int compare(PlayerInfo playerInfo, PlayerInfo playerInfo2) {
			PlayerTeam playerTeam = playerInfo.getTeam();
			PlayerTeam playerTeam2 = playerInfo2.getTeam();
			return ComparisonChain.start()
				.compareTrueFirst(playerInfo.getGameMode() != GameType.SPECTATOR, playerInfo2.getGameMode() != GameType.SPECTATOR)
				.compare(playerTeam != null ? playerTeam.getName() : "", playerTeam2 != null ? playerTeam2.getName() : "")
				.compare(playerInfo.getProfile().getName(), playerInfo2.getProfile().getName(), String::compareToIgnoreCase)
				.result();
		}
	}
}
