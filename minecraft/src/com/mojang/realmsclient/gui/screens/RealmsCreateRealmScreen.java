package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.task.RealmCreationTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.StringUtil;

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

	public RealmsCreateRealmScreen(RealmsMainScreen realmsMainScreen, RealmsServer realmsServer, boolean bl) {
		super(CREATE_REALM_TEXT);
		this.lastScreen = realmsMainScreen;
		this.createWorldRunnable = () -> this.createWorld(realmsServer, bl);
	}

	@Override
	public void init() {
		this.layout.addTitleHeader(this.title, this.font);
		LinearLayout linearLayout = this.layout.addToContents(LinearLayout.vertical()).spacing(10);
		Button button = Button.builder(CommonComponents.GUI_CONTINUE, buttonx -> this.createWorldRunnable.run()).build();
		button.active = false;
		this.nameBox = new EditBox(this.font, 210, 20, NAME_LABEL);
		this.nameBox.setResponder(string -> button.active = !StringUtil.isBlank(string));
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
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.nameBox);
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	private void createWorld(RealmsServer realmsServer, boolean bl) {
		if (!realmsServer.isSnapshotRealm() && bl) {
			AtomicBoolean atomicBoolean = new AtomicBoolean();
			this.minecraft.setScreen(new AlertScreen(() -> {
				atomicBoolean.set(true);
				this.lastScreen.resetScreen();
				this.minecraft.setScreen(this.lastScreen);
			}, Component.translatable("mco.upload.preparing"), Component.empty()));
			CompletableFuture.supplyAsync(() -> createSnapshotRealm(realmsServer), Util.backgroundExecutor()).thenAcceptAsync(realmsServerx -> {
				if (!atomicBoolean.get()) {
					this.showResetWorldScreen(realmsServerx);
				}
			}, this.minecraft).exceptionallyAsync(throwable -> {
				this.lastScreen.resetScreen();
				Component component;
				if (throwable.getCause() instanceof RealmsServiceException realmsServiceException) {
					component = realmsServiceException.realmsError.errorMessage();
				} else {
					component = Component.translatable("mco.errorMessage.initialize.failed");
				}

				this.minecraft.setScreen(new RealmsGenericErrorScreen(component, this.lastScreen));
				return null;
			}, this.minecraft);
		} else {
			this.showResetWorldScreen(realmsServer);
		}
	}

	private static RealmsServer createSnapshotRealm(RealmsServer realmsServer) {
		RealmsClient realmsClient = RealmsClient.create();

		try {
			return realmsClient.createSnapshotRealm(realmsServer.id);
		} catch (RealmsServiceException var3) {
			throw new RuntimeException(var3);
		}
	}

	private void showResetWorldScreen(RealmsServer realmsServer) {
		RealmCreationTask realmCreationTask = new RealmCreationTask(realmsServer.id, this.nameBox.getValue(), this.descriptionBox.getValue());
		RealmsResetWorldScreen realmsResetWorldScreen = RealmsResetWorldScreen.forNewRealm(
			this, realmsServer, realmCreationTask, () -> this.minecraft.execute(() -> {
					RealmsMainScreen.refreshServerList();
					this.minecraft.setScreen(this.lastScreen);
				})
		);
		this.minecraft.setScreen(realmsResetWorldScreen);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}
}
