package net.minecraft.client.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerLinksScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerLinks;

@Environment(EnvType.CLIENT)
public class PauseScreen extends Screen {
	private static final ResourceLocation DRAFT_REPORT_SPRITE = ResourceLocation.withDefaultNamespace("icon/draft_report");
	private static final int COLUMNS = 2;
	private static final int MENU_PADDING_TOP = 50;
	private static final int BUTTON_PADDING = 4;
	private static final int BUTTON_WIDTH_FULL = 204;
	private static final int BUTTON_WIDTH_HALF = 98;
	private static final Component RETURN_TO_GAME = Component.translatable("menu.returnToGame");
	private static final Component ADVANCEMENTS = Component.translatable("gui.advancements");
	private static final Component STATS = Component.translatable("gui.stats");
	private static final Component SEND_FEEDBACK = Component.translatable("menu.sendFeedback");
	private static final Component REPORT_BUGS = Component.translatable("menu.reportBugs");
	private static final Component FEEDBACK_SUBSCREEN = Component.translatable("menu.feedback");
	private static final Component SERVER_LINKS = Component.translatable("menu.server_links");
	private static final Component OPTIONS = Component.translatable("menu.options");
	private static final Component SHARE_TO_LAN = Component.translatable("menu.shareToLan");
	private static final Component PLAYER_REPORTING = Component.translatable("menu.playerReporting");
	private static final Component RETURN_TO_MENU = Component.translatable("menu.returnToMenu");
	private static final Component SAVING_LEVEL = Component.translatable("menu.savingLevel");
	private static final Component GAME = Component.translatable("menu.game");
	private static final Component PAUSED = Component.translatable("menu.paused");
	private final boolean showPauseMenu;
	@Nullable
	private Button disconnectButton;

	public PauseScreen(boolean bl) {
		super(bl ? GAME : PAUSED);
		this.showPauseMenu = bl;
	}

	public boolean showsPauseMenu() {
		return this.showPauseMenu;
	}

	@Override
	protected void init() {
		if (this.showPauseMenu) {
			this.createPauseMenu();
		}

		this.addRenderableWidget(new StringWidget(0, this.showPauseMenu ? 40 : 10, this.width, 9, this.title, this.font));
	}

	private void createPauseMenu() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
		GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
		rowHelper.addChild(Button.builder(RETURN_TO_GAME, button -> {
			this.minecraft.setScreen(null);
			this.minecraft.mouseHandler.grabMouse();
		}).width(204).build(), 2, gridLayout.newCellSettings().paddingTop(50));
		rowHelper.addChild(this.openScreenButton(ADVANCEMENTS, () -> new AdvancementsScreen(this.minecraft.player.connection.getAdvancements(), this)));
		rowHelper.addChild(this.openScreenButton(STATS, () -> new StatsScreen(this, this.minecraft.player.getStats())));
		ServerLinks serverLinks = this.minecraft.player.connection.serverLinks();
		if (serverLinks.isEmpty()) {
			addFeedbackButtons(this, rowHelper);
		} else {
			rowHelper.addChild(this.openScreenButton(FEEDBACK_SUBSCREEN, () -> new PauseScreen.FeedbackSubScreen(this)));
			rowHelper.addChild(this.openScreenButton(SERVER_LINKS, () -> new ServerLinksScreen(this, serverLinks)));
		}

		rowHelper.addChild(this.openScreenButton(OPTIONS, () -> new OptionsScreen(this, this.minecraft.options)));
		if (this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished()) {
			rowHelper.addChild(this.openScreenButton(SHARE_TO_LAN, () -> new ShareToLanScreen(this)));
		} else {
			rowHelper.addChild(this.openScreenButton(PLAYER_REPORTING, () -> new SocialInteractionsScreen(this)));
		}

		Component component = this.minecraft.isLocalServer() ? RETURN_TO_MENU : CommonComponents.GUI_DISCONNECT;
		this.disconnectButton = rowHelper.addChild(Button.builder(component, button -> {
			button.active = false;
			this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::onDisconnect, true);
		}).width(204).build(), 2);
		gridLayout.arrangeElements();
		FrameLayout.alignInRectangle(gridLayout, 0, 0, this.width, this.height, 0.5F, 0.25F);
		gridLayout.visitWidgets(this::addRenderableWidget);
	}

	static void addFeedbackButtons(Screen screen, GridLayout.RowHelper rowHelper) {
		rowHelper.addChild(
			openLinkButton(
				screen, SEND_FEEDBACK, SharedConstants.getCurrentVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game"
			)
		);
		rowHelper.addChild(openLinkButton(screen, REPORT_BUGS, "https://aka.ms/snapshotbugs?ref=game")).active = !SharedConstants.getCurrentVersion()
			.getDataVersion()
			.isSideSeries();
	}

	private void onDisconnect() {
		boolean bl = this.minecraft.isLocalServer();
		ServerData serverData = this.minecraft.getCurrentServer();
		this.minecraft.level.disconnect();
		if (bl) {
			this.minecraft.disconnect(new GenericMessageScreen(SAVING_LEVEL));
		} else {
			this.minecraft.disconnect();
		}

		TitleScreen titleScreen = new TitleScreen();
		if (bl) {
			this.minecraft.setScreen(titleScreen);
		} else if (serverData != null && serverData.isRealm()) {
			this.minecraft.setScreen(new RealmsMainScreen(titleScreen));
		} else {
			this.minecraft.setScreen(new JoinMultiplayerScreen(titleScreen));
		}
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		if (this.showPauseMenu && this.minecraft != null && this.minecraft.getReportingContext().hasDraftReport() && this.disconnectButton != null) {
			guiGraphics.blitSprite(DRAFT_REPORT_SPRITE, this.disconnectButton.getX() + this.disconnectButton.getWidth() - 17, this.disconnectButton.getY() + 3, 15, 15);
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.showPauseMenu) {
			super.renderBackground(guiGraphics, i, j, f);
		}
	}

	private Button openScreenButton(Component component, Supplier<Screen> supplier) {
		return Button.builder(component, button -> this.minecraft.setScreen((Screen)supplier.get())).width(98).build();
	}

	private static Button openLinkButton(Screen screen, Component component, String string) {
		return Button.builder(component, ConfirmLinkScreen.confirmLink(screen, string)).width(98).build();
	}

	@Environment(EnvType.CLIENT)
	static class FeedbackSubScreen extends Screen {
		private static final Component TITLE = Component.translatable("menu.feedback.title");
		public final Screen parent;
		private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

		protected FeedbackSubScreen(Screen screen) {
			super(TITLE);
			this.parent = screen;
		}

		@Override
		protected void init() {
			this.layout.addTitleHeader(TITLE, this.font);
			GridLayout gridLayout = this.layout.addToContents(new GridLayout());
			gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
			GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
			PauseScreen.addFeedbackButtons(this, rowHelper);
			this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).width(200).build());
			this.layout.visitWidgets(this::addRenderableWidget);
			this.repositionElements();
		}

		@Override
		protected void repositionElements() {
			this.layout.arrangeElements();
		}

		@Override
		public void onClose() {
			this.minecraft.setScreen(this.parent);
		}
	}
}
