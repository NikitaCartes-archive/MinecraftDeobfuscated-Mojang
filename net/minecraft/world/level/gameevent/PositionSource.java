/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.phys.Vec3;

public interface PositionSource {
    public static final Codec<PositionSource> CODEC = Registry.POSITION_SOURCE_TYPE.byNameCodec().dispatch(PositionSource::getType, PositionSourceType::codec);

    public Optional<Vec3> getPosition(Level var1);

    public PositionSourceType<?> getType();
}

