package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class PauseScreen extends Screen {
	private static final String URL_FEEDBACK_SNAPSHOT = "https://aka.ms/snapshotfeedback?ref=game";
	private static final String URL_FEEDBACK_RELEASE = "https://aka.ms/javafeedback?ref=game";
	private static final String URL_BUGS = "https://aka.ms/snapshotbugs?ref=game";
	private final boolean showPauseMenu;

	public PauseScreen(boolean bl) {
		super(bl ? new TranslatableComponent("menu.game") : new TranslatableComponent("menu.paused"));
		this.showPauseMenu = bl;
	}

	@Override
	protected void init() {
		if (this.showPauseMenu) {
			this.createPauseMenu();
		}
	}

	private void createPauseMenu() {
		int i = -16;
		int j = 98;
		this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, new TranslatableComponent("menu.returnToGame"), buttonx -> {
			this.minecraft.setScreen(null);
			this.minecraft.mouseHandler.grabMouse();
		}));
		this.addRenderableWidget(
			new Button(
				this.width / 2 - 102,
				this.height / 4 + 48 + -16,
				98,
				20,
				new TranslatableComponent("gui.advancements"),
				buttonx -> this.minecraft.setScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancements()))
			)
		);
		this.addRenderableWidget(
			new Button(
				this.width / 2 + 4,
				this.height / 4 + 48 + -16,
				98,
				20,
				new TranslatableComponent("gui.stats"),
				buttonx -> this.minecraft.setScreen(new StatsScreen(this, this.minecraft.player.getStats()))
			)
		);
		String string = SharedConstants.getCurrentVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
		this.addRenderableWidget(
			new Button(
				this.width / 2 - 102,
				this.height / 4 + 72 + -16,
				98,
				20,
				new TranslatableComponent("menu.sendFeedback"),
				buttonx -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
						if (bl) {
							Util.getPlatform().openUri(string);
						}

						this.minecraft.setScreen(this);
					}, string, true))
			)
		);
		this.addRenderableWidget(
			new Button(
				this.width / 2 + 4,
				this.height / 4 + 72 + -16,
				98,
				20,
				new TranslatableComponent("menu.reportBugs"),
				buttonx -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
						if (bl) {
							Util.getPlatform().openUri("https://aka.ms/snapshotbugs?ref=game");
						}

						this.minecraft.setScreen(this);
					}, "https://aka.ms/snapshotbugs?ref=game", true))
			)
		);
		this.addRenderableWidget(
			new Button(
				this.width / 2 - 102,
				this.height / 4 + 96 + -16,
				98,
				20,
				new TranslatableComponent("menu.options"),
				buttonx -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))
			)
		);
		Button button = this.addRenderableWidget(
			new Button(
				this.width / 2 + 4,
				this.height / 4 + 96 + -16,
				98,
				20,
				new TranslatableComponent("menu.shareToLan"),
				buttonx -> this.minecraft.setScreen(new ShareToLanScreen(this))
			)
		);
		button.active = this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished();
		Component component = this.minecraft.isLocalServer() ? new TranslatableComponent("menu.returnToMenu") : new TranslatableComponent("menu.disconnect");
		this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, component, buttonx -> {
			boolean bl = this.minecraft.isLocalServer();
			boolean bl2 = this.minecraft.isConnectedToRealms();
			buttonx.active = false;
			this.minecraft.level.disconnect();
			if (bl) {
				this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
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
		}));
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.showPauseMenu) {
			this.renderBackground(poseStack);
			drawCenteredString(poseStack, this.font, this.title, this.width / 2, 40, 16777215);
		} else {
			drawCenteredString(poseStack, this.font, this.title, this.width / 2, 10, 16777215);
		}

		super.render(poseStack, i, j, f);
	}
}
