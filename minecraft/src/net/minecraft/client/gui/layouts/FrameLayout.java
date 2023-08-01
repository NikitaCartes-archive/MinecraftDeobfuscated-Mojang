package net.minecraft.client.gui.layouts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class FrameLayout extends AbstractLayout {
	private final List<FrameLayout.ChildContainer> children = new ArrayList();
	private int minWidth;
	private int minHeight;
	private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults().align(0.5F, 0.5F);

	public FrameLayout() {
		this(0, 0, 0, 0);
	}

	public FrameLayout(int i, int j) {
		this(0, 0, i, j);
	}

	public FrameLayout(int i, int j, int k, int l) {
		super(i, j, k, l);
		this.setMinDimensions(k, l);
	}

	public FrameLayout setMinDimensions(int i, int j) {
		return this.setMinWidth(i).setMinHeight(j);
	}

	public FrameLayout setMinHeight(int i) {
		this.minHeight = i;
		return this;
	}

	public FrameLayout setMinWidth(int i) {
		this.minWidth = i;
		return this;
	}

	public LayoutSettings newChildLayoutSettings() {
		return this.defaultChildLayoutSettings.copy();
	}

	public LayoutSettings defaultChildLayoutSetting() {
		return this.defaultChildLayoutSettings;
	}

	@Override
	public void arrangeElements() {
		super.arrangeElements();
		int i = this.minWidth;
		int j = this.minHeight;

		for (FrameLayout.ChildContainer childContainer : this.children) {
			i = Math.max(i, childContainer.getWidth());
			j = Math.max(j, childContainer.getHeight());
		}

		for (FrameLayout.ChildContainer childContainer : this.children) {
			childContainer.setX(this.getX(), i);
			childContainer.setY(this.getY(), j);
		}

		this.width = i;
		this.height = j;
	}

	public <T extends LayoutElement> T addChild(T layoutElement) {
		return this.addChild(layoutElement, this.newChildLayoutSettings());
	}

	public <T extends LayoutElement> T addChild(T layoutElement, LayoutSettings layoutSettings) {
		this.children.add(new FrameLayout.ChildContainer(layoutElement, layoutSettings));
		return layoutElement;
	}

	public <T extends LayoutElement> T addChild(T layoutElement, Consumer<LayoutSettings> consumer) {
		return this.addChild(layoutElement, Util.make(this.newChildLayoutSettings(), consumer));
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> consumer) {
		this.children.forEach(childContainer -> consumer.accept(childContainer.child));
	}

	public static void centerInRectangle(LayoutElement layoutElement, int i, int j, int k, int l) {
		alignInRectangle(layoutElement, i, j, k, l, 0.5F, 0.5F);
	}

	public static void centerInRectangle(LayoutElement layoutElement, ScreenRectangle screenRectangle) {
		centerInRectangle(layoutElement, screenRectangle.position().x(), screenRectangle.position().y(), screenRectangle.width(), screenRectangle.height());
	}

	public static void alignInRectangle(LayoutElement layoutElement, ScreenRectangle screenRectangle, float f, float g) {
		alignInRectangle(layoutElement, screenRectangle.left(), screenRectangle.top(), screenRectangle.width(), screenRectangle.height(), f, g);
	}

	public static void alignInRectangle(LayoutElement layoutElement, int i, int j, int k, int l, float f, float g) {
		alignInDimension(i, k, layoutElement.getWidth(), layoutElement::setX, f);
		alignInDimension(j, l, layoutElement.getHeight(), layoutElement::setY, g);
	}

	public static void alignInDimension(int i, int j, int k, Consumer<Integer> consumer, float f) {
		int l = (int)Mth.lerp(f, 0.0F, (float)(j - k));
		consumer.accept(i + l);
	}

	@Environment(EnvType.CLIENT)
	static class ChildContainer extends AbstractLayout.AbstractChildWrapper {
		protected ChildContainer(LayoutElement layoutElement, LayoutSettings layoutSettings) {
			super(layoutElement, layoutSettings);
		}
	}
}
