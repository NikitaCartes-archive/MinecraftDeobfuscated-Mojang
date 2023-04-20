package net.minecraft.client.gui.screens.social;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;

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
	protected void enableScissor(GuiGraphics guiGraphics) {
		guiGraphics.enableScissor(this.x0, this.y0 + 4, this.x1, this.y1);
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
				boolean bl = playerInfo.hasVerifiableChat();
				map.put(uUID, new PlayerEntry(this.minecraft, this.socialInteractionsScreen, uUID, playerInfo.getProfile().getName(), playerInfo::getSkinLocation, bl));
			}
		}
	}

	private void updatePlayersFromChatLog(Map<UUID, PlayerEntry> map, boolean bl) {
		for (GameProfile gameProfile : collectProfilesFromChatLog(this.minecraft.getReportingContext().chatLog())) {
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

	private static Collection<GameProfile> collectProfilesFromChatLog(ChatLog chatLog) {
		Set<GameProfile> set = new ObjectLinkedOpenHashSet<>();

		for (int i = chatLog.end(); i >= chatLog.start(); i--) {
			LoggedChatEvent loggedChatEvent = chatLog.lookup(i);
			if (loggedChatEvent instanceof LoggedChatMessage.Player) {
				LoggedChatMessage.Player player = (LoggedChatMessage.Player)loggedChatEvent;
				if (player.message().hasSignature()) {
					set.add(player.profile());
				}
			}
		}

		return set;
	}

	private void sortPlayerEntries() {
		this.players.sort(Comparator.comparing(playerEntry -> {
			if (playerEntry.getPlayerId().equals(this.minecraft.getUser().getProfileId())) {
				return 0;
			} else if (playerEntry.getPlayerId().version() == 2) {
				return 4;
			} else if (this.minecraft.getReportingContext().hasDraftReportFor(playerEntry.getPlayerId())) {
				return 1;
			} else {
				return playerEntry.hasRecentMessages() ? 2 : 3;
			}
		}).thenComparing(playerEntry -> {
			if (!playerEntry.getPlayerName().isBlank()) {
				int i = playerEntry.getPlayerName().codePointAt(0);
				if (i == 95 || i >= 97 && i <= 122 || i >= 65 && i <= 90 || i >= 48 && i <= 57) {
					return 0;
				}
			}

			return 1;
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
			boolean bl = playerInfo.hasVerifiableChat();
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
