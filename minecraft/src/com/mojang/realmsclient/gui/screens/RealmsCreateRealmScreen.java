package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.task.WorldCreationTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsCreateRealmScreen extends RealmsScreen {
	private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
	private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
	private static final int BUTTON_SPACING = 10;
	private static final int CONTENT_WIDTH = 210;
	private final RealmsServer server;
	private final RealmsMainScreen lastScreen;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private EditBox nameBox;
	private EditBox descriptionBox;

	public RealmsCreateRealmScreen(RealmsServer realmsServer, RealmsMainScreen realmsMainScreen) {
		super(Component.translatable("mco.selectServer.create"));
		this.server = realmsServer;
		this.lastScreen = realmsMainScreen;
	}

	@Override
	public void init() {
		this.layout.addToHeader(new StringWidget(this.title, this.font));
		LinearLayout linearLayout = this.layout.addToContents(LinearLayout.vertical()).spacing(10);
		linearLayout.defaultCellSetting().alignHorizontallyCenter();
		Button button = Button.builder(Component.translatable("mco.create.world"), buttonx -> this.createWorld()).build();
		button.active = false;
		this.nameBox = new EditBox(this.font, 208, 20, Component.translatable("mco.configure.world.name"));
		this.nameBox.setResponder(string -> button.active = !Util.isBlank(string));
		this.descriptionBox = new EditBox(this.font, 208, 20, Component.translatable("mco.configure.world.description"));
		LinearLayout linearLayout2 = linearLayout.addChild(LinearLayout.vertical().spacing(4));
		linearLayout2.addChild(new StringWidget(NAME_LABEL, this.font), LayoutSettings::alignHorizontallyLeft);
		linearLayout2.addChild(this.nameBox, layoutSettings -> layoutSettings.padding(1));
		LinearLayout linearLayout3 = linearLayout.addChild(LinearLayout.vertical().spacing(4));
		linearLayout3.addChild(new StringWidget(DESCRIPTION_LABEL, this.font), LayoutSettings::alignHorizontallyLeft);
		linearLayout3.addChild(this.descriptionBox, layoutSettings -> layoutSettings.padding(1));
		LinearLayout linearLayout4 = this.layout.addToFooter(LinearLayout.horizontal().spacing(10));
		linearLayout4.addChild(button);
		linearLayout4.addChild(Button.builder(CommonComponents.GUI_CANCEL, buttonx -> this.onClose()).build());
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

	private void createWorld() {
		RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(
			this.lastScreen,
			this.server,
			Component.translatable("mco.selectServer.create"),
			Component.translatable("mco.create.world.subtitle"),
			-6250336,
			Component.translatable("mco.create.world.skip"),
			() -> this.minecraft.execute(() -> this.minecraft.setScreen(this.lastScreen.newScreen())),
			() -> this.minecraft.setScreen(this.lastScreen.newScreen())
		);
		realmsResetWorldScreen.setResetTitle(Component.translatable("mco.create.world.reset.title"));
		this.minecraft
			.setScreen(
				new RealmsLongRunningMcoTaskScreen(
					this.lastScreen, new WorldCreationTask(this.server.id, this.nameBox.getValue(), this.descriptionBox.getValue(), realmsResetWorldScreen)
				)
			);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}
}
