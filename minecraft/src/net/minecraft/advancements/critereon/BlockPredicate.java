package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockPredicate(Optional<HolderSet<Block>> blocks, Optional<StatePropertiesPredicate> properties, Optional<NbtPredicate> nbt) {
	public static final Codec<BlockPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("blocks").forGetter(BlockPredicate::blocks),
					StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(BlockPredicate::properties),
					NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(BlockPredicate::nbt)
				)
				.apply(instance, BlockPredicate::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockPredicate> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(ByteBufCodecs.holderSet(Registries.BLOCK)),
		BlockPredicate::blocks,
		ByteBufCodecs.optional(StatePropertiesPredicate.STREAM_CODEC),
		BlockPredicate::properties,
		ByteBufCodecs.optional(NbtPredicate.STREAM_CODEC),
		BlockPredicate::nbt,
		BlockPredicate::new
	);

	public boolean matches(ServerLevel serverLevel, BlockPos blockPos) {
		if (!serverLevel.isLoaded(blockPos)) {
			return false;
		} else {
			return !this.matchesState(serverLevel.getBlockState(blockPos))
				? false
				: !this.nbt.isPresent() || matchesBlockEntity(serverLevel, serverLevel.getBlockEntity(blockPos), (NbtPredicate)this.nbt.get());
		}
	}

	public boolean matches(BlockInWorld blockInWorld) {
		return !this.matchesState(blockInWorld.getState())
			? false
			: !this.nbt.isPresent() || matchesBlockEntity(blockInWorld.getLevel(), blockInWorld.getEntity(), (NbtPredicate)this.nbt.get());
	}

	private boolean matchesState(BlockState blockState) {
		return this.blocks.isPresent() && !blockState.is((HolderSet<Block>)this.blocks.get())
			? false
			: !this.properties.isPresent() || ((StatePropertiesPredicate)this.properties.get()).matches(blockState);
	}

	private static boolean matchesBlockEntity(LevelReader levelReader, @Nullable BlockEntity blockEntity, NbtPredicate nbtPredicate) {
		return blockEntity != null && nbtPredicate.matches(blockEntity.saveWithFullMetadata(levelReader.registryAccess()));
	}

	public boolean requiresNbt() {
		return this.nbt.isPresent();
	}

	public static class Builder {
		private Optional<HolderSet<Block>> blocks = Optional.empty();
		private Optional<StatePropertiesPredicate> properties = Optional.empty();
		private Optional<NbtPredicate> nbt = Optional.empty();

		private Builder() {
		}

		public static BlockPredicate.Builder block() {
			return new BlockPredicate.Builder();
		}

		public BlockPredicate.Builder of(HolderGetter<Block> holderGetter, Block... blocks) {
			return this.of(holderGetter, Arrays.asList(blocks));
		}

		public BlockPredicate.Builder of(HolderGetter<Block> holderGetter, Collection<Block> collection) {
			this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, collection));
			return this;
		}

		public BlockPredicate.Builder of(HolderGetter<Block> holderGetter, TagKey<Block> tagKey) {
			this.blocks = Optional.of(holderGetter.getOrThrow(tagKey));
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
			return new BlockPredicate(this.blocks, this.properties, this.nbt);
		}
	}
}
