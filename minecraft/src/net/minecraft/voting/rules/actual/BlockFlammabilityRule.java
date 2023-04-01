package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.voting.rules.MapRule;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.level.block.Block;

public class BlockFlammabilityRule extends BlockMapRule<BlockFlammabilityRule.Flammability> {
	public BlockFlammabilityRule() {
		super(BlockFlammabilityRule.Flammability.CODEC);
	}

	protected Component description(ResourceKey<Block> resourceKey, BlockFlammabilityRule.Flammability flammability) {
		return Component.translatable(flammability.descriptionId, Component.translatable(Util.makeDescriptionId("block", resourceKey.location())));
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		Registry<Block> registry = minecraftServer.registryAccess().registryOrThrow(Registries.BLOCK);
		return registry.getRandom(randomSource).stream().flatMap(reference -> {
			BlockFlammabilityRule.Flammability flammability = this.get((Block)reference.value());
			ObjectArrayList<BlockFlammabilityRule.Flammability> objectArrayList = new ObjectArrayList<>(BlockFlammabilityRule.Flammability.values());
			if (flammability != null) {
				objectArrayList.remove(flammability);
			}

			Util.shuffle(objectArrayList, randomSource);
			return objectArrayList.stream().map(flammabilityx -> new MapRule.MapRuleChange(reference.key(), flammabilityx));
		}).limit((long)i).map(mapRuleChange -> mapRuleChange);
	}

	public static enum Flammability implements StringRepresentable {
		LOW("low", 5, 5),
		MEDIUM("medium", 15, 20),
		HIGH("high", 30, 60),
		VERY_HIGH("very_high", 60, 100);

		public static final Codec<BlockFlammabilityRule.Flammability> CODEC = StringRepresentable.fromEnum(BlockFlammabilityRule.Flammability::values);
		private final String name;
		final String descriptionId;
		private final int igniteOdds;
		private final int burnOdds;

		private Flammability(String string2, int j, int k) {
			this.name = string2;
			this.descriptionId = "rule.flammability." + string2;
			this.igniteOdds = j;
			this.burnOdds = k;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public int burnOdds() {
			return this.burnOdds;
		}

		public int igniteOdds() {
			return this.igniteOdds;
		}
	}
}
