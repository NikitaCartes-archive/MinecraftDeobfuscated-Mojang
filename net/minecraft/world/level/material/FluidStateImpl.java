/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.material;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.level.block.state.AbstractStateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class FluidStateImpl
extends AbstractStateHolder<Fluid, FluidState>
implements FluidState {
    public FluidStateImpl(Fluid fluid, ImmutableMap<Property<?>, Comparable<?>> immutableMap) {
        super(fluid, immutableMap);
    }

    @Override
    public Fluid getType() {
        return (Fluid)this.owner;
    }
}

