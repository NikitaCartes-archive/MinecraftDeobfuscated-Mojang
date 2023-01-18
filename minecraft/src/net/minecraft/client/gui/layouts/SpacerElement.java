package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;

@Environment(EnvType.CLIENT)
public class SpacerElement implements LayoutElement {
	private int x;
	private int y;
	private final int width;
	private final int height;

	public SpacerElement(int i, int j) {
		this(0, 0, i, j);
	}

	public SpacerElement(int i, int j, int k, int l) {
		this.x = i;
		this.y = j;
		this.width = k;
		this.height = l;
	}

	public static SpacerElement width(int i) {
		return new SpacerElement(i, 0);
	}

	public static SpacerElement height(int i) {
		return new SpacerElement(0, i);
	}

	@Override
	public void setX(int i) {
		this.x = i;
	}

	@Override
	public void setY(int i) {
		this.y = i;
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public void visitWidgets(Consumer<AbstractWidget> consumer) {
	}
}
