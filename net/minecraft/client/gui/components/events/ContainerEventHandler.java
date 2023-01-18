/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.events;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

@Environment(value=EnvType.CLIENT)
public interface ContainerEventHandler
extends GuiEventListener {
    public List<? extends GuiEventListener> children();

    default public Optional<GuiEventListener> getChildAt(double d, double e) {
        for (GuiEventListener guiEventListener : this.children()) {
            if (!guiEventListener.isMouseOver(d, e)) continue;
            return Optional.of(guiEventListener);
        }
        return Optional.empty();
    }

    @Override
    default public boolean mouseClicked(double d, double e, int i) {
        GuiEventListener guiEventListener = null;
        List<? extends GuiEventListener> list = List.copyOf(this.children());
        for (GuiEventListener guiEventListener2 : list) {
            if (!guiEventListener2.mouseClicked(d, e, i)) continue;
            guiEventListener = guiEventListener2;
        }
        if (guiEventListener != null) {
            this.setFocused(guiEventListener);
            if (i == 0) {
                this.setDragging(true);
            }
            return true;
        }
        return false;
    }

    @Override
    default public boolean mouseReleased(double d, double e, int i) {
        this.setDragging(false);
        return this.getChildAt(d, e).filter(guiEventListener -> guiEventListener.mouseReleased(d, e, i)).isPresent();
    }

    @Override
    default public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (this.getFocused() != null && this.isDragging() && i == 0) {
            return this.getFocused().mouseDragged(d, e, i, f, g);
        }
        return false;
    }

    public boolean isDragging();

    public void setDragging(boolean var1);

    @Override
    default public boolean mouseScrolled(double d, double e, double f) {
        return this.getChildAt(d, e).filter(guiEventListener -> guiEventListener.mouseScrolled(d, e, f)).isPresent();
    }

    @Override
    default public boolean keyPressed(int i, int j, int k) {
        return this.getFocused() != null && this.getFocused().keyPressed(i, j, k);
    }

    @Override
    default public boolean keyReleased(int i, int j, int k) {
        return this.getFocused() != null && this.getFocused().keyReleased(i, j, k);
    }

    @Override
    default public boolean charTyped(char c, int i) {
        return this.getFocused() != null && this.getFocused().charTyped(c, i);
    }

    @Nullable
    public GuiEventListener getFocused();

    public void setFocused(@Nullable GuiEventListener var1);

    @Override
    default public void setFocused(boolean bl) {
    }

    @Override
    default public boolean isFocused() {
        return this.getFocused() != null;
    }

    @Override
    @Nullable
    default public ComponentPath getCurrentFocusPath() {
        GuiEventListener guiEventListener = this.getFocused();
        if (guiEventListener != null) {
            return ComponentPath.path(this, guiEventListener.getCurrentFocusPath());
        }
        return null;
    }

    default public void magicalSpecialHackyFocus(@Nullable GuiEventListener guiEventListener) {
        this.setFocused(guiEventListener);
    }

    @Override
    @Nullable
    default public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        ComponentPath componentPath;
        GuiEventListener guiEventListener = this.getFocused();
        if (guiEventListener != null && (componentPath = guiEventListener.nextFocusPath(focusNavigationEvent)) != null) {
            return ComponentPath.path(this, componentPath);
        }
        if (focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation) {
            FocusNavigationEvent.TabNavigation tabNavigation = (FocusNavigationEvent.TabNavigation)focusNavigationEvent;
            return this.handleTabNavigation(tabNavigation);
        }
        if (focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation) {
            FocusNavigationEvent.ArrowNavigation arrowNavigation = (FocusNavigationEvent.ArrowNavigation)focusNavigationEvent;
            return this.handleArrowNavigation(arrowNavigation);
        }
        return null;
    }

    @Nullable
    private ComponentPath handleTabNavigation(FocusNavigationEvent.TabNavigation tabNavigation) {
        Supplier<GuiEventListener> supplier;
        BooleanSupplier booleanSupplier;
        boolean bl = tabNavigation.forward();
        GuiEventListener guiEventListener = this.getFocused();
        List<? extends GuiEventListener> list = this.children();
        int i = list.indexOf(guiEventListener);
        int j = guiEventListener != null && i >= 0 ? i + (bl ? 1 : 0) : (bl ? 0 : list.size());
        ListIterator<? extends GuiEventListener> listIterator = list.listIterator(j);
        BooleanSupplier booleanSupplier2 = bl ? listIterator::hasNext : (booleanSupplier = listIterator::hasPrevious);
        Supplier<GuiEventListener> supplier2 = bl ? listIterator::next : (supplier = listIterator::previous);
        while (booleanSupplier.getAsBoolean()) {
            GuiEventListener guiEventListener2 = supplier.get();
            ComponentPath componentPath = guiEventListener2.nextFocusPath(tabNavigation);
            if (componentPath == null) continue;
            return ComponentPath.path(this, componentPath);
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
        }
        ScreenRectangle screenRectangle2 = guiEventListener.getRectangle();
        return ComponentPath.path(this, this.nextFocusPathInDirection(screenRectangle2, arrowNavigation.direction(), guiEventListener, arrowNavigation));
    }

    @Nullable
    private ComponentPath nextFocusPathInDirection(ScreenRectangle screenRectangle, ScreenDirection screenDirection, @Nullable GuiEventListener guiEventListener2, FocusNavigationEvent focusNavigationEvent) {
        ScreenAxis screenAxis = screenDirection.getAxis();
        ScreenAxis screenAxis2 = screenAxis.orthogonal();
        ScreenDirection screenDirection2 = screenAxis2.getPositive();
        int i = screenRectangle.getBoundInDirection(screenDirection.getOpposite());
        ArrayList<GuiEventListener> list = new ArrayList<GuiEventListener>();
        for (GuiEventListener guiEventListener3 : this.children()) {
            ScreenRectangle screenRectangle2;
            if (guiEventListener3 == guiEventListener2 || !(screenRectangle2 = guiEventListener3.getRectangle()).overlapsInAxis(screenRectangle, screenAxis2)) continue;
            int j = screenRectangle2.getBoundInDirection(screenDirection.getOpposite());
            if (screenDirection.isAfter(j, i)) {
                list.add(guiEventListener3);
                continue;
            }
            if (j != i || !screenDirection.isAfter(screenRectangle2.getBoundInDirection(screenDirection), screenRectangle.getBoundInDirection(screenDirection))) continue;
            list.add(guiEventListener3);
        }
        Comparator<GuiEventListener> comparator = Comparator.comparing(guiEventListener -> guiEventListener.getRectangle().getBoundInDirection(screenDirection.getOpposite()), screenDirection.coordinateValueComparator());
        Comparator<GuiEventListener> comparator2 = Comparator.comparing(guiEventListener -> guiEventListener.getRectangle().getBoundInDirection(screenDirection2.getOpposite()), screenDirection2.coordinateValueComparator());
        list.sort(comparator.thenComparing(comparator2));
        for (GuiEventListener guiEventListener3 : list) {
            ComponentPath componentPath = guiEventListener3.nextFocusPath(focusNavigationEvent);
            if (componentPath == null) continue;
            return componentPath;
        }
        return this.nextFocusPathVaguelyInDirection(screenRectangle, screenDirection, guiEventListener2, focusNavigationEvent);
    }

    @Nullable
    private ComponentPath nextFocusPathVaguelyInDirection(ScreenRectangle screenRectangle, ScreenDirection screenDirection, @Nullable GuiEventListener guiEventListener, FocusNavigationEvent focusNavigationEvent) {
        ScreenAxis screenAxis = screenDirection.getAxis();
        ScreenAxis screenAxis2 = screenAxis.orthogonal();
        ArrayList<Pair> list = new ArrayList<Pair>();
        ScreenPosition screenPosition = ScreenPosition.of(screenAxis, screenRectangle.getBoundInDirection(screenDirection), screenRectangle.getCenterInAxis(screenAxis2));
        for (GuiEventListener guiEventListener2 : this.children()) {
            ScreenRectangle screenRectangle2;
            ScreenPosition screenPosition2;
            if (guiEventListener2 == guiEventListener || !screenDirection.isAfter((screenPosition2 = ScreenPosition.of(screenAxis, (screenRectangle2 = guiEventListener2.getRectangle()).getBoundInDirection(screenDirection.getOpposite()), screenRectangle2.getCenterInAxis(screenAxis2))).getCoordinate(screenAxis), screenPosition.getCoordinate(screenAxis))) continue;
            long l = Vector2i.distanceSquared(screenPosition.x(), screenPosition.y(), screenPosition2.x(), screenPosition2.y());
            list.add(Pair.of(guiEventListener2, l));
        }
        list.sort(Comparator.comparingDouble(Pair::getSecond));
        for (Pair pair : list) {
            ComponentPath componentPath = ((GuiEventListener)pair.getFirst()).nextFocusPath(focusNavigationEvent);
            if (componentPath == null) continue;
            return componentPath;
        }
        return null;
    }
}

