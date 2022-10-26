package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class FrameWidget extends AbstractContainerWidget {
	private final List<FrameWidget.ChildContainer> children = new ArrayList();
	private final List<AbstractWidget> containedChildrenView = Collections.unmodifiableList(Lists.transform(this.children, childContainer -> childContainer.child));
	private int minWidth;
	private int minHeight;
	private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults().align(0.5F, 0.5F);

	public static FrameWidget withMinDimensions(int i, int j) {
		return new FrameWidget(0, 0, 0, 0).setMinDimensions(i, j);
	}

	public FrameWidget() {
		this(0, 0, 0, 0);
	}

	public FrameWidget(int i, int j, int k, int l) {
		super(i, j, k, l, Component.empty());
	}

	public FrameWidget setMinDimensions(int i, int j) {
		return this.setMinWidth(i).setMinHeight(j);
	}

	public FrameWidget setMinHeight(int i) {
		this.minHeight = i;
		return this;
	}

	public FrameWidget setMinWidth(int i) {
		this.minWidth = i;
		return this;
	}

	public LayoutSettings newChildLayoutSettings() {
		return this.defaultChildLayoutSettings.copy();
	}

	public LayoutSettings defaultChildLayoutSetting() {
		return this.defaultChildLayoutSettings;
	}

	public void pack() {
		int i = this.minWidth;
		int j = this.minHeight;

		for (FrameWidget.ChildContainer childContainer : this.children) {
			i = Math.max(i, childContainer.getWidth());
			j = Math.max(j, childContainer.getHeight());
		}

		for (FrameWidget.ChildContainer childContainer : this.children) {
			childContainer.setX(this.getX(), i);
			childContainer.setY(this.getY(), j);
		}

		this.width = i;
		this.height = j;
	}

	public <T extends AbstractWidget> T addChild(T abstractWidget) {
		return this.addChild(abstractWidget, this.newChildLayoutSettings());
	}

	public <T extends AbstractWidget> T addChild(T abstractWidget, LayoutSettings layoutSettings) {
		this.children.add(new FrameWidget.ChildContainer(abstractWidget, layoutSettings));
		return abstractWidget;
	}

	@Override
	protected List<AbstractWidget> getContainedChildren() {
		return this.containedChildrenView;
	}

	public static void centerInRectangle(AbstractWidget abstractWidget, int i, int j, int k, int l) {
		alignInRectangle(abstractWidget, i, j, k, l, 0.5F, 0.5F);
	}

	public static void alignInRectangle(AbstractWidget abstractWidget, int i, int j, int k, int l, float f, float g) {
		alignInDimension(i, k, abstractWidget.getWidth(), abstractWidget::setX, f);
		alignInDimension(j, l, abstractWidget.getHeight(), abstractWidget::setY, g);
	}

	public static void alignInDimension(int i, int j, int k, Consumer<Integer> consumer, float f) {
		int l = (int)Mth.lerp(f, 0.0F, (float)(j - k));
		consumer.accept(i + l);
	}

	@Environment(EnvType.CLIENT)
	static class ChildContainer extends AbstractContainerWidget.AbstractChildWrapper {
		protected ChildContainer(AbstractWidget abstractWidget, LayoutSettings layoutSettings) {
			super(abstractWidget, layoutSettings);
		}
	}
}
