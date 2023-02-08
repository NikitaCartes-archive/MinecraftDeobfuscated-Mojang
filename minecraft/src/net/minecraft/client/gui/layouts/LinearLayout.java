package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LinearLayout extends AbstractLayout {
	private final LinearLayout.Orientation orientation;
	private final List<LinearLayout.ChildContainer> children = new ArrayList();
	private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();

	public LinearLayout(int i, int j, LinearLayout.Orientation orientation) {
		this(0, 0, i, j, orientation);
	}

	public LinearLayout(int i, int j, int k, int l, LinearLayout.Orientation orientation) {
		super(i, j, k, l);
		this.orientation = orientation;
	}

	@Override
	public void arrangeElements() {
		super.arrangeElements();
		if (!this.children.isEmpty()) {
			int i = 0;
			int j = this.orientation.getSecondaryLength(this);

			for (LinearLayout.ChildContainer childContainer : this.children) {
				i += this.orientation.getPrimaryLength(childContainer);
				j = Math.max(j, this.orientation.getSecondaryLength(childContainer));
			}

			int k = this.orientation.getPrimaryLength(this) - i;
			int l = this.orientation.getPrimaryPosition(this);
			Iterator<LinearLayout.ChildContainer> iterator = this.children.iterator();
			LinearLayout.ChildContainer childContainer2 = (LinearLayout.ChildContainer)iterator.next();
			this.orientation.setPrimaryPosition(childContainer2, l);
			l += this.orientation.getPrimaryLength(childContainer2);
			if (this.children.size() >= 2) {
				Divisor divisor = new Divisor(k, this.children.size() - 1);

				while (divisor.hasNext()) {
					l += divisor.nextInt();
					LinearLayout.ChildContainer childContainer3 = (LinearLayout.ChildContainer)iterator.next();
					this.orientation.setPrimaryPosition(childContainer3, l);
					l += this.orientation.getPrimaryLength(childContainer3);
				}
			}

			int m = this.orientation.getSecondaryPosition(this);

			for (LinearLayout.ChildContainer childContainer4 : this.children) {
				this.orientation.setSecondaryPosition(childContainer4, m, j);
			}

			switch (this.orientation) {
				case HORIZONTAL:
					this.height = j;
					break;
				case VERTICAL:
					this.width = j;
			}
		}
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> consumer) {
		this.children.forEach(childContainer -> consumer.accept(childContainer.child));
	}

	public LayoutSettings newChildLayoutSettings() {
		return this.defaultChildLayoutSettings.copy();
	}

	public LayoutSettings defaultChildLayoutSetting() {
		return this.defaultChildLayoutSettings;
	}

	public <T extends LayoutElement> T addChild(T layoutElement) {
		return this.addChild(layoutElement, this.newChildLayoutSettings());
	}

	public <T extends LayoutElement> T addChild(T layoutElement, LayoutSettings layoutSettings) {
		this.children.add(new LinearLayout.ChildContainer(layoutElement, layoutSettings));
		return layoutElement;
	}

	@Environment(EnvType.CLIENT)
	static class ChildContainer extends AbstractLayout.AbstractChildWrapper {
		protected ChildContainer(LayoutElement layoutElement, LayoutSettings layoutSettings) {
			super(layoutElement, layoutSettings);
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Orientation {
		HORIZONTAL,
		VERTICAL;

		int getPrimaryLength(LayoutElement layoutElement) {
			return switch (this) {
				case HORIZONTAL -> layoutElement.getWidth();
				case VERTICAL -> layoutElement.getHeight();
			};
		}

		int getPrimaryLength(LinearLayout.ChildContainer childContainer) {
			return switch (this) {
				case HORIZONTAL -> childContainer.getWidth();
				case VERTICAL -> childContainer.getHeight();
			};
		}

		int getSecondaryLength(LayoutElement layoutElement) {
			return switch (this) {
				case HORIZONTAL -> layoutElement.getHeight();
				case VERTICAL -> layoutElement.getWidth();
			};
		}

		int getSecondaryLength(LinearLayout.ChildContainer childContainer) {
			return switch (this) {
				case HORIZONTAL -> childContainer.getHeight();
				case VERTICAL -> childContainer.getWidth();
			};
		}

		void setPrimaryPosition(LinearLayout.ChildContainer childContainer, int i) {
			switch (this) {
				case HORIZONTAL:
					childContainer.setX(i, childContainer.getWidth());
					break;
				case VERTICAL:
					childContainer.setY(i, childContainer.getHeight());
			}
		}

		void setSecondaryPosition(LinearLayout.ChildContainer childContainer, int i, int j) {
			switch (this) {
				case HORIZONTAL:
					childContainer.setY(i, j);
					break;
				case VERTICAL:
					childContainer.setX(i, j);
			}
		}

		int getPrimaryPosition(LayoutElement layoutElement) {
			return switch (this) {
				case HORIZONTAL -> layoutElement.getX();
				case VERTICAL -> layoutElement.getY();
			};
		}

		int getSecondaryPosition(LayoutElement layoutElement) {
			return switch (this) {
				case HORIZONTAL -> layoutElement.getY();
				case VERTICAL -> layoutElement.getX();
			};
		}
	}
}
