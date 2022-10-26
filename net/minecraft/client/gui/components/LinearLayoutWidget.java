/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.LayoutSettings;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class LinearLayoutWidget
extends AbstractContainerWidget {
    private final Orientation orientation;
    private final List<ChildContainer> children = new ArrayList<ChildContainer>();
    private final List<AbstractWidget> containedChildrenView = Collections.unmodifiableList(Lists.transform(this.children, childContainer -> childContainer.child));
    private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();

    public LinearLayoutWidget(int i, int j, Orientation orientation) {
        this(0, 0, i, j, orientation);
    }

    public LinearLayoutWidget(int i, int j, int k, int l, Orientation orientation) {
        super(i, j, k, l, Component.empty());
        this.orientation = orientation;
    }

    public void pack() {
        if (this.children.isEmpty()) {
            return;
        }
        int i = 0;
        int j = this.orientation.getSecondaryLength(this);
        for (ChildContainer childContainer : this.children) {
            i += this.orientation.getPrimaryLength(childContainer);
            j = Math.max(j, this.orientation.getSecondaryLength(childContainer));
        }
        int k = this.orientation.getPrimaryLength(this) - i;
        int l = this.orientation.getPrimaryPosition(this);
        Iterator<ChildContainer> iterator = this.children.iterator();
        ChildContainer childContainer2 = iterator.next();
        this.orientation.setPrimaryPosition(childContainer2, l);
        l += this.orientation.getPrimaryLength(childContainer2);
        if (this.children.size() >= 2) {
            Divisor divisor = new Divisor(k, this.children.size() - 1);
            while (divisor.hasNext()) {
                ChildContainer childContainer3 = iterator.next();
                this.orientation.setPrimaryPosition(childContainer3, l += divisor.nextInt());
                l += this.orientation.getPrimaryLength(childContainer3);
            }
        }
        int m = this.orientation.getSecondaryPosition(this);
        for (ChildContainer childContainer4 : this.children) {
            this.orientation.setSecondaryPosition(childContainer4, m, j);
        }
        this.orientation.setSecondaryLength(this, j);
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
        this.children.add(new ChildContainer(abstractWidget, layoutSettings));
        return abstractWidget;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Orientation {
        HORIZONTAL,
        VERTICAL;


        int getPrimaryLength(AbstractWidget abstractWidget) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case HORIZONTAL -> abstractWidget.getWidth();
                case VERTICAL -> abstractWidget.getHeight();
            };
        }

        int getPrimaryLength(ChildContainer childContainer) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case HORIZONTAL -> childContainer.getWidth();
                case VERTICAL -> childContainer.getHeight();
            };
        }

        int getSecondaryLength(AbstractWidget abstractWidget) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case HORIZONTAL -> abstractWidget.getHeight();
                case VERTICAL -> abstractWidget.getWidth();
            };
        }

        int getSecondaryLength(ChildContainer childContainer) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case HORIZONTAL -> childContainer.getHeight();
                case VERTICAL -> childContainer.getWidth();
            };
        }

        void setPrimaryPosition(ChildContainer childContainer, int i) {
            switch (this) {
                case HORIZONTAL: {
                    childContainer.setX(i, childContainer.getWidth());
                    break;
                }
                case VERTICAL: {
                    childContainer.setY(i, childContainer.getHeight());
                }
            }
        }

        void setSecondaryPosition(ChildContainer childContainer, int i, int j) {
            switch (this) {
                case HORIZONTAL: {
                    childContainer.setY(i, j);
                    break;
                }
                case VERTICAL: {
                    childContainer.setX(i, j);
                }
            }
        }

        int getPrimaryPosition(AbstractWidget abstractWidget) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case HORIZONTAL -> abstractWidget.getX();
                case VERTICAL -> abstractWidget.getY();
            };
        }

        int getSecondaryPosition(AbstractWidget abstractWidget) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case HORIZONTAL -> abstractWidget.getY();
                case VERTICAL -> abstractWidget.getX();
            };
        }

        void setSecondaryLength(AbstractWidget abstractWidget, int i) {
            switch (this) {
                case HORIZONTAL: {
                    abstractWidget.height = i;
                    break;
                }
                case VERTICAL: {
                    abstractWidget.width = i;
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ChildContainer
    extends AbstractContainerWidget.AbstractChildWrapper {
        protected ChildContainer(AbstractWidget abstractWidget, LayoutSettings layoutSettings) {
            super(abstractWidget, layoutSettings);
        }
    }
}

