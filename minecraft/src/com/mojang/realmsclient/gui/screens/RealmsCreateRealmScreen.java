package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.task.CreateSnapshotRealmTask;
import com.mojang.realmsclient.util.task.WorldCreationTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsCreateRealmScreen extends RealmsScreen {
	private static final Component CREATE_REALM_TEXT = Component.translatable("mco.selectServer.create");
	private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
	private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
	private static final int BUTTON_SPACING = 10;
	private static final int CONTENT_WIDTH = 210;
	private final RealmsMainScreen lastScreen;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private EditBox nameBox;
	private EditBox descriptionBox;
	private final Runnable createWorldRunnable;

	public RealmsCreateRealmScreen(RealmsMainScreen realmsMainScreen, RealmsServer realmsServer) {
		super(CREATE_REALM_TEXT);
		this.lastScreen = realmsMainScreen;
		this.createWorldRunnable = () -> this.createWorld(realmsServer);
	}

	public RealmsCreateRealmScreen(RealmsMainScreen realmsMainScreen, long l) {
		super(CREATE_REALM_TEXT);
		this.lastScreen = realmsMainScreen;
		this.createWorldRunnable = () -> this.createSnapshotWorld(l);
	}

	@Override
	public void init() {
		this.layout.addToHeader(new StringWidget(this.title, this.font));
		LinearLayout linearLayout = this.layout.addToContents(LinearLayout.vertical()).spacing(10);
		Button button = Button.builder(CommonComponents.GUI_CONTINUE, buttonx -> this.createWorldRunnable.run()).build();
		button.active = false;
		this.nameBox = new EditBox(this.font, 210, 20, NAME_LABEL);
		this.nameBox.setResponder(string -> button.active = !Util.isBlank(string));
		this.descriptionBox = new EditBox(this.font, 210, 20, DESCRIPTION_LABEL);
		linearLayout.addChild(CommonLayouts.labeledElement(this.font, this.nameBox, NAME_LABEL));
		linearLayout.addChild(CommonLayouts.labeledElement(this.font, this.descriptionBox, DESCRIPTION_LABEL));
		LinearLayout linearLayout2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(10));
		linearLayout2.addChild(button);
		linearLayout2.addChild(Button.builder(CommonComponents.GUI_BACK, buttonx -> this.onClose()).build());
		this.layout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		this.repositionElements();
		this.setInitialFocus(this.nameBox);
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	private void createWorld(RealmsServer realmsServer) {
		WorldCreationTask worldCreationTask = new WorldCreationTask(realmsServer.id, this.nameBox.getValue(), this.descriptionBox.getValue());
		RealmsResetWorldScreen realmsResetWorldScreen = RealmsResetWorldScreen.forNewRealm(
			this, realmsServer, worldCreationTask, () -> this.minecraft.execute(() -> {
					RealmsMainScreen.refreshServerList();
					this.minecraft.setScreen(this.lastScreen);
				})
		);
		this.minecraft.setScreen(realmsResetWorldScreen);
	}

	private void createSnapshotWorld(long l) {
		Screen screen = new RealmsResetNormalWorldScreen(
			worldGenerationInfo -> {
				if (worldGenerationInfo == null) {
					this.minecraft.setScreen(this);
				} else {
					this.minecraft
						.setScreen(
							new RealmsLongRunningMcoTaskScreen(
								this, new CreateSnapshotRealmTask(this.lastScreen, l, worldGenerationInfo, this.nameBox.getValue(), this.descriptionBox.getValue())
							)
						);
				}
			},
			CREATE_REALM_TEXT
		);
		this.minecraft.setScreen(screen);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}
}
