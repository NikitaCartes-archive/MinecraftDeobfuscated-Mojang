package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;

@Environment(EnvType.CLIENT)
public interface LayoutElement {
	void setX(int i);

	void setY(int i);

	int getX();

	int getY();

	int getWidth();

	int getHeight();

	default ScreenRectangle getRectangle() {
		return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	default void setPosition(int i, int j) {
		this.setX(i);
		this.setY(j);
	}

	void visitWidgets(Consumer<AbstractWidget> consumer);
}
