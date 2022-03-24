package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Difficulty;

@Environment(EnvType.CLIENT)
public class OnlineOptionsScreen extends SimpleOptionsSubScreen {
	public OnlineOptionsScreen(Screen screen, Options options) {
		super(screen, options, new TranslatableComponent("options.online.title"), new OptionInstance[]{options.realmsNotifications(), options.allowServerListing()});
	}

	@Override
	protected void createFooter() {
		if (this.minecraft.level != null) {
			CycleButton<Difficulty> cycleButton = this.addRenderableWidget(
				OptionsScreen.createDifficultyButton(this.smallOptions.length, this.width, this.height, "options.difficulty.online", this.minecraft)
			);
			cycleButton.active = false;
		}

		super.createFooter();
	}
}
