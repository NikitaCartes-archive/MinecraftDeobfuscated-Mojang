package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CenteredStringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class PauseScreen extends Screen {
	private static final String URL_FEEDBACK_SNAPSHOT = "https://aka.ms/snapshotfeedback?ref=game";
	private static final String URL_FEEDBACK_RELEASE = "https://aka.ms/javafeedback?ref=game";
	private static final String URL_BUGS = "https://aka.ms/snapshotbugs?ref=game";
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
	private static final Component OPTIONS = Component.translatable("menu.options");
	private static final Component SHARE_TO_LAN = Component.translatable("menu.shareToLan");
	private static final Component PLAYER_REPORTING = Component.translatable("menu.playerReporting");
	private static final Component RETURN_TO_MENU = Component.translatable("menu.returnToMenu");
	private static final Component DISCONNECT = Component.translatable("menu.disconnect");
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

	@Override
	protected void init() {
		if (this.showPauseMenu) {
			this.createPauseMenu();
		}

		this.addRenderableWidget(new CenteredStringWidget(0, this.showPauseMenu ? 40 : 10, this.width, 9, this.title, this.font));
	}

	private void createPauseMenu() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
		GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
		rowHelper.addChild(Button.builder(RETURN_TO_GAME, button -> {
			this.minecraft.setScreen(null);
			this.minecraft.mouseHandler.grabMouse();
		}).width(204).build(), 2, gridLayout.newCellSettings().paddingTop(50));
		rowHelper.addChild(this.openScreenButton(ADVANCEMENTS, () -> new AdvancementsScreen(this.minecraft.player.connection.getAdvancements())));
		rowHelper.addChild(this.openScreenButton(STATS, () -> new StatsScreen(this, this.minecraft.player.getStats())));
		rowHelper.addChild(
			this.openLinkButton(
				SEND_FEEDBACK, SharedConstants.getCurrentVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game"
			)
		);
		rowHelper.addChild(this.openLinkButton(REPORT_BUGS, "https://aka.ms/snapshotbugs?ref=game")).active = !SharedConstants.getCurrentVersion()
			.getDataVersion()
			.isSideSeries();
		rowHelper.addChild(this.openScreenButton(OPTIONS, () -> new OptionsScreen(this, this.minecraft.options)));
		if (this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished()) {
			rowHelper.addChild(this.openScreenButton(SHARE_TO_LAN, () -> new ShareToLanScreen(this)));
		} else {
			rowHelper.addChild(this.openScreenButton(PLAYER_REPORTING, SocialInteractionsScreen::new));
		}

		Component component = this.minecraft.isLocalServer() ? RETURN_TO_MENU : DISCONNECT;
		this.disconnectButton = rowHelper.addChild(Button.builder(component, button -> {
			button.active = false;
			this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::onDisconnect, true);
		}).width(204).build(), 2);
		gridLayout.arrangeElements();
		FrameLayout.alignInRectangle(gridLayout, 0, 0, this.width, this.height, 0.5F, 0.25F);
		gridLayout.visitWidgets(this::addRenderableWidget);
	}

	private void onDisconnect() {
		boolean bl = this.minecraft.isLocalServer();
		boolean bl2 = this.minecraft.isConnectedToRealms();
		this.minecraft.level.disconnect();
		if (bl) {
			this.minecraft.clearLevel(new GenericDirtMessageScreen(SAVING_LEVEL));
		} else {
			this.minecraft.clearLevel();
		}

		TitleScreen titleScreen = new TitleScreen();
		if (bl) {
			this.minecraft.setScreen(titleScreen);
		} else if (bl2) {
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.showPauseMenu) {
			this.renderBackground(poseStack);
		}

		super.render(poseStack, i, j, f);
		if (this.showPauseMenu && this.minecraft != null && this.minecraft.getReportingContext().hasDraftReport() && this.disconnectButton != null) {
			RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
			this.blit(poseStack, this.disconnectButton.getX() + this.disconnectButton.getWidth() - 17, this.disconnectButton.getY() + 3, 182, 24, 15, 15);
		}
	}

	private Button openScreenButton(Component component, Supplier<Screen> supplier) {
		return Button.builder(component, button -> this.minecraft.setScreen((Screen)supplier.get())).width(98).build();
	}

	private Button openLinkButton(Component component, String string) {
		return this.openScreenButton(component, () -> new ConfirmLinkScreen(bl -> {
				if (bl) {
					Util.getPlatform().openUri(string);
				}

				this.minecraft.setScreen(this);
			}, string, true));
	}
}
