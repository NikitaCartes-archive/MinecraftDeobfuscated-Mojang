package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;

public class EndGatewayConfiguration implements FeatureConfiguration {
	public static final Codec<EndGatewayConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockPos.CODEC.optionalFieldOf("exit").forGetter(endGatewayConfiguration -> endGatewayConfiguration.exit),
					Codec.BOOL.fieldOf("exact").forGetter(endGatewayConfiguration -> endGatewayConfiguration.exact)
				)
				.apply(instance, EndGatewayConfiguration::new)
	);
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
