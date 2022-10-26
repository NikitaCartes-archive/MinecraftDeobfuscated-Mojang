/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class FrameWidget
extends AbstractContainerWidget {
    private final List<ChildContainer> children = new ArrayList<ChildContainer>();
    private final List<AbstractWidget> containedChildrenView = Collections.unmodifiableList(Lists.transform(this.children, childContainer -> childContainer.child));
    private int minWidth;
    private int minHeight;
    private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults().align(0.5f, 0.5f);

    public static FrameWidget withMinDimensions(int i, int j) {
        return new FrameWidget(0, 0, 0, 0).setMinDimensions(i, j);
    }

    public FrameWidget() {
        this(0, 0, 0, 0);
    }

    public FrameWidget(int i, int j, int k, int l) {
        super(i, j, k, l, Component.empty());
    }

    public FrameWidget setMinDimensions(int i, int j) {
        return this.setMinWidth(i).setMinHeight(j);
    }

    public FrameWidget setMinHeight(int i) {
        this.minHeight = i;
        return this;
    }

    public FrameWidget setMinWidth(int i) {
        this.minWidth = i;
        return this;
    }

    public LayoutSettings newChildLayoutSettings() {
        return this.defaultChildLayoutSettings.copy();
    }

    public LayoutSettings defaultChildLayoutSetting() {
        return this.defaultChildLayoutSettings;
    }

    public void pack() {
        int i = this.minWidth;
        int j = this.minHeight;
        for (ChildContainer childContainer : this.children) {
            i = Math.max(i, childContainer.getWidth());
            j = Math.max(j, childContainer.getHeight());
        }
        for (ChildContainer childContainer : this.children) {
            childContainer.setX(this.getX(), i);
            childContainer.setY(this.getY(), j);
        }
        this.width = i;
        this.height = j;
    }

    public <T extends AbstractWidget> T addChild(T abstractWidget) {
        return this.addChild(abstractWidget, this.newChildLayoutSettings());
    }

    public <T extends AbstractWidget> T addChild(T abstractWidget, LayoutSettings layoutSettings) {
        this.children.add(new ChildContainer(abstractWidget, layoutSettings));
        return abstractWidget;
    }

    protected List<AbstractWidget> getContainedChildren() {
        return this.containedChildrenView;
    }

    public static void centerInRectangle(AbstractWidget abstractWidget, int i, int j, int k, int l) {
        FrameWidget.alignInRectangle(abstractWidget, i, j, k, l, 0.5f, 0.5f);
    }

    public static void alignInRectangle(AbstractWidget abstractWidget, int i, int j, int k, int l, float f, float g) {
        FrameWidget.alignInDimension(i, k, abstractWidget.getWidth(), abstractWidget::setX, f);
        FrameWidget.alignInDimension(j, l, abstractWidget.getHeight(), abstractWidget::setY, g);
    }

    public static void alignInDimension(int i, int j, int k, Consumer<Integer> consumer, float f) {
        int l = (int)Mth.lerp(f, 0.0f, j - k);
        consumer.accept(i + l);
    }

    @Environment(value=EnvType.CLIENT)
    static class ChildContainer
    extends AbstractContainerWidget.AbstractChildWrapper {
        protected ChildContainer(AbstractWidget abstractWidget, LayoutSettings layoutSettings) {
            super(abstractWidget, layoutSettings);
        }
    }
}

