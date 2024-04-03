package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;

@Environment(EnvType.CLIENT)
public class LinearLayout implements Layout {
	private final GridLayout wrapped;
	private final LinearLayout.Orientation orientation;
	private int nextChildIndex = 0;

	private LinearLayout(LinearLayout.Orientation orientation) {
		this(0, 0, orientation);
	}

	public LinearLayout(int i, int j, LinearLayout.Orientation orientation) {
		this.wrapped = new GridLayout(i, j);
		this.orientation = orientation;
	}

	public LinearLayout spacing(int i) {
		this.orientation.setSpacing(this.wrapped, i);
		return this;
	}

	public LayoutSettings newCellSettings() {
		return this.wrapped.newCellSettings();
	}

	public LayoutSettings defaultCellSetting() {
		return this.wrapped.defaultCellSetting();
	}

	public <T extends LayoutElement> T addChild(T layoutElement, LayoutSettings layoutSettings) {
		return this.orientation.addChild(this.wrapped, layoutElement, this.nextChildIndex++, layoutSettings);
	}

	public <T extends LayoutElement> T addChild(T layoutElement) {
		return this.addChild(layoutElement, this.newCellSettings());
	}

	public <T extends LayoutElement> T addChild(T layoutElement, Consumer<LayoutSettings> consumer) {
		return this.orientation.addChild(this.wrapped, layoutElement, this.nextChildIndex++, Util.make(this.newCellSettings(), consumer));
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> consumer) {
		this.wrapped.visitChildren(consumer);
	}

	@Override
	public void arrangeElements() {
		this.wrapped.arrangeElements();
	}

	@Override
	public int getWidth() {
		return this.wrapped.getWidth();
	}

	@Override
	public int getHeight() {
		return this.wrapped.getHeight();
	}

	@Override
	public void setX(int i) {
		this.wrapped.setX(i);
	}

	@Override
	public void setY(int i) {
		this.wrapped.setY(i);
	}

	@Override
	public int getX() {
		return this.wrapped.getX();
	}

	@Override
	public int getY() {
		return this.wrapped.getY();
	}

	public static LinearLayout vertical() {
		return new LinearLayout(LinearLayout.Orientation.VERTICAL);
	}

	public static LinearLayout horizontal() {
		return new LinearLayout(LinearLayout.Orientation.HORIZONTAL);
	}

	@Environment(EnvType.CLIENT)
	public static enum Orientation {
		HORIZONTAL,
		VERTICAL;

		void setSpacing(GridLayout gridLayout, int i) {
			switch (this) {
				case HORIZONTAL:
					gridLayout.columnSpacing(i);
					break;
				case VERTICAL:
					gridLayout.rowSpacing(i);
			}
		}

		public <T extends LayoutElement> T addChild(GridLayout gridLayout, T layoutElement, int i, LayoutSettings layoutSettings) {
			return (T)(switch (this) {
				case HORIZONTAL -> (LayoutElement)gridLayout.addChild(layoutElement, 0, i, layoutSettings);
				case VERTICAL -> (LayoutElement)gridLayout.addChild(layoutElement, i, 0, layoutSettings);
			});
		}
	}
}
