package net.minecraft.client.gui.components.events;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ContainerEventHandler extends GuiEventListener {
	List<? extends GuiEventListener> children();

	default Optional<GuiEventListener> getChildAt(double d, double e) {
		for (GuiEventListener guiEventListener : this.children()) {
			if (guiEventListener.isMouseOver(d, e)) {
				return Optional.of(guiEventListener);
			}
		}

		return Optional.empty();
	}

	@Override
	default boolean mouseClicked(double d, double e, int i) {
		for (GuiEventListener guiEventListener : this.children()) {
			if (guiEventListener.mouseClicked(d, e, i)) {
				this.setFocused(guiEventListener);
				if (i == 0) {
					this.setDragging(true);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	default boolean mouseReleased(double d, double e, int i) {
		this.setDragging(false);
		return this.getChildAt(d, e).filter(guiEventListener -> guiEventListener.mouseReleased(d, e, i)).isPresent();
	}

	@Override
	default boolean mouseDragged(double d, double e, int i, double f, double g) {
		return this.getFocused() != null && this.isDragging() && i == 0 ? this.getFocused().mouseDragged(d, e, i, f, g) : false;
	}

	boolean isDragging();

	void setDragging(boolean bl);

	@Override
	default boolean mouseScrolled(double d, double e, double f) {
		return this.getChildAt(d, e).filter(guiEventListener -> guiEventListener.mouseScrolled(d, e, f)).isPresent();
	}

	@Override
	default boolean keyPressed(int i, int j, int k) {
		return this.getFocused() != null && this.getFocused().keyPressed(i, j, k);
	}

	@Override
	default boolean keyReleased(int i, int j, int k) {
		return this.getFocused() != null && this.getFocused().keyReleased(i, j, k);
	}

	@Override
	default boolean charTyped(char c, int i) {
		return this.getFocused() != null && this.getFocused().charTyped(c, i);
	}

	@Nullable
	GuiEventListener getFocused();

	void setFocused(@Nullable GuiEventListener guiEventListener);

	default void setInitialFocus(@Nullable GuiEventListener guiEventListener) {
		this.setFocused(guiEventListener);
	}

	default void magicalSpecialHackyFocus(@Nullable GuiEventListener guiEventListener) {
		this.setFocused(guiEventListener);
	}

	@Override
	default boolean changeFocus(boolean bl) {
		GuiEventListener guiEventListener = this.getFocused();
		boolean bl2 = guiEventListener != null;
		if (bl2 && guiEventListener.changeFocus(bl)) {
			return true;
		} else {
			List<? extends GuiEventListener> list = this.children();
			int i = list.indexOf(guiEventListener);
			int j;
			if (bl2 && i >= 0) {
				j = i + (bl ? 1 : 0);
			} else if (bl) {
				j = 0;
			} else {
				j = list.size();
			}

			ListIterator<? extends GuiEventListener> listIterator = list.listIterator(j);
			BooleanSupplier booleanSupplier = bl ? listIterator::hasNext : listIterator::hasPrevious;
			Supplier<? extends GuiEventListener> supplier = bl ? listIterator::next : listIterator::previous;

			while (booleanSupplier.getAsBoolean()) {
				GuiEventListener guiEventListener2 = (GuiEventListener)supplier.get();
				if (guiEventListener2.changeFocus(bl)) {
					this.setFocused(guiEventListener2);
					return true;
				}
			}

			this.setFocused(null);
			return false;
		}
	}
}
