package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public abstract class SimpleOptionsSubScreen extends OptionsSubScreen {
	protected final OptionInstance<?>[] smallOptions;
	@Nullable
	private AbstractWidget narratorButton;
	private OptionsList list;

	public SimpleOptionsSubScreen(Screen screen, Options options, Component component, OptionInstance<?>[] optionInstances) {
		super(screen, options, component);
		this.smallOptions = optionInstances;
	}

	@Override
	protected void init() {
		this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		this.list.addSmall(this.smallOptions);
		this.addWidget(this.list);
		this.createFooter();
		this.narratorButton = this.list.findOption(this.options.narrator());
		if (this.narratorButton != null) {
			this.narratorButton.active = this.minecraft.getNarrator().isActive();
		}
	}

	protected void createFooter() {
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen))
				.bounds(this.width / 2 - 100, this.height - 27, 200, 20)
				.build()
		);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.basicListRender(poseStack, this.list, i, j, f);
	}

	public void updateNarratorButton() {
		if (this.narratorButton instanceof CycleButton) {
			((CycleButton)this.narratorButton).setValue(this.options.narrator().get());
		}
	}
}
