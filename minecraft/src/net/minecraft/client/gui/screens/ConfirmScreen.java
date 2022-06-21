package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ConfirmScreen extends Screen {
	private static final int MARGIN = 20;
	private final Component message;
	private MultiLineLabel multilineMessage = MultiLineLabel.EMPTY;
	protected Component yesButton;
	protected Component noButton;
	private int delayTicker;
	protected final BooleanConsumer callback;
	private final List<Button> exitButtons = Lists.<Button>newArrayList();

	public ConfirmScreen(BooleanConsumer booleanConsumer, Component component, Component component2) {
		this(booleanConsumer, component, component2, CommonComponents.GUI_YES, CommonComponents.GUI_NO);
	}

	public ConfirmScreen(BooleanConsumer booleanConsumer, Component component, Component component2, Component component3, Component component4) {
		super(component);
		this.callback = booleanConsumer;
		this.message = component2;
		this.yesButton = component3;
		this.noButton = component4;
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(super.getNarrationMessage(), this.message);
	}

	@Override
	protected void init() {
		super.init();
		this.multilineMessage = MultiLineLabel.create(this.font, this.message, this.width - 50);
		int i = Mth.clamp(this.messageTop() + this.messageHeight() + 20, this.height / 6 + 96, this.height - 24);
		this.exitButtons.clear();
		this.addButtons(i);
	}

	protected void addButtons(int i) {
		this.addExitButton(new Button(this.width / 2 - 155, i, 150, 20, this.yesButton, button -> this.callback.accept(true)));
		this.addExitButton(new Button(this.width / 2 - 155 + 160, i, 150, 20, this.noButton, button -> this.callback.accept(false)));
	}

	protected void addExitButton(Button button) {
		this.exitButtons.add(this.addRenderableWidget(button));
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.titleTop(), 16777215);
		this.multilineMessage.renderCentered(poseStack, this.width / 2, this.messageTop());
		super.render(poseStack, i, j, f);
	}

	private int titleTop() {
		int i = (this.height - this.messageHeight()) / 2;
		return Mth.clamp(i - 20 - 9, 10, 80);
	}

	private int messageTop() {
		return this.titleTop() + 20;
	}

	private int messageHeight() {
		return this.multilineMessage.getLineCount() * 9;
	}

	public void setDelay(int i) {
		this.delayTicker = i;

		for (Button button : this.exitButtons) {
			button.active = false;
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (--this.delayTicker == 0) {
			for (Button button : this.exitButtons) {
				button.active = true;
			}
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
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
}
