package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class AlertScreen extends Screen {
	private final Runnable callback;
	protected final Component text;
	private final List<Component> lines = Lists.<Component>newArrayList();
	protected final Component okButton;
	private int delayTicker;

	public AlertScreen(Runnable runnable, Component component, Component component2) {
		this(runnable, component, component2, CommonComponents.GUI_BACK);
	}

	public AlertScreen(Runnable runnable, Component component, Component component2, Component component3) {
		super(component);
		this.callback = runnable;
		this.text = component2;
		this.okButton = component3;
	}

	@Override
	protected void init() {
		super.init();
		this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, this.okButton, button -> this.callback.run()));
		this.lines.clear();
		this.lines.addAll(this.font.split(this.text, this.width - 50));
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 70, 16777215);
		int k = 90;

		for (Component component : this.lines) {
			this.drawCenteredString(poseStack, this.font, component, this.width / 2, k, 16777215);
			k += 9;
		}

		super.render(poseStack, i, j, f);
	}

	@Override
	public void tick() {
		super.tick();
		if (--this.delayTicker == 0) {
			for (AbstractWidget abstractWidget : this.buttons) {
				abstractWidget.active = true;
			}
		}
	}
}
