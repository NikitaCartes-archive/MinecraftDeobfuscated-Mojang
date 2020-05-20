/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class EndGatewayConfiguration
implements FeatureConfiguration {
    public static final Codec<EndGatewayConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(BlockPos.CODEC.optionalFieldOf("exit").forGetter(endGatewayConfiguration -> endGatewayConfiguration.exit), ((MapCodec)Codec.BOOL.fieldOf("exact")).forGetter(endGatewayConfiguration -> endGatewayConfiguration.exact)).apply((Applicative<EndGatewayConfiguration, ?>)instance, EndGatewayConfiguration::new));
    private final Optional<BlockPos> exit;
    private final boolean exact;

    private EndGatewayConfiguration(Optional<BlockPos> optional, boolean bl) {
        this.exit = optional;
        this.exact = bl;
    }

    public static EndGatewayConfiguration knownExit(BlockPos blockPos, boolean bl) {
        return new EndGatewayConfiguration(Optional.of(blockPos), bl);
    }

    public static EndGatewayConfiguration delayedExitSearch() {
        return new EndGatewayConfiguration(Optional.empty(), false);
    }

    public Optional<BlockPos> getExit() {
        return this.exit;
    }

    public boolean isExitExact() {
        return this.exact;
    }
}

