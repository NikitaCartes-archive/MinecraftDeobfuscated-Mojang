package net.minecraft.client.gui.components.tabs;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class GridLayoutTab implements Tab {
	private final Component title;
	protected final GridLayout layout = new GridLayout();

	public GridLayoutTab(Component component) {
		this.title = component;
	}

	@Override
	public Component getTabTitle() {
		return this.title;
	}

	@Override
	public void visitChildren(Consumer<AbstractWidget> consumer) {
		this.layout.visitWidgets(consumer);
	}

	@Override
	public void doLayout(ScreenRectangle screenRectangle) {
		this.layout.arrangeElements();
		FrameLayout.alignInRectangle(this.layout, screenRectangle, 0.5F, 0.16666667F);
	}
}
