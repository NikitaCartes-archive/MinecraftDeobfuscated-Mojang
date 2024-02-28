package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerModelPart;

@Environment(EnvType.CLIENT)
public class SkinCustomizationScreen extends OptionsSubScreen {
	private static final Component TITLE = Component.translatable("options.skinCustomisation.title");

	public SkinCustomizationScreen(Screen screen, Options options) {
		super(screen, options, TITLE);
	}

	@Override
	protected void init() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();
		GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);

		for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
			rowHelper.addChild(
				CycleButton.onOffBuilder(this.options.isModelPartEnabled(playerModelPart))
					.create(playerModelPart.getName(), (cycleButton, boolean_) -> this.options.toggleModelPart(playerModelPart, boolean_))
			);
		}

		rowHelper.addChild(this.options.mainHand().createButton(this.options));
		this.layout.addToContents(gridLayout);
		super.init();
	}
}
