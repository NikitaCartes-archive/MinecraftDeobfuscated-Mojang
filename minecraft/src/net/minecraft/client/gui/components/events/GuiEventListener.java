package net.minecraft.client.gui.components.events;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;

@Environment(EnvType.CLIENT)
public interface GuiEventListener extends TabOrderedElement {
	long DOUBLE_CLICK_THRESHOLD_MS = 250L;

	default void mouseMoved(double d, double e) {
	}

	default boolean mouseClicked(double d, double e, int i) {
		return false;
	}

	default boolean mouseReleased(double d, double e, int i) {
		return false;
	}

	default boolean mouseDragged(double d, double e, int i, double f, double g) {
		return false;
	}

	default boolean mouseScrolled(double d, double e, double f) {
		return false;
	}

	default boolean keyPressed(int i, int j, int k) {
		return false;
	}

	default boolean keyReleased(int i, int j, int k) {
		return false;
	}

	default boolean charTyped(char c, int i) {
		return false;
	}

	@Nullable
	default ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
		return null;
	}

	default boolean isMouseOver(double d, double e) {
		return false;
	}

	void setFocused(boolean bl);

	boolean isFocused();

	@Nullable
	default ComponentPath getCurrentFocusPath() {
		return this.isFocused() ? ComponentPath.leaf(this) : null;
	}

	default ScreenRectangle getRectangle() {
		return ScreenRectangle.empty();
	}
}
