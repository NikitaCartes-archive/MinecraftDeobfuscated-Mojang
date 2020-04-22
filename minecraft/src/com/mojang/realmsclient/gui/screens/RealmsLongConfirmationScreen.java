package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsLongConfirmationScreen extends RealmsScreen {
	private final RealmsLongConfirmationScreen.Type type;
	private final Component line2;
	private final Component line3;
	protected final BooleanConsumer callback;
	private final boolean yesNoQuestion;

	public RealmsLongConfirmationScreen(
		BooleanConsumer booleanConsumer, RealmsLongConfirmationScreen.Type type, Component component, Component component2, boolean bl
	) {
		this.callback = booleanConsumer;
		this.type = type;
		this.line2 = component;
		this.line3 = component2;
		this.yesNoQuestion = bl;
	}

	@Override
	public void init() {
		NarrationHelper.now(this.type.text, this.line2.getString(), this.line3.getString());
		if (this.yesNoQuestion) {
			this.addButton(new Button(this.width / 2 - 105, row(8), 100, 20, CommonComponents.GUI_YES, button -> this.callback.accept(true)));
			this.addButton(new Button(this.width / 2 + 5, row(8), 100, 20, CommonComponents.GUI_NO, button -> this.callback.accept(false)));
		} else {
			this.addButton(new Button(this.width / 2 - 50, row(8), 100, 20, new TranslatableComponent("mco.gui.ok"), button -> this.callback.accept(true)));
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.callback.accept(false);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.type.text, this.width / 2, row(2), this.type.colorCode);
		this.drawCenteredString(poseStack, this.font, this.line2, this.width / 2, row(4), 16777215);
		this.drawCenteredString(poseStack, this.font, this.line3, this.width / 2, row(6), 16777215);
		super.render(poseStack, i, j, f);
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		Warning("Warning!", 16711680),
		Info("Info!", 8226750);

		public final int colorCode;
		public final String text;

		private Type(String string2, int j) {
			this.text = string2;
			this.colorCode = j;
		}
	}
}
