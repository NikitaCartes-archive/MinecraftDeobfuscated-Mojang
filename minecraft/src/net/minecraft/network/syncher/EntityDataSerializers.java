package net.minecraft.network.syncher;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Rotations;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityDataSerializers {
	private static final CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS = CrudeIncrementalIntIdentityHashBiMap.create(16);
	public static final EntityDataSerializer<Byte> BYTE = EntityDataSerializer.forValueType(ByteBufCodecs.BYTE);
	public static final EntityDataSerializer<Integer> INT = EntityDataSerializer.forValueType(ByteBufCodecs.VAR_INT);
	public static final EntityDataSerializer<Long> LONG = EntityDataSerializer.forValueType(ByteBufCodecs.VAR_LONG);
	public static final EntityDataSerializer<Float> FLOAT = EntityDataSerializer.forValueType(ByteBufCodecs.FLOAT);
	public static final EntityDataSerializer<String> STRING = EntityDataSerializer.forValueType(ByteBufCodecs.STRING_UTF8);
	public static final EntityDataSerializer<Component> COMPONENT = EntityDataSerializer.forValueType(ComponentSerialization.TRUSTED_STREAM_CODEC);
	public static final EntityDataSerializer<Optional<Component>> OPTIONAL_COMPONENT = EntityDataSerializer.forValueType(
		ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC
	);
	public static final EntityDataSerializer<ItemStack> ITEM_STACK = new EntityDataSerializer<ItemStack>() {
		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ItemStack> codec() {
			return ItemStack.OPTIONAL_STREAM_CODEC;
		}

		public ItemStack copy(ItemStack itemStack) {
			return itemStack.copy();
		}
	};
	public static final EntityDataSerializer<BlockState> BLOCK_STATE = EntityDataSerializer.forValueType(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY));
	private static final StreamCodec<ByteBuf, Optional<BlockState>> OPTIONAL_BLOCK_STATE_CODEC = new StreamCodec<ByteBuf, Optional<BlockState>>() {
		public void encode(ByteBuf byteBuf, Optional<BlockState> optional) {
			if (optional.isPresent()) {
				VarInt.write(byteBuf, Block.getId((BlockState)optional.get()));
			} else {
				VarInt.write(byteBuf, 0);
			}
		}

		public Optional<BlockState> decode(ByteBuf byteBuf) {
			int i = VarInt.read(byteBuf);
			return i == 0 ? Optional.empty() : Optional.of(Block.stateById(i));
		}
	};
	public static final EntityDataSerializer<Optional<BlockState>> OPTIONAL_BLOCK_STATE = EntityDataSerializer.forValueType(OPTIONAL_BLOCK_STATE_CODEC);
	public static final EntityDataSerializer<Boolean> BOOLEAN = EntityDataSerializer.forValueType(ByteBufCodecs.BOOL);
	public static final EntityDataSerializer<ParticleOptions> PARTICLE = EntityDataSerializer.forValueType(ParticleTypes.STREAM_CODEC);
	public static final EntityDataSerializer<List<ParticleOptions>> PARTICLES = EntityDataSerializer.forValueType(
		ParticleTypes.STREAM_CODEC.apply(ByteBufCodecs.list())
	);
	public static final EntityDataSerializer<Rotations> ROTATIONS = EntityDataSerializer.forValueType(Rotations.STREAM_CODEC);
	public static final EntityDataSerializer<BlockPos> BLOCK_POS = EntityDataSerializer.forValueType(BlockPos.STREAM_CODEC);
	public static final EntityDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = EntityDataSerializer.forValueType(
		BlockPos.STREAM_CODEC.apply(ByteBufCodecs::optional)
	);
	public static final EntityDataSerializer<Direction> DIRECTION = EntityDataSerializer.forValueType(Direction.STREAM_CODEC);
	public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.forValueType(
		UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional)
	);
	public static final EntityDataSerializer<Optional<GlobalPos>> OPTIONAL_GLOBAL_POS = EntityDataSerializer.forValueType(
		GlobalPos.STREAM_CODEC.apply(ByteBufCodecs::optional)
	);
	public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = new EntityDataSerializer<CompoundTag>() {
		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, CompoundTag> codec() {
			return ByteBufCodecs.TRUSTED_COMPOUND_TAG;
		}

		public CompoundTag copy(CompoundTag compoundTag) {
			return compoundTag.copy();
		}
	};
	public static final EntityDataSerializer<VillagerData> VILLAGER_DATA = EntityDataSerializer.forValueType(VillagerData.STREAM_CODEC);
	private static final StreamCodec<ByteBuf, OptionalInt> OPTIONAL_UNSIGNED_INT_CODEC = new StreamCodec<ByteBuf, OptionalInt>() {
		public OptionalInt decode(ByteBuf byteBuf) {
			int i = VarInt.read(byteBuf);
			return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
		}

		public void encode(ByteBuf byteBuf, OptionalInt optionalInt) {
			VarInt.write(byteBuf, optionalInt.orElse(-1) + 1);
		}
	};
	public static final EntityDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = EntityDataSerializer.forValueType(OPTIONAL_UNSIGNED_INT_CODEC);
	public static final EntityDataSerializer<Pose> POSE = EntityDataSerializer.forValueType(Pose.STREAM_CODEC);
	public static final EntityDataSerializer<Holder<CatVariant>> CAT_VARIANT = EntityDataSerializer.forValueType(CatVariant.STREAM_CODEC);
	public static final EntityDataSerializer<Holder<WolfVariant>> WOLF_VARIANT = EntityDataSerializer.forValueType(WolfVariant.STREAM_CODEC);
	public static final EntityDataSerializer<Holder<FrogVariant>> FROG_VARIANT = EntityDataSerializer.forValueType(FrogVariant.STREAM_CODEC);
	public static final EntityDataSerializer<Holder<PaintingVariant>> PAINTING_VARIANT = EntityDataSerializer.forValueType(PaintingVariant.STREAM_CODEC);
	public static final EntityDataSerializer<Armadillo.ArmadilloState> ARMADILLO_STATE = EntityDataSerializer.forValueType(Armadillo.ArmadilloState.STREAM_CODEC);
	public static final EntityDataSerializer<Sniffer.State> SNIFFER_STATE = EntityDataSerializer.forValueType(Sniffer.State.STREAM_CODEC);
	public static final EntityDataSerializer<Vector3f> VECTOR3 = EntityDataSerializer.forValueType(ByteBufCodecs.VECTOR3F);
	public static final EntityDataSerializer<Quaternionf> QUATERNION = EntityDataSerializer.forValueType(ByteBufCodecs.QUATERNIONF);

	public static void registerSerializer(EntityDataSerializer<?> entityDataSerializer) {
		SERIALIZERS.add(entityDataSerializer);
	}

	@Nullable
	public static EntityDataSerializer<?> getSerializer(int i) {
		return SERIALIZERS.byId(i);
	}

	public static int getSerializedId(EntityDataSerializer<?> entityDataSerializer) {
		return SERIALIZERS.getId(entityDataSerializer);
	}

	private EntityDataSerializers() {
	}

	static {
		registerSerializer(BYTE);
		registerSerializer(INT);
		registerSerializer(LONG);
		registerSerializer(FLOAT);
		registerSerializer(STRING);
		registerSerializer(COMPONENT);
		registerSerializer(OPTIONAL_COMPONENT);
		registerSerializer(ITEM_STACK);
		registerSerializer(BOOLEAN);
		registerSerializer(ROTATIONS);
		registerSerializer(BLOCK_POS);
		registerSerializer(OPTIONAL_BLOCK_POS);
		registerSerializer(DIRECTION);
		registerSerializer(OPTIONAL_UUID);
		registerSerializer(BLOCK_STATE);
		registerSerializer(OPTIONAL_BLOCK_STATE);
		registerSerializer(COMPOUND_TAG);
		registerSerializer(PARTICLE);
		registerSerializer(PARTICLES);
		registerSerializer(VILLAGER_DATA);
		registerSerializer(OPTIONAL_UNSIGNED_INT);
		registerSerializer(POSE);
		registerSerializer(CAT_VARIANT);
		registerSerializer(WOLF_VARIANT);
		registerSerializer(FROG_VARIANT);
		registerSerializer(OPTIONAL_GLOBAL_POS);
		registerSerializer(PAINTING_VARIANT);
		registerSerializer(SNIFFER_STATE);
		registerSerializer(ARMADILLO_STATE);
		registerSerializer(VECTOR3);
		registerSerializer(QUATERNION);
	}
}
