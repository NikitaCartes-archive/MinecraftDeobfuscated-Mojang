package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class LinearLayoutWidget extends AbstractContainerWidget {
	private final LinearLayoutWidget.Orientation orientation;
	private final List<LinearLayoutWidget.ChildContainer> children = new ArrayList();
	private final List<AbstractWidget> containedChildrenView = Collections.unmodifiableList(Lists.transform(this.children, childContainer -> childContainer.child));
	private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();

	public LinearLayoutWidget(int i, int j, LinearLayoutWidget.Orientation orientation) {
		this(0, 0, i, j, orientation);
	}

	public LinearLayoutWidget(int i, int j, int k, int l, LinearLayoutWidget.Orientation orientation) {
		super(i, j, k, l, Component.empty());
		this.orientation = orientation;
	}

	public void pack() {
		if (!this.children.isEmpty()) {
			int i = 0;
			int j = this.orientation.getSecondaryLength(this);

			for (LinearLayoutWidget.ChildContainer childContainer : this.children) {
				i += this.orientation.getPrimaryLength(childContainer);
				j = Math.max(j, this.orientation.getSecondaryLength(childContainer));
			}

			int k = this.orientation.getPrimaryLength(this) - i;
			int l = this.orientation.getPrimaryPosition(this);
			Iterator<LinearLayoutWidget.ChildContainer> iterator = this.children.iterator();
			LinearLayoutWidget.ChildContainer childContainer2 = (LinearLayoutWidget.ChildContainer)iterator.next();
			this.orientation.setPrimaryPosition(childContainer2, l);
			l += this.orientation.getPrimaryLength(childContainer2);
			if (this.children.size() >= 2) {
				Divisor divisor = new Divisor(k, this.children.size() - 1);

				while (divisor.hasNext()) {
					l += divisor.nextInt();
					LinearLayoutWidget.ChildContainer childContainer3 = (LinearLayoutWidget.ChildContainer)iterator.next();
					this.orientation.setPrimaryPosition(childContainer3, l);
					l += this.orientation.getPrimaryLength(childContainer3);
				}
			}

			int m = this.orientation.getSecondaryPosition(this);

			for (LinearLayoutWidget.ChildContainer childContainer4 : this.children) {
				this.orientation.setSecondaryPosition(childContainer4, m, j);
			}

			this.orientation.setSecondaryLength(this, j);
		}
	}

	@Override
	protected List<? extends AbstractWidget> getContainedChildren() {
		return this.containedChildrenView;
	}

	public LayoutSettings newChildLayoutSettings() {
		return this.defaultChildLayoutSettings.copy();
	}

	public LayoutSettings defaultChildLayoutSetting() {
		return this.defaultChildLayoutSettings;
	}

	public <T extends AbstractWidget> T addChild(T abstractWidget) {
		return this.addChild(abstractWidget, this.newChildLayoutSettings());
	}

	public <T extends AbstractWidget> T addChild(T abstractWidget, LayoutSettings layoutSettings) {
		this.children.add(new LinearLayoutWidget.ChildContainer(abstractWidget, layoutSettings));
		return abstractWidget;
	}

	@Environment(EnvType.CLIENT)
	static class ChildContainer extends AbstractContainerWidget.AbstractChildWrapper {
		protected ChildContainer(AbstractWidget abstractWidget, LayoutSettings layoutSettings) {
			super(abstractWidget, layoutSettings);
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Orientation {
		HORIZONTAL,
		VERTICAL;

		int getPrimaryLength(AbstractWidget abstractWidget) {
			return switch (this) {
				case HORIZONTAL -> abstractWidget.getWidth();
				case VERTICAL -> abstractWidget.getHeight();
			};
		}

		int getPrimaryLength(LinearLayoutWidget.ChildContainer childContainer) {
			return switch (this) {
				case HORIZONTAL -> childContainer.getWidth();
				case VERTICAL -> childContainer.getHeight();
			};
		}

		int getSecondaryLength(AbstractWidget abstractWidget) {
			return switch (this) {
				case HORIZONTAL -> abstractWidget.getHeight();
				case VERTICAL -> abstractWidget.getWidth();
			};
		}

		int getSecondaryLength(LinearLayoutWidget.ChildContainer childContainer) {
			return switch (this) {
				case HORIZONTAL -> childContainer.getHeight();
				case VERTICAL -> childContainer.getWidth();
			};
		}

		void setPrimaryPosition(LinearLayoutWidget.ChildContainer childContainer, int i) {
			switch (this) {
				case HORIZONTAL:
					childContainer.setX(i, childContainer.getWidth());
					break;
				case VERTICAL:
					childContainer.setY(i, childContainer.getHeight());
			}
		}

		void setSecondaryPosition(LinearLayoutWidget.ChildContainer childContainer, int i, int j) {
			switch (this) {
				case HORIZONTAL:
					childContainer.setY(i, j);
					break;
				case VERTICAL:
					childContainer.setX(i, j);
			}
		}

		int getPrimaryPosition(AbstractWidget abstractWidget) {
			return switch (this) {
				case HORIZONTAL -> abstractWidget.getX();
				case VERTICAL -> abstractWidget.getY();
			};
		}

		int getSecondaryPosition(AbstractWidget abstractWidget) {
			return switch (this) {
				case HORIZONTAL -> abstractWidget.getY();
				case VERTICAL -> abstractWidget.getX();
			};
		}

		void setSecondaryLength(AbstractWidget abstractWidget, int i) {
			switch (this) {
				case HORIZONTAL:
					abstractWidget.height = i;
					break;
				case VERTICAL:
					abstractWidget.width = i;
			}
		}
	}
}
