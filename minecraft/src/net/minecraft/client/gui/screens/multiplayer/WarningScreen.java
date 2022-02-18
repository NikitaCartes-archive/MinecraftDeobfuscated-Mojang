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
	protected final Screen previous;
	@Nullable
	protected Checkbox stopShowing;
	private MultiLineLabel message = MultiLineLabel.EMPTY;

	protected WarningScreen(Component component, Component component2, Component component3, Component component4, Screen screen) {
		super(NarratorChatListener.NO_TITLE);
		this.titleComponent = component;
		this.content = component2;
		this.check = component3;
		this.narration = component4;
		this.previous = screen;
	}

	protected abstract void initButtons(int i);

	@Override
	protected void init() {
		super.init();
		this.message = MultiLineLabel.create(this.font, this.content, this.width - 50);
		int i = (this.message.getLineCount() + 1) * 9 * 2;
		this.stopShowing = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, this.check, false);
		this.addRenderableWidget(this.stopShowing);
		this.initButtons(i);
	}

	@Override
	public Component getNarrationMessage() {
		return this.narration;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderDirtBackground(0);
		drawString(poseStack, this.font, this.titleComponent, 25, 30, 16777215);
		this.message.renderLeftAligned(poseStack, 25, 70, 9 * 2, 16777215);
		super.render(poseStack, i, j, f);
	}
}
