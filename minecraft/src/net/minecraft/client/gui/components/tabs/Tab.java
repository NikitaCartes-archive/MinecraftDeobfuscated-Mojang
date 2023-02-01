package net.minecraft.client.gui.components.tabs;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public interface Tab {
	Component getTabTitle();

	void visitChildren(Consumer<AbstractWidget> consumer);

	void doLayout(ScreenRectangle screenRectangle);

	default void tick() {
	}
}
