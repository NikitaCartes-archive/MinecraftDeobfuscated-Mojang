package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class AlertScreen extends Screen {
	private final Runnable callback;
	protected final Component text;
	private final List<String> lines = Lists.<String>newArrayList();
	protected final String okButton;
	private int delayTicker;

	public AlertScreen(Runnable runnable, Component component, Component component2) {
		this(runnable, component, component2, "gui.back");
	}

	public AlertScreen(Runnable runnable, Component component, Component component2, String string) {
		super(component);
		this.callback = runnable;
		this.text = component2;
		this.okButton = I18n.get(string);
	}

	@Override
	protected void init() {
		super.init();
		this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, this.okButton, button -> this.callback.run()));
		this.lines.clear();
		this.lines.addAll(this.font.split(this.text.getColoredString(), this.width - 50));
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 70, 16777215);
		int k = 90;

		for (String string : this.lines) {
			this.drawCenteredString(this.font, string, this.width / 2, k, 16777215);
			k += 9;
		}

		super.render(i, j, f);
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
