package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class EndGatewayConfiguration implements FeatureConfiguration {
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

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			(T)this.exit
				.map(
					blockPos -> dynamicOps.createMap(
							ImmutableMap.of(
								dynamicOps.createString("exit_x"),
								dynamicOps.createInt(blockPos.getX()),
								dynamicOps.createString("exit_y"),
								dynamicOps.createInt(blockPos.getY()),
								dynamicOps.createString("exit_z"),
								dynamicOps.createInt(blockPos.getZ()),
								dynamicOps.createString("exact"),
								dynamicOps.createBoolean(this.exact)
							)
						)
				)
				.orElse(dynamicOps.emptyMap())
		);
	}

	public static <T> EndGatewayConfiguration deserialize(Dynamic<T> dynamic) {
		Optional<BlockPos> optional = dynamic.get("exit_x")
			.asNumber()
			.flatMap(
				number -> dynamic.get("exit_y")
						.asNumber()
						.flatMap(number2 -> dynamic.get("exit_z").asNumber().map(number3 -> new BlockPos(number.intValue(), number2.intValue(), number3.intValue())))
			);
		boolean bl = dynamic.get("exact").asBoolean(false);
		return new EndGatewayConfiguration(optional, bl);
	}

	public static EndGatewayConfiguration random(Random random) {
		return new EndGatewayConfiguration(Optional.of(new BlockPos(random.nextInt(4096), random.nextInt(4096), random.nextInt(4096))), false);
	}
}
