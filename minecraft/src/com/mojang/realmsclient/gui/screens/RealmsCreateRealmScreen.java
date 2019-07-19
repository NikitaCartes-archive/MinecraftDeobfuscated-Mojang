package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.RealmsTasks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsCreateRealmScreen extends RealmsScreen {
	private final RealmsServer server;
	private final RealmsMainScreen lastScreen;
	private RealmsEditBox nameBox;
	private RealmsEditBox descriptionBox;
	private RealmsButton createButton;
	private RealmsLabel createRealmLabel;

	public RealmsCreateRealmScreen(RealmsServer realmsServer, RealmsMainScreen realmsMainScreen) {
		this.server = realmsServer;
		this.lastScreen = realmsMainScreen;
	}

	@Override
	public void tick() {
		if (this.nameBox != null) {
			this.nameBox.tick();
		}

		if (this.descriptionBox != null) {
			this.descriptionBox.tick();
		}
	}

	@Override
	public void init() {
		this.setKeyboardHandlerSendRepeatsToGui(true);
		this.buttonsAdd(
			this.createButton = new RealmsButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 17, 97, 20, getLocalizedString("mco.create.world")) {
				@Override
				public void onPress() {
					RealmsCreateRealmScreen.this.createWorld();
				}
			}
		);
		this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, this.height() / 4 + 120 + 17, 95, 20, getLocalizedString("gui.cancel")) {
			@Override
			public void onPress() {
				Realms.setScreen(RealmsCreateRealmScreen.this.lastScreen);
			}
		});
		this.createButton.active(false);
		this.nameBox = this.newEditBox(3, this.width() / 2 - 100, 65, 200, 20, getLocalizedString("mco.configure.world.name"));
		this.addWidget(this.nameBox);
		this.focusOn(this.nameBox);
		this.descriptionBox = this.newEditBox(4, this.width() / 2 - 100, 115, 200, 20, getLocalizedString("mco.configure.world.description"));
		this.addWidget(this.descriptionBox);
		this.createRealmLabel = new RealmsLabel(getLocalizedString("mco.selectServer.create"), this.width() / 2, 11, 16777215);
		this.addWidget(this.createRealmLabel);
		this.narrateLabels();
	}

	@Override
	public void removed() {
		this.setKeyboardHandlerSendRepeatsToGui(false);
	}

	@Override
	public boolean charTyped(char c, int i) {
		this.createButton.active(this.valid());
		return false;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		switch (i) {
			case 256:
				Realms.setScreen(this.lastScreen);
				return true;
			default:
				this.createButton.active(this.valid());
				return false;
		}
	}

	private void createWorld() {
		if (this.valid()) {
			RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(
				this.lastScreen,
				this.server,
				this.lastScreen.newScreen(),
				getLocalizedString("mco.selectServer.create"),
				getLocalizedString("mco.create.world.subtitle"),
				10526880,
				getLocalizedString("mco.create.world.skip")
			);
			realmsResetWorldScreen.setResetTitle(getLocalizedString("mco.create.world.reset.title"));
			RealmsTasks.WorldCreationTask worldCreationTask = new RealmsTasks.WorldCreationTask(
				this.server.id, this.nameBox.getValue(), this.descriptionBox.getValue(), realmsResetWorldScreen
			);
			RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, worldCreationTask);
			realmsLongRunningMcoTaskScreen.start();
			Realms.setScreen(realmsLongRunningMcoTaskScreen);
		}
	}

	private boolean valid() {
		return this.nameBox.getValue() != null && !this.nameBox.getValue().trim().isEmpty();
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.createRealmLabel.render(this);
		this.drawString(getLocalizedString("mco.configure.world.name"), this.width() / 2 - 100, 52, 10526880);
		this.drawString(getLocalizedString("mco.configure.world.description"), this.width() / 2 - 100, 102, 10526880);
		if (this.nameBox != null) {
			this.nameBox.render(i, j, f);
		}

		if (this.descriptionBox != null) {
			this.descriptionBox.render(i, j, f);
		}

		super.render(i, j, f);
	}
}
