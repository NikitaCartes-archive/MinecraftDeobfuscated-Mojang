package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class DemoIntroScreen extends Screen {
	private static final ResourceLocation DEMO_BACKGROUND_LOCATION = new ResourceLocation("textures/gui/demo_background.png");

	public DemoIntroScreen() {
		super(new TranslatableComponent("demo.help.title"));
	}

	@Override
	protected void init() {
		int i = -16;
		this.addButton(new Button(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20, I18n.get("demo.help.buy"), button -> {
			button.active = false;
			Util.getPlatform().openUri("http://www.minecraft.net/store?source=demo");
		}));
		this.addButton(new Button(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20, I18n.get("demo.help.later"), button -> {
			this.minecraft.setScreen(null);
			this.minecraft.mouseHandler.grabMouse();
		}));
	}

	@Override
	public void renderBackground() {
		super.renderBackground();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(DEMO_BACKGROUND_LOCATION);
		int i = (this.width - 248) / 2;
		int j = (this.height - 166) / 2;
		this.blit(i, j, 0, 0, 248, 166);
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		int k = (this.width - 248) / 2 + 10;
		int l = (this.height - 166) / 2 + 8;
		this.font.draw(this.title.getColoredString(), (float)k, (float)l, 2039583);
		l += 12;
		Options options = this.minecraft.options;
		this.font
			.draw(
				I18n.get(
					"demo.help.movementShort",
					options.keyUp.getTranslatedKeyMessage(),
					options.keyLeft.getTranslatedKeyMessage(),
					options.keyDown.getTranslatedKeyMessage(),
					options.keyRight.getTranslatedKeyMessage()
				),
				(float)k,
				(float)l,
				5197647
			);
		this.font.draw(I18n.get("demo.help.movementMouse"), (float)k, (float)(l + 12), 5197647);
		this.font.draw(I18n.get("demo.help.jump", options.keyJump.getTranslatedKeyMessage()), (float)k, (float)(l + 24), 5197647);
		this.font.draw(I18n.get("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()), (float)k, (float)(l + 36), 5197647);
		this.font.drawWordWrap(I18n.get("demo.help.fullWrapped"), k, l + 68, 218, 2039583);
		super.render(i, j, f);
	}
}
