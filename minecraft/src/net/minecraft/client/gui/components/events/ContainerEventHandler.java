package net.minecraft.client.gui.components.events;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Vector2i;

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
	default boolean mouseScrolled(double d, double e, double f, double g) {
		return this.getChildAt(d, e).filter(guiEventListener -> guiEventListener.mouseScrolled(d, e, f, g)).isPresent();
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

	@Override
	default void setFocused(boolean bl) {
	}

	@Override
	default boolean isFocused() {
		return this.getFocused() != null;
	}

	@Nullable
	@Override
	default ComponentPath getCurrentFocusPath() {
		GuiEventListener guiEventListener = this.getFocused();
		return guiEventListener != null ? ComponentPath.path(this, guiEventListener.getCurrentFocusPath()) : null;
	}

	default void magicalSpecialHackyFocus(@Nullable GuiEventListener guiEventListener) {
		this.setFocused(guiEventListener);
	}

	@Nullable
	@Override
	default ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
		GuiEventListener guiEventListener = this.getFocused();
		if (guiEventListener != null) {
			ComponentPath componentPath = guiEventListener.nextFocusPath(focusNavigationEvent);
			if (componentPath != null) {
				return ComponentPath.path(this, componentPath);
			}
		}

		if (focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation tabNavigation) {
			return this.handleTabNavigation(tabNavigation);
		} else {
			return focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation arrowNavigation ? this.handleArrowNavigation(arrowNavigation) : null;
		}
	}

	@Nullable
	private ComponentPath handleTabNavigation(FocusNavigationEvent.TabNavigation tabNavigation) {
		boolean bl = tabNavigation.forward();
		GuiEventListener guiEventListener = this.getFocused();
		List<? extends GuiEventListener> list = new ArrayList(this.children());
		Collections.sort(list, Comparator.comparingInt(guiEventListenerx -> guiEventListenerx.getTabOrderGroup()));
		int i = list.indexOf(guiEventListener);
		int j;
		if (guiEventListener != null && i >= 0) {
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
			ComponentPath componentPath = guiEventListener2.nextFocusPath(tabNavigation);
			if (componentPath != null) {
				return ComponentPath.path(this, componentPath);
			}
		}

		return null;
	}

	@Nullable
	private ComponentPath handleArrowNavigation(FocusNavigationEvent.ArrowNavigation arrowNavigation) {
		GuiEventListener guiEventListener = this.getFocused();
		if (guiEventListener == null) {
			ScreenDirection screenDirection = arrowNavigation.direction();
			ScreenRectangle screenRectangle = this.getRectangle().getBorder(screenDirection.getOpposite());
			return ComponentPath.path(this, this.nextFocusPathInDirection(screenRectangle, screenDirection, null, arrowNavigation));
		} else {
			ScreenRectangle screenRectangle2 = guiEventListener.getRectangle();
			return ComponentPath.path(this, this.nextFocusPathInDirection(screenRectangle2, arrowNavigation.direction(), guiEventListener, arrowNavigation));
		}
	}

	@Nullable
	private ComponentPath nextFocusPathInDirection(
		ScreenRectangle screenRectangle, ScreenDirection screenDirection, @Nullable GuiEventListener guiEventListener, FocusNavigationEvent focusNavigationEvent
	) {
		ScreenAxis screenAxis = screenDirection.getAxis();
		ScreenAxis screenAxis2 = screenAxis.orthogonal();
		ScreenDirection screenDirection2 = screenAxis2.getPositive();
		int i = screenRectangle.getBoundInDirection(screenDirection.getOpposite());
		List<GuiEventListener> list = new ArrayList();

		for (GuiEventListener guiEventListener2 : this.children()) {
			if (guiEventListener2 != guiEventListener) {
				ScreenRectangle screenRectangle2 = guiEventListener2.getRectangle();
				if (screenRectangle2.overlapsInAxis(screenRectangle, screenAxis2)) {
					int j = screenRectangle2.getBoundInDirection(screenDirection.getOpposite());
					if (screenDirection.isAfter(j, i)) {
						list.add(guiEventListener2);
					} else if (j == i && screenDirection.isAfter(screenRectangle2.getBoundInDirection(screenDirection), screenRectangle.getBoundInDirection(screenDirection))) {
						list.add(guiEventListener2);
					}
				}
			}
		}

		Comparator<GuiEventListener> comparator = Comparator.comparing(
			guiEventListenerx -> guiEventListenerx.getRectangle().getBoundInDirection(screenDirection.getOpposite()), screenDirection.coordinateValueComparator()
		);
		Comparator<GuiEventListener> comparator2 = Comparator.comparing(
			guiEventListenerx -> guiEventListenerx.getRectangle().getBoundInDirection(screenDirection2.getOpposite()), screenDirection2.coordinateValueComparator()
		);
		list.sort(comparator.thenComparing(comparator2));

		for (GuiEventListener guiEventListener3 : list) {
			ComponentPath componentPath = guiEventListener3.nextFocusPath(focusNavigationEvent);
			if (componentPath != null) {
				return componentPath;
			}
		}

		return this.nextFocusPathVaguelyInDirection(screenRectangle, screenDirection, guiEventListener, focusNavigationEvent);
	}

	@Nullable
	private ComponentPath nextFocusPathVaguelyInDirection(
		ScreenRectangle screenRectangle, ScreenDirection screenDirection, @Nullable GuiEventListener guiEventListener, FocusNavigationEvent focusNavigationEvent
	) {
		ScreenAxis screenAxis = screenDirection.getAxis();
		ScreenAxis screenAxis2 = screenAxis.orthogonal();
		List<Pair<GuiEventListener, Long>> list = new ArrayList();
		ScreenPosition screenPosition = ScreenPosition.of(
			screenAxis, screenRectangle.getBoundInDirection(screenDirection), screenRectangle.getCenterInAxis(screenAxis2)
		);

		for (GuiEventListener guiEventListener2 : this.children()) {
			if (guiEventListener2 != guiEventListener) {
				ScreenRectangle screenRectangle2 = guiEventListener2.getRectangle();
				ScreenPosition screenPosition2 = ScreenPosition.of(
					screenAxis, screenRectangle2.getBoundInDirection(screenDirection.getOpposite()), screenRectangle2.getCenterInAxis(screenAxis2)
				);
				if (screenDirection.isAfter(screenPosition2.getCoordinate(screenAxis), screenPosition.getCoordinate(screenAxis))) {
					long l = Vector2i.distanceSquared(screenPosition.x(), screenPosition.y(), screenPosition2.x(), screenPosition2.y());
					list.add(Pair.of(guiEventListener2, l));
				}
			}
		}

		list.sort(Comparator.comparingDouble(Pair::getSecond));

		for (Pair<GuiEventListener, Long> pair : list) {
			ComponentPath componentPath = pair.getFirst().nextFocusPath(focusNavigationEvent);
			if (componentPath != null) {
				return componentPath;
			}
		}

		return null;
	}
}
