package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Arrays;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class MouseSettingsScreen extends OptionsSubScreen {
	private static final Component TITLE = Component.translatable("options.mouse_settings.title");
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private OptionsList list;

	private static OptionInstance<?>[] options(Options options) {
		return new OptionInstance[]{
			options.sensitivity(), options.invertYMouse(), options.mouseWheelSensitivity(), options.discreteMouseScroll(), options.touchscreen()
		};
	}

	public MouseSettingsScreen(Screen screen, Options options) {
		super(screen, options, TITLE);
	}

	@Override
	protected void init() {
		this.list = this.addRenderableWidget(new OptionsList(this.minecraft, this.width, this.height, this));
		if (InputConstants.isRawMouseInputSupported()) {
			this.list
				.addSmall((OptionInstance<?>[])Stream.concat(Arrays.stream(options(this.options)), Stream.of(this.options.rawMouseInput())).toArray(OptionInstance[]::new));
		} else {
			this.list.addSmall(options(this.options));
		}

		super.init();
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		this.list.updateSize(this.width, this.layout);
	}
}
