package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractContainerWidget extends AbstractWidget implements ContainerEventHandler {
	@Nullable
	private GuiEventListener focused;
	private boolean dragging;

	public AbstractContainerWidget(int i, int j, int k, int l, Component component) {
		super(i, j, k, l, component);
	}

	@Override
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		for (AbstractWidget abstractWidget : this.getContainedChildren()) {
			abstractWidget.render(poseStack, i, j, f);
		}
	}

	@Override
	public boolean isMouseOver(double d, double e) {
		for (AbstractWidget abstractWidget : this.getContainedChildren()) {
			if (abstractWidget.isMouseOver(d, e)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void mouseMoved(double d, double e) {
		this.getContainedChildren().forEach(abstractWidget -> abstractWidget.mouseMoved(d, e));
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return this.getContainedChildren();
	}

	protected abstract List<? extends AbstractWidget> getContainedChildren();

	@Override
	public boolean isDragging() {
		return this.dragging;
	}

	@Override
	public void setDragging(boolean bl) {
		this.dragging = bl;
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		boolean bl = false;

		for (AbstractWidget abstractWidget : this.getContainedChildren()) {
			if (abstractWidget.isMouseOver(d, e) && abstractWidget.mouseScrolled(d, e, f)) {
				bl = true;
			}
		}

		return bl || super.mouseScrolled(d, e, f);
	}

	@Override
	public boolean changeFocus(boolean bl) {
		return ContainerEventHandler.super.changeFocus(bl);
	}

	@Nullable
	protected GuiEventListener getHovered() {
		for (AbstractWidget abstractWidget : this.getContainedChildren()) {
			if (abstractWidget.isHovered) {
				return abstractWidget;
			}
		}

		return null;
	}

	@Nullable
	@Override
	public GuiEventListener getFocused() {
		return this.focused;
	}

	@Override
	public void setFocused(@Nullable GuiEventListener guiEventListener) {
		this.focused = guiEventListener;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		GuiEventListener guiEventListener = this.getHovered();
		if (guiEventListener != null) {
			if (guiEventListener instanceof NarrationSupplier narrationSupplier) {
				narrationSupplier.updateNarration(narrationElementOutput.nest());
			}
		} else {
			GuiEventListener guiEventListener2 = this.getFocused();
			if (guiEventListener2 != null && guiEventListener2 instanceof NarrationSupplier narrationSupplier2) {
				narrationSupplier2.updateNarration(narrationElementOutput.nest());
			}
		}
	}

	@Override
	public NarratableEntry.NarrationPriority narrationPriority() {
		if (this.isHovered || this.getHovered() != null) {
			return NarratableEntry.NarrationPriority.HOVERED;
		} else {
			return this.focused != null ? NarratableEntry.NarrationPriority.FOCUSED : super.narrationPriority();
		}
	}

	@Override
	public void setX(int i) {
		for (AbstractWidget abstractWidget : this.getContainedChildren()) {
			int j = abstractWidget.getX() + (i - this.getX());
			abstractWidget.setX(j);
		}

		super.setX(i);
	}

	@Override
	public void setY(int i) {
		for (AbstractWidget abstractWidget : this.getContainedChildren()) {
			int j = abstractWidget.getY() + (i - this.getY());
			abstractWidget.setY(j);
		}

		super.setY(i);
	}

	@Override
	public Optional<GuiEventListener> getChildAt(double d, double e) {
		return ContainerEventHandler.super.getChildAt(d, e);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		return ContainerEventHandler.super.mouseClicked(d, e, i);
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		return ContainerEventHandler.super.mouseReleased(d, e, i);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		return ContainerEventHandler.super.mouseDragged(d, e, i, f, g);
	}

	@Environment(EnvType.CLIENT)
	protected abstract static class AbstractChildWrapper {
		public final AbstractWidget child;
		public final LayoutSettings.LayoutSettingsImpl layoutSettings;

		protected AbstractChildWrapper(AbstractWidget abstractWidget, LayoutSettings layoutSettings) {
			this.child = abstractWidget;
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
