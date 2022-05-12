package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public abstract class WarningScreen extends Screen {
	private final Component titleComponent;
	private final Component content;
	private final Component check;
	private final Component narration;
	@Nullable
	protected Checkbox stopShowing;
	private MultiLineLabel message = MultiLineLabel.EMPTY;

	protected WarningScreen(Component component, Component component2, Component component3, Component component4) {
		super(NarratorChatListener.NO_TITLE);
		this.titleComponent = component;
		this.content = component2;
		this.check = component3;
		this.narration = component4;
	}

	protected abstract void initButtons(int i);

	@Override
	protected void init() {
		super.init();
		this.message = MultiLineLabel.create(this.font, this.content, this.width - 100);
		int i = (this.message.getLineCount() + 1) * this.getLineHeight();
		int j = this.font.width(this.check);
		this.stopShowing = new Checkbox(this.width / 2 - j / 2 - 8, 76 + i, j + 24, 20, this.check, false);
		this.addRenderableWidget(this.stopShowing);
		this.initButtons(i);
	}

	@Override
	public Component getNarrationMessage() {
		return this.narration;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawString(poseStack, this.font, this.titleComponent, 25, 30, 16777215);
		int k = this.width / 2 - this.message.getWidth() / 2;
		this.message.renderLeftAligned(poseStack, k, 70, this.getLineHeight(), 16777215);
		super.render(poseStack, i, j, f);
	}

	protected int getLineHeight() {
		return 9 * 2;
	}
}
