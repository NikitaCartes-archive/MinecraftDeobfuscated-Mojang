package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsGenericErrorScreen extends RealmsScreen {
	private final Screen nextScreen;
	private Component line1;
	private Component line2;

	public RealmsGenericErrorScreen(RealmsServiceException realmsServiceException, Screen screen) {
		super(NarratorChatListener.NO_TITLE);
		this.nextScreen = screen;
		this.errorMessage(realmsServiceException);
	}

	public RealmsGenericErrorScreen(Component component, Screen screen) {
		super(NarratorChatListener.NO_TITLE);
		this.nextScreen = screen;
		this.errorMessage(component);
	}

	public RealmsGenericErrorScreen(Component component, Component component2, Screen screen) {
		super(NarratorChatListener.NO_TITLE);
		this.nextScreen = screen;
		this.errorMessage(component, component2);
	}

	private void errorMessage(RealmsServiceException realmsServiceException) {
		if (realmsServiceException.errorCode == -1) {
			this.line1 = new TextComponent("An error occurred (" + realmsServiceException.httpResultCode + "):");
			this.line2 = new TextComponent(realmsServiceException.httpResponseContent);
		} else {
			this.line1 = new TextComponent("Realms (" + realmsServiceException.errorCode + "):");
			String string = "mco.errorMessage." + realmsServiceException.errorCode;
			this.line2 = (Component)(I18n.exists(string) ? new TranslatableComponent(string) : Component.nullToEmpty(realmsServiceException.errorMsg));
		}
	}

	private void errorMessage(Component component) {
		this.line1 = new TextComponent("An error occurred: ");
		this.line2 = component;
	}

	private void errorMessage(Component component, Component component2) {
		this.line1 = component;
		this.line2 = component2;
	}

	@Override
	public void init() {
		this.addRenderableWidget(
			new Button(this.width / 2 - 100, this.height - 52, 200, 20, new TextComponent("Ok"), button -> this.minecraft.setScreen(this.nextScreen))
		);
	}

	@Override
	public Component getNarrationMessage() {
		return new TextComponent("").append(this.line1).append(": ").append(this.line2);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.line1, this.width / 2, 80, 16777215);
		drawCenteredString(poseStack, this.font, this.line2, this.width / 2, 100, 16711680);
		super.render(poseStack, i, j, f);
	}
}
