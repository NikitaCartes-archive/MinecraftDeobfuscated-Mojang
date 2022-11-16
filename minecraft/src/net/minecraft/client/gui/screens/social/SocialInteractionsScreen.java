package net.minecraft.client.gui.screens.social;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class SocialInteractionsScreen extends Screen {
	protected static final ResourceLocation SOCIAL_INTERACTIONS_LOCATION = new ResourceLocation("textures/gui/social_interactions.png");
	private static final Component TAB_ALL = Component.translatable("gui.socialInteractions.tab_all");
	private static final Component TAB_HIDDEN = Component.translatable("gui.socialInteractions.tab_hidden");
	private static final Component TAB_BLOCKED = Component.translatable("gui.socialInteractions.tab_blocked");
	private static final Component TAB_ALL_SELECTED = TAB_ALL.plainCopy().withStyle(ChatFormatting.UNDERLINE);
	private static final Component TAB_HIDDEN_SELECTED = TAB_HIDDEN.plainCopy().withStyle(ChatFormatting.UNDERLINE);
	private static final Component TAB_BLOCKED_SELECTED = TAB_BLOCKED.plainCopy().withStyle(ChatFormatting.UNDERLINE);
	private static final Component SEARCH_HINT = Component.translatable("gui.socialInteractions.search_hint")
		.withStyle(ChatFormatting.ITALIC)
		.withStyle(ChatFormatting.GRAY);
	static final Component EMPTY_SEARCH = Component.translatable("gui.socialInteractions.search_empty").withStyle(ChatFormatting.GRAY);
	private static final Component EMPTY_HIDDEN = Component.translatable("gui.socialInteractions.empty_hidden").withStyle(ChatFormatting.GRAY);
	private static final Component EMPTY_BLOCKED = Component.translatable("gui.socialInteractions.empty_blocked").withStyle(ChatFormatting.GRAY);
	private static final Component BLOCKING_HINT = Component.translatable("gui.socialInteractions.blocking_hint");
	private static final String BLOCK_LINK = "https://aka.ms/javablocking";
	private static final int BG_BORDER_SIZE = 8;
	private static final int BG_UNITS = 16;
	private static final int BG_WIDTH = 236;
	private static final int SEARCH_HEIGHT = 16;
	private static final int MARGIN_Y = 64;
	public static final int LIST_START = 88;
	public static final int SEARCH_START = 78;
	private static final int IMAGE_WIDTH = 238;
	private static final int BUTTON_HEIGHT = 20;
	private static final int ITEM_HEIGHT = 36;
	SocialInteractionsPlayerList socialInteractionsPlayerList;
	EditBox searchBox;
	private String lastSearch = "";
	private SocialInteractionsScreen.Page page = SocialInteractionsScreen.Page.ALL;
	private Button allButton;
	private Button hiddenButton;
	private Button blockedButton;
	private Button blockingHintButton;
	@Nullable
	private Component serverLabel;
	private int playerCount;
	private boolean initialized;

	public SocialInteractionsScreen() {
		super(Component.translatable("gui.socialInteractions.title"));
		this.updateServerLabel(Minecraft.getInstance());
	}

	private int windowHeight() {
		return Math.max(52, this.height - 128 - 16);
	}

	private int backgroundUnits() {
		return this.windowHeight() / 16;
	}

	private int listEnd() {
		return 80 + this.backgroundUnits() * 16 - 8;
	}

	private int marginX() {
		return (this.width - 238) / 2;
	}

	@Override
	public Component getNarrationMessage() {
		return (Component)(this.serverLabel != null ? CommonComponents.joinForNarration(super.getNarrationMessage(), this.serverLabel) : super.getNarrationMessage());
	}

	@Override
	public void tick() {
		super.tick();
		this.searchBox.tick();
	}

	@Override
	protected void init() {
		if (this.initialized) {
			this.socialInteractionsPlayerList.updateSize(this.width, this.height, 88, this.listEnd());
		} else {
			this.socialInteractionsPlayerList = new SocialInteractionsPlayerList(this, this.minecraft, this.width, this.height, 88, this.listEnd(), 36);
		}

		int i = this.socialInteractionsPlayerList.getRowWidth() / 3;
		int j = this.socialInteractionsPlayerList.getRowLeft();
		int k = this.socialInteractionsPlayerList.getRowRight();
		int l = this.font.width(BLOCKING_HINT) + 40;
		int m = 64 + 16 * this.backgroundUnits();
		int n = (this.width - l) / 2 + 3;
		this.allButton = this.addRenderableWidget(Button.builder(TAB_ALL, button -> this.showPage(SocialInteractionsScreen.Page.ALL)).bounds(j, 45, i, 20).build());
		this.hiddenButton = this.addRenderableWidget(
			Button.builder(TAB_HIDDEN, button -> this.showPage(SocialInteractionsScreen.Page.HIDDEN)).bounds((j + k - i) / 2 + 1, 45, i, 20).build()
		);
		this.blockedButton = this.addRenderableWidget(
			Button.builder(TAB_BLOCKED, button -> this.showPage(SocialInteractionsScreen.Page.BLOCKED)).bounds(k - i + 1, 45, i, 20).build()
		);
		String string = this.searchBox != null ? this.searchBox.getValue() : "";
		this.searchBox = new EditBox(this.font, this.marginX() + 28, 78, 196, 16, SEARCH_HINT) {
			@Override
			protected MutableComponent createNarrationMessage() {
				return !SocialInteractionsScreen.this.searchBox.getValue().isEmpty() && SocialInteractionsScreen.this.socialInteractionsPlayerList.isEmpty()
					? super.createNarrationMessage().append(", ").append(SocialInteractionsScreen.EMPTY_SEARCH)
					: super.createNarrationMessage();
			}
		};
		this.searchBox.setMaxLength(16);
		this.searchBox.setBordered(false);
		this.searchBox.setVisible(true);
		this.searchBox.setTextColor(16777215);
		this.searchBox.setValue(string);
		this.searchBox.setHint(SEARCH_HINT);
		this.searchBox.setResponder(this::checkSearchStringUpdate);
		this.addWidget(this.searchBox);
		this.addWidget(this.socialInteractionsPlayerList);
		this.blockingHintButton = this.addRenderableWidget(Button.builder(BLOCKING_HINT, button -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
				if (bl) {
					Util.getPlatform().openUri("https://aka.ms/javablocking");
				}

				this.minecraft.setScreen(this);
			}, "https://aka.ms/javablocking", true))).bounds(n, m, l, 20).build());
		this.initialized = true;
		this.showPage(this.page);
	}

	private void showPage(SocialInteractionsScreen.Page page) {
		this.page = page;
		this.allButton.setMessage(TAB_ALL);
		this.hiddenButton.setMessage(TAB_HIDDEN);
		this.blockedButton.setMessage(TAB_BLOCKED);
		boolean bl = false;
		switch (page) {
			case ALL:
				this.allButton.setMessage(TAB_ALL_SELECTED);
				Collection<UUID> collection = this.minecraft.player.connection.getOnlinePlayerIds();
				this.socialInteractionsPlayerList.updatePlayerList(collection, this.socialInteractionsPlayerList.getScrollAmount(), true);
				break;
			case HIDDEN:
				this.hiddenButton.setMessage(TAB_HIDDEN_SELECTED);
				Set<UUID> set = this.minecraft.getPlayerSocialManager().getHiddenPlayers();
				bl = set.isEmpty();
				this.socialInteractionsPlayerList.updatePlayerList(set, this.socialInteractionsPlayerList.getScrollAmount(), false);
				break;
			case BLOCKED:
				this.blockedButton.setMessage(TAB_BLOCKED_SELECTED);
				PlayerSocialManager playerSocialManager = this.minecraft.getPlayerSocialManager();
				Set<UUID> set2 = (Set<UUID>)this.minecraft
					.player
					.connection
					.getOnlinePlayerIds()
					.stream()
					.filter(playerSocialManager::isBlocked)
					.collect(Collectors.toSet());
				bl = set2.isEmpty();
				this.socialInteractionsPlayerList.updatePlayerList(set2, this.socialInteractionsPlayerList.getScrollAmount(), false);
		}

		GameNarrator gameNarrator = this.minecraft.getNarrator();
		if (!this.searchBox.getValue().isEmpty() && this.socialInteractionsPlayerList.isEmpty() && !this.searchBox.isFocused()) {
			gameNarrator.sayNow(EMPTY_SEARCH);
		} else if (bl) {
			if (page == SocialInteractionsScreen.Page.HIDDEN) {
				gameNarrator.sayNow(EMPTY_HIDDEN);
			} else if (page == SocialInteractionsScreen.Page.BLOCKED) {
				gameNarrator.sayNow(EMPTY_BLOCKED);
			}
		}
	}

	@Override
	public void renderBackground(PoseStack poseStack) {
		int i = this.marginX() + 3;
		super.renderBackground(poseStack);
		RenderSystem.setShaderTexture(0, SOCIAL_INTERACTIONS_LOCATION);
		this.blit(poseStack, i, 64, 1, 1, 236, 8);
		int j = this.backgroundUnits();

		for (int k = 0; k < j; k++) {
			this.blit(poseStack, i, 72 + 16 * k, 1, 10, 236, 16);
		}

		this.blit(poseStack, i, 72 + 16 * j, 1, 27, 236, 8);
		this.blit(poseStack, i + 10, 76, 243, 1, 12, 12);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.updateServerLabel(this.minecraft);
		this.renderBackground(poseStack);
		if (this.serverLabel != null) {
			drawString(poseStack, this.minecraft.font, this.serverLabel, this.marginX() + 8, 35, -1);
		}

		if (!this.socialInteractionsPlayerList.isEmpty()) {
			this.socialInteractionsPlayerList.render(poseStack, i, j, f);
		} else if (!this.searchBox.getValue().isEmpty()) {
			drawCenteredString(poseStack, this.minecraft.font, EMPTY_SEARCH, this.width / 2, (78 + this.listEnd()) / 2, -1);
		} else if (this.page == SocialInteractionsScreen.Page.HIDDEN) {
			drawCenteredString(poseStack, this.minecraft.font, EMPTY_HIDDEN, this.width / 2, (78 + this.listEnd()) / 2, -1);
		} else if (this.page == SocialInteractionsScreen.Page.BLOCKED) {
			drawCenteredString(poseStack, this.minecraft.font, EMPTY_BLOCKED, this.width / 2, (78 + this.listEnd()) / 2, -1);
		}

		this.searchBox.render(poseStack, i, j, f);
		this.blockingHintButton.visible = this.page == SocialInteractionsScreen.Page.BLOCKED;
		super.render(poseStack, i, j, f);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.searchBox.isFocused()) {
			this.searchBox.mouseClicked(d, e, i);
		}

		return super.mouseClicked(d, e, i) || this.socialInteractionsPlayerList.mouseClicked(d, e, i);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (!this.searchBox.isFocused() && this.minecraft.options.keySocialInteractions.matches(i, j)) {
			this.minecraft.setScreen(null);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void checkSearchStringUpdate(String string) {
		string = string.toLowerCase(Locale.ROOT);
		if (!string.equals(this.lastSearch)) {
			this.socialInteractionsPlayerList.setFilter(string);
			this.lastSearch = string;
			this.showPage(this.page);
		}
	}

	private void updateServerLabel(Minecraft minecraft) {
		int i = minecraft.getConnection().getOnlinePlayers().size();
		if (this.playerCount != i) {
			String string = "";
			ServerData serverData = minecraft.getCurrentServer();
			if (minecraft.isLocalServer()) {
				string = minecraft.getSingleplayerServer().getMotd();
			} else if (serverData != null) {
				string = serverData.name;
			}

			if (i > 1) {
				this.serverLabel = Component.translatable("gui.socialInteractions.server_label.multiple", string, i);
			} else {
				this.serverLabel = Component.translatable("gui.socialInteractions.server_label.single", string, i);
			}

			this.playerCount = i;
		}
	}

	public void onAddPlayer(PlayerInfo playerInfo) {
		this.socialInteractionsPlayerList.addPlayer(playerInfo, this.page);
	}

	public void onRemovePlayer(UUID uUID) {
		this.socialInteractionsPlayerList.removePlayer(uUID);
	}

	@Environment(EnvType.CLIENT)
	public static enum Page {
		ALL,
		HIDDEN,
		BLOCKED;
	}
}
