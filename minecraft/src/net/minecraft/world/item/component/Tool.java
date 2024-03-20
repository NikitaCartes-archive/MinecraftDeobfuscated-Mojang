package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record Tool(List<Tool.Rule> rules, float defaultMiningSpeed, int damagePerBlock) {
	public static final Codec<Tool> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Tool.Rule.CODEC.listOf().fieldOf("rules").forGetter(Tool::rules),
					ExtraCodecs.strictOptionalField(Codec.FLOAT, "default_mining_speed", 1.0F).forGetter(Tool::defaultMiningSpeed),
					ExtraCodecs.strictOptionalField(ExtraCodecs.NON_NEGATIVE_INT, "damage_per_block", 1).forGetter(Tool::damagePerBlock)
				)
				.apply(instance, Tool::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, Tool> STREAM_CODEC = StreamCodec.composite(
		Tool.Rule.STREAM_CODEC.apply(ByteBufCodecs.list()),
		Tool::rules,
		ByteBufCodecs.FLOAT,
		Tool::defaultMiningSpeed,
		ByteBufCodecs.VAR_INT,
		Tool::damagePerBlock,
		Tool::new
	);

	public float getMiningSpeed(BlockState blockState) {
		for (Tool.Rule rule : this.rules) {
			if (rule.speed.isPresent() && blockState.is(rule.blocks)) {
				return (Float)rule.speed.get();
			}
		}

		return this.defaultMiningSpeed;
	}

	public boolean isCorrectForDrops(BlockState blockState) {
		for (Tool.Rule rule : this.rules) {
			if (rule.correctForDrops.isPresent() && blockState.is(rule.blocks)) {
				return (Boolean)rule.correctForDrops.get();
			}
		}

		return false;
	}

	public static record Rule(HolderSet<Block> blocks, Optional<Float> speed, Optional<Boolean> correctForDrops) {
		public static final Codec<Tool.Rule> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(Tool.Rule::blocks),
						ExtraCodecs.strictOptionalField(ExtraCodecs.POSITIVE_FLOAT, "speed").forGetter(Tool.Rule::speed),
						ExtraCodecs.strictOptionalField(Codec.BOOL, "correct_for_drops").forGetter(Tool.Rule::correctForDrops)
					)
					.apply(instance, Tool.Rule::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, Tool.Rule> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderSet(Registries.BLOCK),
			Tool.Rule::blocks,
			ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional),
			Tool.Rule::speed,
			ByteBufCodecs.BOOL.apply(ByteBufCodecs::optional),
			Tool.Rule::correctForDrops,
			Tool.Rule::new
		);

		public static Tool.Rule minesAndDrops(List<Block> list, float f) {
			return forBlocks(list, Optional.of(f), Optional.of(true));
		}

		public static Tool.Rule minesAndDrops(TagKey<Block> tagKey, float f) {
			return forTag(tagKey, Optional.of(f), Optional.of(true));
		}

		public static Tool.Rule deniesDrops(TagKey<Block> tagKey) {
			return forTag(tagKey, Optional.empty(), Optional.of(false));
		}

		public static Tool.Rule overrideSpeed(TagKey<Block> tagKey, float f) {
			return forTag(tagKey, Optional.of(f), Optional.empty());
		}

		public static Tool.Rule overrideSpeed(List<Block> list, float f) {
			return forBlocks(list, Optional.of(f), Optional.empty());
		}

		private static Tool.Rule forTag(TagKey<Block> tagKey, Optional<Float> optional, Optional<Boolean> optional2) {
			return new Tool.Rule(BuiltInRegistries.BLOCK.getOrCreateTag(tagKey), optional, optional2);
		}

		private static Tool.Rule forBlocks(List<Block> list, Optional<Float> optional, Optional<Boolean> optional2) {
			return new Tool.Rule(
				HolderSet.direct((List<? extends Holder<Block>>)list.stream().map(Block::builtInRegistryHolder).collect(Collectors.toList())), optional, optional2
			);
		}
	}
}
