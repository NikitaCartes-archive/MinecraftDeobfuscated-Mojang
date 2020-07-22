package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsParentalConsentScreen extends RealmsScreen {
	private static final Component MESSAGE = new TranslatableComponent("mco.account.privacyinfo");
	private final Screen nextScreen;
	private MultiLineLabel messageLines = MultiLineLabel.EMPTY;

	public RealmsParentalConsentScreen(Screen screen) {
		this.nextScreen = screen;
	}

	@Override
	public void init() {
		NarrationHelper.now(MESSAGE.getString());
		Component component = new TranslatableComponent("mco.account.update");
		Component component2 = CommonComponents.GUI_BACK;
		int i = Math.max(this.font.width(component), this.font.width(component2)) + 30;
		Component component3 = new TranslatableComponent("mco.account.privacy.info");
		int j = (int)((double)this.font.width(component3) * 1.2);
		this.addButton(new Button(this.width / 2 - j / 2, row(11), j, 20, component3, button -> Util.getPlatform().openUri("https://minecraft.net/privacy/gdpr/")));
		this.addButton(new Button(this.width / 2 - (i + 5), row(13), i, 20, component, button -> Util.getPlatform().openUri("https://minecraft.net/update-account")));
		this.addButton(new Button(this.width / 2 + 5, row(13), i, 20, component2, button -> this.minecraft.setScreen(this.nextScreen)));
		this.messageLines = MultiLineLabel.create(this.font, MESSAGE, (int)Math.round((double)this.width * 0.9));
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.messageLines.renderCentered(poseStack, this.width / 2, 15, 15, 16777215);
		super.render(poseStack, i, j, f);
	}
}
