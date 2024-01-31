package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public record BlockPredicate(
	Optional<TagKey<Block>> tag, Optional<HolderSet<Block>> blocks, Optional<StatePropertiesPredicate> properties, Optional<NbtPredicate> nbt
) {
	private static final Codec<HolderSet<Block>> BLOCKS_CODEC = BuiltInRegistries.BLOCK
		.holderByNameCodec()
		.listOf()
		.xmap(HolderSet::direct, holderSet -> holderSet.stream().toList());
	public static final Codec<BlockPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(TagKey.codec(Registries.BLOCK), "tag").forGetter(BlockPredicate::tag),
					ExtraCodecs.strictOptionalField(BLOCKS_CODEC, "blocks").forGetter(BlockPredicate::blocks),
					ExtraCodecs.strictOptionalField(StatePropertiesPredicate.CODEC, "state").forGetter(BlockPredicate::properties),
					ExtraCodecs.strictOptionalField(NbtPredicate.CODEC, "nbt").forGetter(BlockPredicate::nbt)
				)
				.apply(instance, BlockPredicate::new)
	);

	public boolean matches(ServerLevel serverLevel, BlockPos blockPos) {
		if (!serverLevel.isLoaded(blockPos)) {
			return false;
		} else {
			BlockState blockState = serverLevel.getBlockState(blockPos);
			if (this.tag.isPresent() && !blockState.is((TagKey<Block>)this.tag.get())) {
				return false;
			} else if (this.blocks.isPresent() && !blockState.is((HolderSet<Block>)this.blocks.get())) {
				return false;
			} else if (this.properties.isPresent() && !((StatePropertiesPredicate)this.properties.get()).matches(blockState)) {
				return false;
			} else {
				if (this.nbt.isPresent()) {
					BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
					if (blockEntity == null || !((NbtPredicate)this.nbt.get()).matches(blockEntity.saveWithFullMetadata(serverLevel.registryAccess()))) {
						return false;
					}
				}

				return true;
			}
		}
	}

	public static class Builder {
		private Optional<HolderSet<Block>> blocks = Optional.empty();
		private Optional<TagKey<Block>> tag = Optional.empty();
		private Optional<StatePropertiesPredicate> properties = Optional.empty();
		private Optional<NbtPredicate> nbt = Optional.empty();

		private Builder() {
		}

		public static BlockPredicate.Builder block() {
			return new BlockPredicate.Builder();
		}

		public BlockPredicate.Builder of(Block... blocks) {
			this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, blocks));
			return this;
		}

		public BlockPredicate.Builder of(Collection<Block> collection) {
			this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, collection));
			return this;
		}

		public BlockPredicate.Builder of(TagKey<Block> tagKey) {
			this.tag = Optional.of(tagKey);
			return this;
		}

		public BlockPredicate.Builder hasNbt(CompoundTag compoundTag) {
			this.nbt = Optional.of(new NbtPredicate(compoundTag));
			return this;
		}

		public BlockPredicate.Builder setProperties(StatePropertiesPredicate.Builder builder) {
			this.properties = builder.build();
			return this;
		}

		public BlockPredicate build() {
			return new BlockPredicate(this.tag, this.blocks, this.properties, this.nbt);
		}
	}
}
