/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.PositionSourceType;

public interface PositionSource {
    public static final Codec<PositionSource> CODEC = Registry.POSITION_SOURCE_TYPE.dispatch(PositionSource::getType, PositionSourceType::codec);

    public Optional<BlockPos> getPosition(Level var1);

    public PositionSourceType<?> getType();
}

