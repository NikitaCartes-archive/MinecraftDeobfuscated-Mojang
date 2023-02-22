/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;

@Environment(value=EnvType.CLIENT)
public interface LayoutElement {
    public void setX(int var1);

    public void setY(int var1);

    public int getX();

    public int getY();

    public int getWidth();

    public int getHeight();

    default public ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    default public void setPosition(int i, int j) {
        this.setX(i);
        this.setY(j);
    }

    public void visitWidgets(Consumer<AbstractWidget> var1);
}

