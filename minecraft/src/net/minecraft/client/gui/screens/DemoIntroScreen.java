package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonLinks;

@Environment(EnvType.CLIENT)
public class DemoIntroScreen extends Screen {
	private static final ResourceLocation DEMO_BACKGROUND_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/demo_background.png");
	private static final int BACKGROUND_TEXTURE_WIDTH = 256;
	private static final int BACKGROUND_TEXTURE_HEIGHT = 256;
	private MultiLineLabel movementMessage = MultiLineLabel.EMPTY;
	private MultiLineLabel durationMessage = MultiLineLabel.EMPTY;

	public DemoIntroScreen() {
		super(Component.translatable("demo.help.title"));
	}

	@Override
	protected void init() {
		int i = -16;
		this.addRenderableWidget(Button.builder(Component.translatable("demo.help.buy"), button -> {
			button.active = false;
			Util.getPlatform().openUri(CommonLinks.BUY_MINECRAFT_JAVA);
		}).bounds(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("demo.help.later"), button -> {
			this.minecraft.setScreen(null);
			this.minecraft.mouseHandler.grabMouse();
		}).bounds(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20).build());
		Options options = this.minecraft.options;
		this.movementMessage = MultiLineLabel.create(
			this.font,
			Component.translatable(
				"demo.help.movementShort",
				options.keyUp.getTranslatedKeyMessage(),
				options.keyLeft.getTranslatedKeyMessage(),
				options.keyDown.getTranslatedKeyMessage(),
				options.keyRight.getTranslatedKeyMessage()
			),
			Component.translatable("demo.help.movementMouse"),
			Component.translatable("demo.help.jump", options.keyJump.getTranslatedKeyMessage()),
			Component.translatable("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage())
		);
		this.durationMessage = MultiLineLabel.create(this.font, Component.translatable("demo.help.fullWrapped"), 218);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		super.renderBackground(guiGraphics, i, j, f);
		int k = (this.width - 248) / 2;
		int l = (this.height - 166) / 2;
		guiGraphics.blit(RenderType::guiTextured, DEMO_BACKGROUND_LOCATION, k, l, 0.0F, 0.0F, 248, 166, 256, 256);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		int k = (this.width - 248) / 2 + 10;
		int l = (this.height - 166) / 2 + 8;
		guiGraphics.drawString(this.font, this.title, k, l, 2039583, false);
		l = this.movementMessage.renderLeftAlignedNoShadow(guiGraphics, k, l + 12, 12, 5197647);
		this.durationMessage.renderLeftAlignedNoShadow(guiGraphics, k, l + 20, 9, 2039583);
	}
}
