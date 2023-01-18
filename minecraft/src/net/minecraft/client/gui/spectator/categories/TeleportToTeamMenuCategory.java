package net.minecraft.client.gui.spectator.categories;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

@Environment(EnvType.CLIENT)
public class TeleportToTeamMenuCategory implements SpectatorMenuCategory, SpectatorMenuItem {
	private static final Component TELEPORT_TEXT = Component.translatable("spectatorMenu.team_teleport");
	private static final Component TELEPORT_PROMPT = Component.translatable("spectatorMenu.team_teleport.prompt");
	private final List<SpectatorMenuItem> items;

	public TeleportToTeamMenuCategory() {
		Minecraft minecraft = Minecraft.getInstance();
		this.items = createTeamEntries(minecraft, minecraft.level.getScoreboard());
	}

	private static List<SpectatorMenuItem> createTeamEntries(Minecraft minecraft, Scoreboard scoreboard) {
		return scoreboard.getPlayerTeams()
			.stream()
			.flatMap(playerTeam -> TeleportToTeamMenuCategory.TeamSelectionItem.create(minecraft, playerTeam).stream())
			.toList();
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
		return !this.items.isEmpty();
	}

	@Environment(EnvType.CLIENT)
	static class TeamSelectionItem implements SpectatorMenuItem {
		private final PlayerTeam team;
		private final ResourceLocation iconSkin;
		private final List<PlayerInfo> players;

		private TeamSelectionItem(PlayerTeam playerTeam, List<PlayerInfo> list, ResourceLocation resourceLocation) {
			this.team = playerTeam;
			this.players = list;
			this.iconSkin = resourceLocation;
		}

		public static Optional<SpectatorMenuItem> create(Minecraft minecraft, PlayerTeam playerTeam) {
			List<PlayerInfo> list = new ArrayList();

			for (String string : playerTeam.getPlayers()) {
				PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(string);
				if (playerInfo != null && playerInfo.getGameMode() != GameType.SPECTATOR) {
					list.add(playerInfo);
				}
			}

			if (list.isEmpty()) {
				return Optional.empty();
			} else {
				GameProfile gameProfile = ((PlayerInfo)list.get(RandomSource.create().nextInt(list.size()))).getProfile();
				ResourceLocation resourceLocation = minecraft.getSkinManager().getInsecureSkinLocation(gameProfile);
				return Optional.of(new TeleportToTeamMenuCategory.TeamSelectionItem(playerTeam, list, resourceLocation));
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

			RenderSystem.setShaderTexture(0, this.iconSkin);
			RenderSystem.setShaderColor(f, f, f, (float)i / 255.0F);
			PlayerFaceRenderer.draw(poseStack, 2, 2, 12);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}

		@Override
		public boolean isEnabled() {
			return true;
		}
	}
}
