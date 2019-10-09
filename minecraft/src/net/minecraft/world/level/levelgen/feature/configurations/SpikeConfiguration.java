package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;

public class SpikeConfiguration implements FeatureConfiguration {
	private final boolean crystalInvulnerable;
	private final List<SpikeFeature.EndSpike> spikes;
	@Nullable
	private final BlockPos crystalBeamTarget;

	public SpikeConfiguration(boolean bl, List<SpikeFeature.EndSpike> list, @Nullable BlockPos blockPos) {
		this.crystalInvulnerable = bl;
		this.spikes = list;
		this.crystalBeamTarget = blockPos;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("crystalInvulnerable"),
					dynamicOps.createBoolean(this.crystalInvulnerable),
					dynamicOps.createString("spikes"),
					dynamicOps.createList(this.spikes.stream().map(endSpike -> endSpike.serialize(dynamicOps).getValue())),
					dynamicOps.createString("crystalBeamTarget"),
					this.crystalBeamTarget == null
						? dynamicOps.createList(Stream.empty())
						: dynamicOps.createList(
							IntStream.of(new int[]{this.crystalBeamTarget.getX(), this.crystalBeamTarget.getY(), this.crystalBeamTarget.getZ()}).mapToObj(dynamicOps::createInt)
						)
				)
			)
		);
	}

	public static <T> SpikeConfiguration deserialize(Dynamic<T> dynamic) {
		List<SpikeFeature.EndSpike> list = dynamic.get("spikes").asList(SpikeFeature.EndSpike::deserialize);
		List<Integer> list2 = dynamic.get("crystalBeamTarget").asList(dynamicx -> dynamicx.asInt(0));
		BlockPos blockPos;
		if (list2.size() == 3) {
			blockPos = new BlockPos((Integer)list2.get(0), (Integer)list2.get(1), (Integer)list2.get(2));
		} else {
			blockPos = null;
		}

		return new SpikeConfiguration(dynamic.get("crystalInvulnerable").asBoolean(false), list, blockPos);
	}

	public boolean isCrystalInvulnerable() {
		return this.crystalInvulnerable;
	}

	public List<SpikeFeature.EndSpike> getSpikes() {
		return this.spikes;
	}

	@Nullable
	public BlockPos getCrystalBeamTarget() {
		return this.crystalBeamTarget;
	}
}
