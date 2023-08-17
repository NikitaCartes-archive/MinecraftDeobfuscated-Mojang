package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class CommonLayouts {
	private static final int LABEL_SPACING = 4;

	private CommonLayouts() {
	}

	public static Layout labeledElement(Font font, LayoutElement layoutElement, Component component) {
		return labeledElement(font, layoutElement, component, layoutSettings -> {
		});
	}

	public static Layout labeledElement(Font font, LayoutElement layoutElement, Component component, Consumer<LayoutSettings> consumer) {
		LinearLayout linearLayout = LinearLayout.vertical().spacing(4);
		linearLayout.addChild(new StringWidget(component, font));
		linearLayout.addChild(layoutElement, consumer);
		return linearLayout;
	}
}
