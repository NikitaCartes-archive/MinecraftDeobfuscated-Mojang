package net.minecraft.client.gui.screens;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerModelPart;

@Environment(EnvType.CLIENT)
public class SkinCustomizationScreen extends OptionsSubScreen {
	private static final Component TITLE = Component.translatable("options.skinCustomisation.title");
	@Nullable
	private OptionsList list;

	public SkinCustomizationScreen(Screen screen, Options options) {
		super(screen, options, TITLE);
	}

	@Override
	protected void init() {
		this.list = this.addRenderableWidget(new OptionsList(this.minecraft, this.width, this.height, this));
		List<AbstractWidget> list = new ArrayList();

		for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
			list.add(
				CycleButton.onOffBuilder(this.options.isModelPartEnabled(playerModelPart))
					.create(playerModelPart.getName(), (cycleButton, boolean_) -> this.options.toggleModelPart(playerModelPart, boolean_))
			);
		}

		list.add(this.options.mainHand().createButton(this.options));
		this.list.addSmall(list);
		super.init();
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		if (this.list != null) {
			this.list.updateSize(this.width, this.layout);
		}
	}
}
