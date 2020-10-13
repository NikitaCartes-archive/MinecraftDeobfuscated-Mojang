package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class SocialInteractionsScreen extends Screen {
	protected static final ResourceLocation SOCIAL_INTERACTIONS_LOCATION = new ResourceLocation("textures/gui/social_interactions.png");
	private static final Component TAB_ALL = new TranslatableComponent("gui.socialInteractions.tab_all");
	private static final Component TAB_HIDDEN = new TranslatableComponent("gui.socialInteractions.tab_hidden");
	private static final Component TAB_ALL_SELECTED = TAB_ALL.plainCopy().withStyle(ChatFormatting.GRAY);
	private static final Component TAB_HIDDEN_SELECTED = TAB_HIDDEN.plainCopy().withStyle(ChatFormatting.GRAY);
	private static final Component SEARCH_HINT = new TranslatableComponent("gui.socialInteractions.search_hint")
		.withStyle(ChatFormatting.ITALIC)
		.withStyle(ChatFormatting.GRAY);
	private static final Component EMPTY_HIDDEN = new TranslatableComponent("gui.socialInteractions.empty_hidden").withStyle(ChatFormatting.GRAY);
	private SocialInteractionsPlayerList socialInteractionsPlayerList;
	private EditBox searchBox;
	private String lastSearch = "";
	private SocialInteractionsScreen.Page page = SocialInteractionsScreen.Page.ALL;
	private Button allButton;
	private Button hiddenButton;
	@Nullable
	private Component serverLabel;
	private int playerCount;
	private boolean showHiddenExclaim;
	private boolean initialized;
	@Nullable
	private Runnable postRenderRunnable;

	public SocialInteractionsScreen() {
		super(new TranslatableComponent("gui.socialInteractions.title"));
		this.updateServerLabel(Minecraft.getInstance());
	}

	private int windowHeight() {
		return Math.max(0, this.height - 128 - 16);
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
	public String getNarrationMessage() {
		return super.getNarrationMessage() + ". " + this.serverLabel.getString();
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		if (this.initialized) {
			this.socialInteractionsPlayerList.updateSize(this.width, this.height, 88, this.listEnd());
		} else {
			this.socialInteractionsPlayerList = new SocialInteractionsPlayerList(this, this.minecraft, this.width, this.height, 88, this.listEnd(), 36);
		}

		this.allButton = this.addButton(
			new Button(
				this.socialInteractionsPlayerList.getRowLeft() + this.socialInteractionsPlayerList.getRowWidth() / 4 - 30,
				45,
				60,
				20,
				TAB_ALL,
				button -> this.showPage(SocialInteractionsScreen.Page.ALL)
			)
		);
		this.hiddenButton = this.addButton(
			new Button(
				this.socialInteractionsPlayerList.getRowLeft() + this.socialInteractionsPlayerList.getRowWidth() / 4 * 3 - 30,
				45,
				60,
				20,
				TAB_HIDDEN,
				button -> this.showPage(SocialInteractionsScreen.Page.HIDDEN)
			)
		);
		String string = this.searchBox != null ? this.searchBox.getValue() : "";
		this.searchBox = new EditBox(this.font, this.marginX() + 28, 78, 220, 16, SEARCH_HINT);
		this.searchBox.setMaxLength(50);
		this.searchBox.setBordered(false);
		this.searchBox.setVisible(true);
		this.searchBox.setTextColor(16777215);
		this.searchBox.setValue(string);
		this.searchBox.setResponder(this::checkSearchStringUpdate);
		this.children.add(this.searchBox);
		this.children.add(this.socialInteractionsPlayerList);
		this.initialized = true;
		this.showPage(this.page);
	}

	private void showPage(SocialInteractionsScreen.Page page) {
		this.page = page;
		this.allButton.setMessage(TAB_ALL);
		this.hiddenButton.setMessage(TAB_HIDDEN);
		Collection<UUID> collection;
		switch (page) {
			case ALL:
				this.allButton.setMessage(TAB_ALL_SELECTED);
				collection = this.minecraft.player.connection.getOnlinePlayerIds();
				break;
			case HIDDEN:
				this.hiddenButton.setMessage(TAB_HIDDEN_SELECTED);
				collection = this.minecraft.getPlayerSocialManager().getHiddenPlayers();
				this.showHiddenExclaim = false;
				if (collection.isEmpty()) {
					NarratorChatListener.INSTANCE.sayNow(EMPTY_HIDDEN.getString());
				}
				break;
			default:
				collection = ImmutableList.<UUID>of();
		}

		this.page = page;
		this.socialInteractionsPlayerList.showPage(page, collection, this.socialInteractionsPlayerList.getScrollAmount());
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public void renderBackground(PoseStack poseStack) {
		int i = this.marginX() + 3;
		super.renderBackground(poseStack);
		this.minecraft.getTextureManager().bind(SOCIAL_INTERACTIONS_LOCATION);
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
		} else if (this.page == SocialInteractionsScreen.Page.HIDDEN) {
			drawCenteredString(poseStack, this.minecraft.font, EMPTY_HIDDEN, this.width / 2, (78 + this.listEnd()) / 2, -1);
		}

		if (!this.searchBox.isFocused() && this.searchBox.getValue().isEmpty()) {
			drawString(poseStack, this.minecraft.font, SEARCH_HINT, this.searchBox.x, this.searchBox.y, -1);
		} else {
			this.searchBox.render(poseStack, i, j, f);
		}

		super.render(poseStack, i, j, f);
		if (this.showHiddenExclaim) {
			this.minecraft.getTextureManager().bind(SOCIAL_INTERACTIONS_LOCATION);
			this.blit(poseStack, this.hiddenButton.x + this.hiddenButton.getWidth() - 8, 44, 249, 14, 6, 22);
		}

		if (this.postRenderRunnable != null) {
			this.postRenderRunnable.run();
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
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
			if (serverData != null) {
				string = serverData.name;
			} else if (minecraft.isLocalServer()) {
				string = minecraft.getSingleplayerServer().getMotd();
			}

			this.serverLabel = new TranslatableComponent("gui.socialInteractions.server_label", string, i);
			this.playerCount = i;
		}
	}

	public void onHiddenOrShown() {
		this.showHiddenExclaim = this.page != SocialInteractionsScreen.Page.HIDDEN && this.minecraft.getPlayerSocialManager().getHiddenPlayers().size() > 0;
	}

	public void onAddPlayer(PlayerInfo playerInfo) {
		this.socialInteractionsPlayerList.addPlayer(playerInfo, this.page);
	}

	public void onRemovePlayer(UUID uUID) {
		this.socialInteractionsPlayerList.removePlayer(uUID);
	}

	public void setPostRenderRunnable(@Nullable Runnable runnable) {
		this.postRenderRunnable = runnable;
	}

	@Environment(EnvType.CLIENT)
	public static enum Page {
		ALL,
		HIDDEN;
	}
}
