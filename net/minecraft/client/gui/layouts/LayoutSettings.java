/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.layouts;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface LayoutSettings {
    public LayoutSettings padding(int var1);

    public LayoutSettings padding(int var1, int var2);

    public LayoutSettings padding(int var1, int var2, int var3, int var4);

    public LayoutSettings paddingLeft(int var1);

    public LayoutSettings paddingTop(int var1);

    public LayoutSettings paddingRight(int var1);

    public LayoutSettings paddingBottom(int var1);

    public LayoutSettings paddingHorizontal(int var1);

    public LayoutSettings paddingVertical(int var1);

    public LayoutSettings align(float var1, float var2);

    public LayoutSettings alignHorizontally(float var1);

    public LayoutSettings alignVertically(float var1);

    default public LayoutSettings alignHorizontallyLeft() {
        return this.alignHorizontally(0.0f);
    }

    default public LayoutSettings alignHorizontallyCenter() {
        return this.alignHorizontally(0.5f);
    }

    default public LayoutSettings alignHorizontallyRight() {
        return this.alignHorizontally(1.0f);
    }

    default public LayoutSettings alignVerticallyTop() {
        return this.alignVertically(0.0f);
    }

    default public LayoutSettings alignVerticallyMiddle() {
        return this.alignVertically(0.5f);
    }

    default public LayoutSettings alignVerticallyBottom() {
        return this.alignVertically(1.0f);
    }

    public LayoutSettings copy();

    public LayoutSettingsImpl getExposed();

    public static LayoutSettings defaults() {
        return new LayoutSettingsImpl();
    }

    @Environment(value=EnvType.CLIENT)
    public static class LayoutSettingsImpl
    implements LayoutSettings {
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;
        public float xAlignment;
        public float yAlignment;

        public LayoutSettingsImpl() {
        }

        public LayoutSettingsImpl(LayoutSettingsImpl layoutSettingsImpl) {
            this.paddingLeft = layoutSettingsImpl.paddingLeft;
            this.paddingTop = layoutSettingsImpl.paddingTop;
            this.paddingRight = layoutSettingsImpl.paddingRight;
            this.paddingBottom = layoutSettingsImpl.paddingBottom;
            this.xAlignment = layoutSettingsImpl.xAlignment;
            this.yAlignment = layoutSettingsImpl.yAlignment;
        }

        @Override
        public LayoutSettingsImpl padding(int i) {
            return this.padding(i, i);
        }

        @Override
        public LayoutSettingsImpl padding(int i, int j) {
            return this.paddingHorizontal(i).paddingVertical(j);
        }

        @Override
        public LayoutSettingsImpl padding(int i, int j, int k, int l) {
            return this.paddingLeft(i).paddingRight(k).paddingTop(j).paddingBottom(l);
        }

        @Override
        public LayoutSettingsImpl paddingLeft(int i) {
            this.paddingLeft = i;
            return this;
        }

        @Override
        public LayoutSettingsImpl paddingTop(int i) {
            this.paddingTop = i;
            return this;
        }

        @Override
        public LayoutSettingsImpl paddingRight(int i) {
            this.paddingRight = i;
            return this;
        }

        @Override
        public LayoutSettingsImpl paddingBottom(int i) {
            this.paddingBottom = i;
            return this;
        }

        @Override
        public LayoutSettingsImpl paddingHorizontal(int i) {
            return this.paddingLeft(i).paddingRight(i);
        }

        @Override
        public LayoutSettingsImpl paddingVertical(int i) {
            return this.paddingTop(i).paddingBottom(i);
        }

        @Override
        public LayoutSettingsImpl align(float f, float g) {
            this.xAlignment = f;
            this.yAlignment = g;
            return this;
        }

        @Override
        public LayoutSettingsImpl alignHorizontally(float f) {
            this.xAlignment = f;
            return this;
        }

        @Override
        public LayoutSettingsImpl alignVertically(float f) {
            this.yAlignment = f;
            return this;
        }

        @Override
        public LayoutSettingsImpl copy() {
            return new LayoutSettingsImpl(this);
        }

        @Override
        public LayoutSettingsImpl getExposed() {
            return this;
        }

        @Override
        public /* synthetic */ LayoutSettings copy() {
            return this.copy();
        }

        @Override
        public /* synthetic */ LayoutSettings alignVertically(float f) {
            return this.alignVertically(f);
        }

        @Override
        public /* synthetic */ LayoutSettings alignHorizontally(float f) {
            return this.alignHorizontally(f);
        }

        @Override
        public /* synthetic */ LayoutSettings align(float f, float g) {
            return this.align(f, g);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingVertical(int i) {
            return this.paddingVertical(i);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingHorizontal(int i) {
            return this.paddingHorizontal(i);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingBottom(int i) {
            return this.paddingBottom(i);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingRight(int i) {
            return this.paddingRight(i);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingTop(int i) {
            return this.paddingTop(i);
        }

        @Override
        public /* synthetic */ LayoutSettings paddingLeft(int i) {
            return this.paddingLeft(i);
        }

        @Override
        public /* synthetic */ LayoutSettings padding(int i, int j, int k, int l) {
            return this.padding(i, j, k, l);
        }

        @Override
        public /* synthetic */ LayoutSettings padding(int i, int j) {
            return this.padding(i, j);
        }

        @Override
        public /* synthetic */ LayoutSettings padding(int i) {
            return this.padding(i);
        }
    }
}

