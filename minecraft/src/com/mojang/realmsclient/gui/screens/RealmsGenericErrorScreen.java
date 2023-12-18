package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsError;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsGenericErrorScreen extends RealmsScreen {
	private final Screen nextScreen;
	private final RealmsGenericErrorScreen.ErrorMessage lines;
	private MultiLineLabel line2Split = MultiLineLabel.EMPTY;

	public RealmsGenericErrorScreen(RealmsServiceException realmsServiceException, Screen screen) {
		super(GameNarrator.NO_TITLE);
		this.nextScreen = screen;
		this.lines = errorMessage(realmsServiceException);
	}

	public RealmsGenericErrorScreen(Component component, Screen screen) {
		super(GameNarrator.NO_TITLE);
		this.nextScreen = screen;
		this.lines = errorMessage(component);
	}

	public RealmsGenericErrorScreen(Component component, Component component2, Screen screen) {
		super(GameNarrator.NO_TITLE);
		this.nextScreen = screen;
		this.lines = errorMessage(component, component2);
	}

	private static RealmsGenericErrorScreen.ErrorMessage errorMessage(RealmsServiceException realmsServiceException) {
		RealmsError realmsError = realmsServiceException.realmsError;
		return errorMessage(Component.translatable("mco.errorMessage.realmsService.realmsError", realmsError.errorCode()), realmsError.errorMessage());
	}

	private static RealmsGenericErrorScreen.ErrorMessage errorMessage(Component component) {
		return errorMessage(Component.translatable("mco.errorMessage.generic"), component);
	}

	private static RealmsGenericErrorScreen.ErrorMessage errorMessage(Component component, Component component2) {
		return new RealmsGenericErrorScreen.ErrorMessage(component, component2);
	}

	@Override
	public void init() {
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_OK, button -> this.onClose()).bounds(this.width / 2 - 100, this.height - 52, 200, 20).build());
		this.line2Split = MultiLineLabel.create(this.font, this.lines.detail, this.width * 3 / 4);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.nextScreen);
	}

	@Override
	public Component getNarrationMessage() {
		return Component.empty().append(this.lines.title).append(": ").append(this.lines.detail);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.lines.title, this.width / 2, 80, -1);
		this.line2Split.renderCentered(guiGraphics, this.width / 2, 100, 9, -2142128);
	}

	@Environment(EnvType.CLIENT)
	static record ErrorMessage(Component title, Component detail) {
	}
}
