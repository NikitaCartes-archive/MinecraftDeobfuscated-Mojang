package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsInviteScreen extends RealmsScreen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component NAME_LABEL = Component.translatable("mco.configure.world.invite.profile.name").withColor(-6250336);
	private static final Component INVITING_PLAYER_TEXT = Component.translatable("mco.configure.world.players.inviting").withColor(-6250336);
	private static final Component NO_SUCH_PLAYER_ERROR_TEXT = Component.translatable("mco.configure.world.players.error").withColor(-65536);
	private EditBox profileName;
	private Button inviteButton;
	private final RealmsServer serverData;
	private final RealmsConfigureWorldScreen configureScreen;
	private final Screen lastScreen;
	@Nullable
	private Component message;

	public RealmsInviteScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, Screen screen, RealmsServer realmsServer) {
		super(GameNarrator.NO_TITLE);
		this.configureScreen = realmsConfigureWorldScreen;
		this.lastScreen = screen;
		this.serverData = realmsServer;
	}

	@Override
	public void init() {
		this.profileName = new EditBox(
			this.minecraft.font, this.width / 2 - 100, row(2), 200, 20, null, Component.translatable("mco.configure.world.invite.profile.name")
		);
		this.addWidget(this.profileName);
		this.setInitialFocus(this.profileName);
		this.inviteButton = this.addRenderableWidget(
			Button.builder(Component.translatable("mco.configure.world.buttons.invite"), button -> this.onInvite())
				.bounds(this.width / 2 - 100, row(10), 200, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 100, row(12), 200, 20).build()
		);
	}

	private void onInvite() {
		if (Util.isBlank(this.profileName.getValue())) {
			this.showMessage(NO_SUCH_PLAYER_ERROR_TEXT);
		} else {
			long l = this.serverData.id;
			String string = this.profileName.getValue().trim();
			this.inviteButton.active = false;
			this.profileName.setEditable(false);
			this.showMessage(INVITING_PLAYER_TEXT);
			CompletableFuture.supplyAsync(() -> {
				try {
					return RealmsClient.create().invite(l, string);
				} catch (Exception var4) {
					LOGGER.error("Couldn't invite user");
					return null;
				}
			}, Util.ioPool()).thenAcceptAsync(realmsServer -> {
				if (realmsServer != null) {
					this.serverData.players = realmsServer.players;
					this.minecraft.setScreen(new RealmsPlayerScreen(this.configureScreen, this.serverData));
				} else {
					this.showMessage(NO_SUCH_PLAYER_ERROR_TEXT);
				}

				this.profileName.setEditable(true);
				this.inviteButton.active = true;
			}, this.screenExecutor);
		}
	}

	private void showMessage(Component component) {
		this.message = component;
		this.minecraft.getNarrator().sayNow(component);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.minecraft.setScreen(this.lastScreen);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.drawString(this.font, NAME_LABEL, this.width / 2 - 100, row(1), -1, false);
		if (this.message != null) {
			guiGraphics.drawCenteredString(this.font, this.message, this.width / 2, row(5), -1);
		}

		this.profileName.render(guiGraphics, i, j, f);
	}
}
