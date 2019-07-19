/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.AbstractRealmsButton;

@Environment(value=EnvType.CLIENT)
public interface RealmsAbstractButtonProxy<T extends AbstractRealmsButton<?>> {
    public T getButton();

    public boolean active();

    public void active(boolean var1);

    public boolean isVisible();

    public void setVisible(boolean var1);
}

