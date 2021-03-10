package net.minecraft.client.gui.spectator.categories;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.PlayerTeam;

@Environment(EnvType.CLIENT)
public class TeleportToTeamMenuCategory implements SpectatorMenuCategory, SpectatorMenuItem {
	private static final Component TELEPORT_TEXT = new TranslatableComponent("spectatorMenu.team_teleport");
	private static final Component TELEPORT_PROMPT = new TranslatableComponent("spectatorMenu.team_teleport.prompt");
	private final List<SpectatorMenuItem> items = Lists.<SpectatorMenuItem>newArrayList();

	public TeleportToTeamMenuCategory() {
		Minecraft minecraft = Minecraft.getInstance();

		for (PlayerTeam playerTeam : minecraft.level.getScoreboard().getPlayerTeams()) {
			this.items.add(new TeleportToTeamMenuCategory.TeamSelectionItem(playerTeam));
		}
	}

	@Override
	public List<SpectatorMenuItem> getItems() {
		return this.items;
	}

	@Override
	public Component getPrompt() {
		return TELEPORT_PROMPT;
	}

	@Override
	public void selectItem(SpectatorMenu spectatorMenu) {
		spectatorMenu.selectCategory(this);
	}

	@Override
	public Component getName() {
		return TELEPORT_TEXT;
	}

	@Override
	public void renderIcon(PoseStack poseStack, float f, int i) {
		RenderSystem.setShaderTexture(0, SpectatorGui.SPECTATOR_LOCATION);
		GuiComponent.blit(poseStack, 0, 0, 16.0F, 0.0F, 16, 16, 256, 256);
	}

	@Override
	public boolean isEnabled() {
		for (SpectatorMenuItem spectatorMenuItem : this.items) {
			if (spectatorMenuItem.isEnabled()) {
				return true;
			}
		}

		return false;
	}

	@Environment(EnvType.CLIENT)
	class TeamSelectionItem implements SpectatorMenuItem {
		private final PlayerTeam team;
		private final ResourceLocation location;
		private final List<PlayerInfo> players;

		public TeamSelectionItem(PlayerTeam playerTeam) {
			this.team = playerTeam;
			this.players = Lists.<PlayerInfo>newArrayList();

			for (String string : playerTeam.getPlayers()) {
				PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(string);
				if (playerInfo != null) {
					this.players.add(playerInfo);
				}
			}

			if (this.players.isEmpty()) {
				this.location = DefaultPlayerSkin.getDefaultSkin();
			} else {
				String string2 = ((PlayerInfo)this.players.get(new Random().nextInt(this.players.size()))).getProfile().getName();
				this.location = AbstractClientPlayer.getSkinLocation(string2);
				AbstractClientPlayer.registerSkinTexture(this.location, string2);
			}
		}

		@Override
		public void selectItem(SpectatorMenu spectatorMenu) {
			spectatorMenu.selectCategory(new TeleportToPlayerMenuCategory(this.players));
		}

		@Override
		public Component getName() {
			return this.team.getDisplayName();
		}

		@Override
		public void renderIcon(PoseStack poseStack, float f, int i) {
			Integer integer = this.team.getColor().getColor();
			if (integer != null) {
				float g = (float)(integer >> 16 & 0xFF) / 255.0F;
				float h = (float)(integer >> 8 & 0xFF) / 255.0F;
				float j = (float)(integer & 0xFF) / 255.0F;
				GuiComponent.fill(poseStack, 1, 1, 15, 15, Mth.color(g * f, h * f, j * f) | i << 24);
			}

			RenderSystem.setShaderTexture(0, this.location);
			RenderSystem.setShaderColor(f, f, f, (float)i / 255.0F);
			GuiComponent.blit(poseStack, 2, 2, 12, 12, 8.0F, 8.0F, 8, 8, 64, 64);
			GuiComponent.blit(poseStack, 2, 2, 12, 12, 40.0F, 8.0F, 8, 8, 64, 64);
		}

		@Override
		public boolean isEnabled() {
			return !this.players.isEmpty();
		}
	}
}
