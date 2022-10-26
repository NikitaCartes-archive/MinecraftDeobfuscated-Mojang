package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsParentalConsentScreen extends RealmsScreen {
	private static final Component MESSAGE = Component.translatable("mco.account.privacyinfo");
	private final Screen nextScreen;
	private MultiLineLabel messageLines = MultiLineLabel.EMPTY;

	public RealmsParentalConsentScreen(Screen screen) {
		super(GameNarrator.NO_TITLE);
		this.nextScreen = screen;
	}

	@Override
	public void init() {
		Component component = Component.translatable("mco.account.update");
		Component component2 = CommonComponents.GUI_BACK;
		int i = Math.max(this.font.width(component), this.font.width(component2)) + 30;
		Component component3 = Component.translatable("mco.account.privacy.info");
		int j = (int)((double)this.font.width(component3) * 1.2);
		this.addRenderableWidget(
			Button.builder(component3, button -> Util.getPlatform().openUri("https://aka.ms/MinecraftGDPR")).bounds(this.width / 2 - j / 2, row(11), j, 20).build()
		);
		this.addRenderableWidget(
			Button.builder(component, button -> Util.getPlatform().openUri("https://aka.ms/UpdateMojangAccount"))
				.bounds(this.width / 2 - (i + 5), row(13), i, 20)
				.build()
		);
		this.addRenderableWidget(Button.builder(component2, button -> this.minecraft.setScreen(this.nextScreen)).bounds(this.width / 2 + 5, row(13), i, 20).build());
		this.messageLines = MultiLineLabel.create(this.font, MESSAGE, (int)Math.round((double)this.width * 0.9));
	}

	@Override
	public Component getNarrationMessage() {
		return MESSAGE;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.messageLines.renderCentered(poseStack, this.width / 2, 15, 15, 16777215);
		super.render(poseStack, i, j, f);
	}
}
