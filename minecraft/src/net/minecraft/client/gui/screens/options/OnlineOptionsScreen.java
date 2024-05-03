package net.minecraft.client.gui.screens.options;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;

@Environment(EnvType.CLIENT)
public class OnlineOptionsScreen extends OptionsSubScreen {
	private static final Component TITLE = Component.translatable("options.online.title");
	@Nullable
	private OptionInstance<Unit> difficultyDisplay;

	public OnlineOptionsScreen(Screen screen, Options options) {
		super(screen, options, TITLE);
	}

	@Override
	protected void init() {
		super.init();
		if (this.difficultyDisplay != null) {
			AbstractWidget abstractWidget = this.list.findOption(this.difficultyDisplay);
			if (abstractWidget != null) {
				abstractWidget.active = false;
			}
		}
	}

	private OptionInstance<?>[] options(Options options, Minecraft minecraft) {
		List<OptionInstance<?>> list = new ArrayList();
		list.add(options.realmsNotifications());
		list.add(options.allowServerListing());
		OptionInstance<Unit> optionInstance = Optionull.map(
			minecraft.level,
			clientLevel -> {
				Difficulty difficulty = clientLevel.getDifficulty();
				return new OptionInstance<>(
					"options.difficulty.online",
					OptionInstance.noTooltip(),
					(component, unit) -> difficulty.getDisplayName(),
					new OptionInstance.Enum<>(List.of(Unit.INSTANCE), Codec.EMPTY.codec()),
					Unit.INSTANCE,
					unit -> {
					}
				);
			}
		);
		if (optionInstance != null) {
			this.difficultyDisplay = optionInstance;
			list.add(optionInstance);
		}

		return (OptionInstance<?>[])list.toArray(new OptionInstance[0]);
	}

	@Override
	protected void addOptions() {
		this.list.addSmall(this.options(this.options, this.minecraft));
	}
}
