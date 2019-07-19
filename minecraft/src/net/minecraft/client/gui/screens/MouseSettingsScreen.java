package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Arrays;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class MouseSettingsScreen extends Screen {
	private final Screen lastScreen;
	private OptionsList list;
	private static final Option[] OPTIONS = new Option[]{
		Option.SENSITIVITY, Option.INVERT_MOUSE, Option.MOUSE_WHEEL_SENSITIVITY, Option.DISCRETE_MOUSE_SCROLL, Option.TOUCHSCREEN
	};

	public MouseSettingsScreen(Screen screen) {
		super(new TranslatableComponent("options.mouse_settings.title"));
		this.lastScreen = screen;
	}

	@Override
	protected void init() {
		this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		if (InputConstants.isRawMouseInputSupported()) {
			this.list.addSmall((Option[])Stream.concat(Arrays.stream(OPTIONS), Stream.of(Option.RAW_MOUSE_INPUT)).toArray(Option[]::new));
		} else {
			this.list.addSmall(OPTIONS);
		}

		this.children.add(this.list);
		this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, I18n.get("gui.done"), button -> {
			this.minecraft.options.save();
			this.minecraft.setScreen(this.lastScreen);
		}));
	}

	@Override
	public void removed() {
		this.minecraft.options.save();
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.list.render(i, j, f);
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 5, 16777215);
		super.render(i, j, f);
	}
}
