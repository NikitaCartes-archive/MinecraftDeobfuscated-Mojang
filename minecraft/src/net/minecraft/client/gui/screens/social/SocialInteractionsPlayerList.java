package net.minecraft.client.gui.screens.social;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.multiplayer.ClientPacketListener;
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

	public void updatePlayerList(Collection<UUID> collection, double d, boolean bl) {
		Map<UUID, PlayerEntry> map = new HashMap();
		this.addOnlinePlayers(collection, map);
		this.updatePlayersFromChatLog(map, bl);
		this.updateFiltersAndScroll(map.values(), d);
	}

	private void addOnlinePlayers(Collection<UUID> collection, Map<UUID, PlayerEntry> map) {
		ClientPacketListener clientPacketListener = this.minecraft.player.connection;

		for (UUID uUID : collection) {
			PlayerInfo playerInfo = clientPacketListener.getPlayerInfo(uUID);
			if (playerInfo != null) {
				UUID uUID2 = playerInfo.getProfile().getId();
				boolean bl = playerInfo.getProfilePublicKey() != null;
				map.put(uUID2, new PlayerEntry(this.minecraft, this.socialInteractionsScreen, uUID2, playerInfo.getProfile().getName(), playerInfo::getSkinLocation, bl));
			}
		}
	}

	private void updatePlayersFromChatLog(Map<UUID, PlayerEntry> map, boolean bl) {
		for (GameProfile gameProfile : this.minecraft.getReportingContext().chatLog().selectAllDescending().reportableGameProfiles()) {
			PlayerEntry playerEntry;
			if (bl) {
				playerEntry = (PlayerEntry)map.computeIfAbsent(
					gameProfile.getId(),
					uUID -> {
						PlayerEntry playerEntryx = new PlayerEntry(
							this.minecraft,
							this.socialInteractionsScreen,
							gameProfile.getId(),
							gameProfile.getName(),
							Suppliers.memoize(() -> this.minecraft.getSkinManager().getInsecureSkinLocation(gameProfile)),
							true
						);
						playerEntryx.setRemoved(true);
						return playerEntryx;
					}
				);
			} else {
				playerEntry = (PlayerEntry)map.get(gameProfile.getId());
				if (playerEntry == null) {
					continue;
				}
			}

			playerEntry.setHasRecentMessages(true);
		}
	}

	private void sortPlayerEntries() {
		this.players.sort(Comparator.comparing(playerEntry -> {
			if (playerEntry.getPlayerId().equals(this.minecraft.getUser().getProfileId())) {
				return 0;
			} else if (playerEntry.getPlayerId().version() == 2) {
				return 3;
			} else {
				return playerEntry.hasRecentMessages() ? 1 : 2;
			}
		}).thenComparing(playerEntry -> {
			int i = playerEntry.getPlayerName().codePointAt(0);
			return i != 95 && (i < 97 || i > 122) && (i < 65 || i > 90) && (i < 48 || i > 57) ? 1 : 0;
		}).thenComparing(PlayerEntry::getPlayerName, String::compareToIgnoreCase));
	}

	private void updateFiltersAndScroll(Collection<PlayerEntry> collection, double d) {
		this.players.clear();
		this.players.addAll(collection);
		this.sortPlayerEntries();
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
			boolean bl = playerInfo.getProfilePublicKey() != null;
			PlayerEntry playerEntryx = new PlayerEntry(
				this.minecraft, this.socialInteractionsScreen, playerInfo.getProfile().getId(), playerInfo.getProfile().getName(), playerInfo::getSkinLocation, bl
			);
			this.addEntry(playerEntryx);
			this.players.add(playerEntryx);
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
