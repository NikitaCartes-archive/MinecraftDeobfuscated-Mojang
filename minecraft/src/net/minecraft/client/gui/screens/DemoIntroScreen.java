package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class DemoIntroScreen extends Screen {
	private static final ResourceLocation DEMO_BACKGROUND_LOCATION = new ResourceLocation("textures/gui/demo_background.png");
	private MultiLineLabel movementMessage = MultiLineLabel.EMPTY;
	private MultiLineLabel durationMessage = MultiLineLabel.EMPTY;

	public DemoIntroScreen() {
		super(Component.translatable("demo.help.title"));
	}

	@Override
	protected void init() {
		int i = -16;
		this.addRenderableWidget(new Button(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20, Component.translatable("demo.help.buy"), button -> {
			button.active = false;
			Util.getPlatform().openUri("http://www.minecraft.net/store?source=demo");
		}));
		this.addRenderableWidget(new Button(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20, Component.translatable("demo.help.later"), button -> {
			this.minecraft.setScreen(null);
			this.minecraft.mouseHandler.grabMouse();
		}));
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
	public void renderBackground(PoseStack poseStack) {
		super.renderBackground(poseStack);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, DEMO_BACKGROUND_LOCATION);
		int i = (this.width - 248) / 2;
		int j = (this.height - 166) / 2;
		this.blit(poseStack, i, j, 0, 0, 248, 166);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		int k = (this.width - 248) / 2 + 10;
		int l = (this.height - 166) / 2 + 8;
		this.font.draw(poseStack, this.title, (float)k, (float)l, 2039583);
		l = this.movementMessage.renderLeftAlignedNoShadow(poseStack, k, l + 12, 12, 5197647);
		this.durationMessage.renderLeftAlignedNoShadow(poseStack, k, l + 20, 9, 2039583);
		super.render(poseStack, i, j, f);
	}
}
