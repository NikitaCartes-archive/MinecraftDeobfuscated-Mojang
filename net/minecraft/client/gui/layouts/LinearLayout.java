/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

@Environment(value=EnvType.CLIENT)
public class LinearLayout
extends AbstractLayout {
    private final Orientation orientation;
    private final List<ChildContainer> children = new ArrayList<ChildContainer>();
    private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();

    public LinearLayout(int i, int j, Orientation orientation) {
        this(0, 0, i, j, orientation);
    }

    public LinearLayout(int i, int j, int k, int l, Orientation orientation) {
        super(i, j, k, l);
        this.orientation = orientation;
    }

    @Override
    public void arrangeElements() {
        super.arrangeElements();
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
        switch (this.orientation) {
            case HORIZONTAL: {
                this.height = j;
                break;
            }
            case VERTICAL: {
                this.width = j;
            }
        }
    }

    @Override
    protected void visitChildren(Consumer<LayoutElement> consumer) {
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
        this.children.add(new ChildContainer(layoutElement, layoutSettings));
        return layoutElement;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Orientation {
        HORIZONTAL,
        VERTICAL;


        int getPrimaryLength(LayoutElement layoutElement) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case HORIZONTAL -> layoutElement.getWidth();
                case VERTICAL -> layoutElement.getHeight();
            };
        }

        int getPrimaryLength(ChildContainer childContainer) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case HORIZONTAL -> childContainer.getWidth();
                case VERTICAL -> childContainer.getHeight();
            };
        }

        int getSecondaryLength(LayoutElement layoutElement) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case HORIZONTAL -> layoutElement.getHeight();
                case VERTICAL -> layoutElement.getWidth();
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

        int getPrimaryPosition(LayoutElement layoutElement) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case HORIZONTAL -> layoutElement.getX();
                case VERTICAL -> layoutElement.getY();
            };
        }

        int getSecondaryPosition(LayoutElement layoutElement) {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case HORIZONTAL -> layoutElement.getY();
                case VERTICAL -> layoutElement.getX();
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ChildContainer
    extends AbstractLayout.AbstractChildWrapper {
        protected ChildContainer(LayoutElement layoutElement, LayoutSettings layoutSettings) {
            super(layoutElement, layoutSettings);
        }
    }
}

