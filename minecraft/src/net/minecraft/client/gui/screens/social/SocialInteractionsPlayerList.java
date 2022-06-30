package net.minecraft.client.gui.screens.social;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.multiplayer.PlayerInfo;

@Environment(EnvType.CLIENT)
public class SocialInteractionsPlayerList extends ContainerObjectSelectionList<PlayerEntry> {
	private final SocialInteractionsScreen socialInteractionsScreen;
	private final List<PlayerEntry> players = Lists.<PlayerEntry>newArrayList();
	@Nullable
	private String filter;

	public SocialInteractionsPlayerList(SocialInteractionsScreen socialInteractionsScreen, Minecraft minecraft, int i, int j, int k, int l, int m) {
		super(minecraft, i, j, k, l, m);
		this.socialInteractionsScreen = socialInteractionsScreen;
		this.setRenderBackground(false);
		this.setRenderTopAndBottom(false);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		double d = this.minecraft.getWindow().getGuiScale();
		RenderSystem.enableScissor(
			(int)((double)this.getRowLeft() * d),
			(int)((double)(this.height - this.y1) * d),
			(int)((double)(this.getScrollbarPosition() + 6) * d),
			(int)((double)(this.height - (this.height - this.y1) - this.y0 - 4) * d)
		);
		super.render(poseStack, i, j, f);
		RenderSystem.disableScissor();
	}

	public void updatePlayerList(Collection<UUID> collection, double d) {
		this.addOnlinePlayers(collection);
		this.updateFiltersAndScroll(d);
	}

	public void updatePlayerListWithLog(Collection<UUID> collection, double d) {
		this.addOnlinePlayers(collection);
		this.addPlayersFromLog(collection);
		this.updateFiltersAndScroll(d);
	}

	private void addOnlinePlayers(Collection<UUID> collection) {
		this.players.clear();

		for (UUID uUID : collection) {
			PlayerInfo playerInfo = this.minecraft.player.connection.getPlayerInfo(uUID);
			if (playerInfo != null) {
				this.players
					.add(
						new PlayerEntry(
							this.minecraft, this.socialInteractionsScreen, playerInfo.getProfile().getId(), playerInfo.getProfile().getName(), playerInfo::getSkinLocation
						)
					);
			}
		}

		this.players.sort((playerEntry, playerEntry2) -> playerEntry.getPlayerName().compareToIgnoreCase(playerEntry2.getPlayerName()));
	}

	private void addPlayersFromLog(Collection<UUID> collection) {
		for (GameProfile gameProfile : this.minecraft.getReportingContext().chatLog().selectAllDescending().distinctGameProfiles()) {
			if (!collection.contains(gameProfile.getId())) {
				PlayerEntry playerEntry = new PlayerEntry(
					this.minecraft,
					this.socialInteractionsScreen,
					gameProfile.getId(),
					gameProfile.getName(),
					Suppliers.memoize(() -> this.minecraft.getSkinManager().getInsecureSkinLocation(gameProfile))
				);
				playerEntry.setRemoved(true);
				this.players.add(playerEntry);
			}
		}
	}

	private void updateFiltersAndScroll(double d) {
		this.updateFilteredPlayers();
		this.replaceEntries(this.players);
		this.setScrollAmount(d);
	}

	private void updateFilteredPlayers() {
		if (this.filter != null) {
			this.players.removeIf(playerEntry -> !playerEntry.getPlayerName().toLowerCase(Locale.ROOT).contains(this.filter));
			this.replaceEntries(this.players);
		}
	}

	public void setFilter(String string) {
		this.filter = string;
	}

	public boolean isEmpty() {
		return this.players.isEmpty();
	}

	public void addPlayer(PlayerInfo playerInfo, SocialInteractionsScreen.Page page) {
		UUID uUID = playerInfo.getProfile().getId();

		for (PlayerEntry playerEntry : this.players) {
			if (playerEntry.getPlayerId().equals(uUID)) {
				playerEntry.setRemoved(false);
				return;
			}
		}

		if ((page == SocialInteractionsScreen.Page.ALL || this.minecraft.getPlayerSocialManager().shouldHideMessageFrom(uUID))
			&& (Strings.isNullOrEmpty(this.filter) || playerInfo.getProfile().getName().toLowerCase(Locale.ROOT).contains(this.filter))) {
			PlayerEntry playerEntry2 = new PlayerEntry(
				this.minecraft, this.socialInteractionsScreen, playerInfo.getProfile().getId(), playerInfo.getProfile().getName(), playerInfo::getSkinLocation
			);
			this.addEntry(playerEntry2);
			this.players.add(playerEntry2);
		}
	}

	public void removePlayer(UUID uUID) {
		for (PlayerEntry playerEntry : this.players) {
			if (playerEntry.getPlayerId().equals(uUID)) {
				playerEntry.setRemoved(true);
				return;
			}
		}
	}
}
