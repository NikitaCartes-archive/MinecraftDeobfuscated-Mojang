/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.properties;

import java.util.Collection;
import java.util.Optional;

public interface Property<T extends Comparable<T>> {
    public String getName();

    public Collection<T> getPossibleValues();

    public Class<T> getValueClass();

    public Optional<T> getValue(String var1);

    public String getName(T var1);
}

