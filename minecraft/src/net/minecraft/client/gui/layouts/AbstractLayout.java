package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractLayout implements LayoutElement {
	private int x;
	private int y;
	protected int width;
	protected int height;

	public AbstractLayout(int i, int j, int k, int l) {
		this.x = i;
		this.y = j;
		this.width = k;
		this.height = l;
	}

	protected abstract void visitChildren(Consumer<LayoutElement> consumer);

	public void arrangeElements() {
		this.visitChildren(layoutElement -> {
			if (layoutElement instanceof AbstractLayout abstractLayout) {
				abstractLayout.arrangeElements();
			}
		});
	}

	@Override
	public void visitWidgets(Consumer<AbstractWidget> consumer) {
		this.visitChildren(layoutElement -> layoutElement.visitWidgets(consumer));
	}

	@Override
	public void setX(int i) {
		this.visitChildren(layoutElement -> {
			int j = layoutElement.getX() + (i - this.getX());
			layoutElement.setX(j);
		});
		this.x = i;
	}

	@Override
	public void setY(int i) {
		this.visitChildren(layoutElement -> {
			int j = layoutElement.getY() + (i - this.getY());
			layoutElement.setY(j);
		});
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

	@Environment(EnvType.CLIENT)
	protected abstract static class AbstractChildWrapper {
		public final LayoutElement child;
		public final LayoutSettings.LayoutSettingsImpl layoutSettings;

		protected AbstractChildWrapper(LayoutElement layoutElement, LayoutSettings layoutSettings) {
			this.child = layoutElement;
			this.layoutSettings = layoutSettings.getExposed();
		}

		public int getHeight() {
			return this.child.getHeight() + this.layoutSettings.paddingTop + this.layoutSettings.paddingBottom;
		}

		public int getWidth() {
			return this.child.getWidth() + this.layoutSettings.paddingLeft + this.layoutSettings.paddingRight;
		}

		public void setX(int i, int j) {
			float f = (float)this.layoutSettings.paddingLeft;
			float g = (float)(j - this.child.getWidth() - this.layoutSettings.paddingRight);
			int k = (int)Mth.lerp(this.layoutSettings.xAlignment, f, g);
			this.child.setX(k + i);
		}

		public void setY(int i, int j) {
			float f = (float)this.layoutSettings.paddingTop;
			float g = (float)(j - this.child.getHeight() - this.layoutSettings.paddingBottom);
			int k = (int)Mth.lerp(this.layoutSettings.yAlignment, f, g);
			this.child.setY(k + i);
		}
	}
}
