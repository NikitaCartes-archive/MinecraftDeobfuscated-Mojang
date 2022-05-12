package net.minecraft.network.syncher;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EntityDataSerializers {
	private static final CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS = CrudeIncrementalIntIdentityHashBiMap.create(16);
	public static final EntityDataSerializer<Byte> BYTE = EntityDataSerializer.simple(
		(friendlyByteBuf, byte_) -> friendlyByteBuf.writeByte(byte_), FriendlyByteBuf::readByte
	);
	public static final EntityDataSerializer<Integer> INT = EntityDataSerializer.simple(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt);
	public static final EntityDataSerializer<Float> FLOAT = EntityDataSerializer.simple(FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat);
	public static final EntityDataSerializer<String> STRING = EntityDataSerializer.simple(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);
	public static final EntityDataSerializer<Component> COMPONENT = EntityDataSerializer.simple(FriendlyByteBuf::writeComponent, FriendlyByteBuf::readComponent);
	public static final EntityDataSerializer<Optional<Component>> OPTIONAL_COMPONENT = EntityDataSerializer.optional(
		FriendlyByteBuf::writeComponent, FriendlyByteBuf::readComponent
	);
	public static final EntityDataSerializer<ItemStack> ITEM_STACK = new EntityDataSerializer<ItemStack>() {
		public void write(FriendlyByteBuf friendlyByteBuf, ItemStack itemStack) {
			friendlyByteBuf.writeItem(itemStack);
		}

		public ItemStack read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readItem();
		}

		public ItemStack copy(ItemStack itemStack) {
			return itemStack.copy();
		}
	};
	public static final EntityDataSerializer<Optional<BlockState>> BLOCK_STATE = new EntityDataSerializer.ForValueType<Optional<BlockState>>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Optional<BlockState> optional) {
			if (optional.isPresent()) {
				friendlyByteBuf.writeVarInt(Block.getId((BlockState)optional.get()));
			} else {
				friendlyByteBuf.writeVarInt(0);
			}
		}

		public Optional<BlockState> read(FriendlyByteBuf friendlyByteBuf) {
			int i = friendlyByteBuf.readVarInt();
			return i == 0 ? Optional.empty() : Optional.of(Block.stateById(i));
		}
	};
	public static final EntityDataSerializer<Boolean> BOOLEAN = EntityDataSerializer.simple(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean);
	public static final EntityDataSerializer<ParticleOptions> PARTICLE = new EntityDataSerializer.ForValueType<ParticleOptions>() {
		public void write(FriendlyByteBuf friendlyByteBuf, ParticleOptions particleOptions) {
			friendlyByteBuf.writeId(Registry.PARTICLE_TYPE, particleOptions.getType());
			particleOptions.writeToNetwork(friendlyByteBuf);
		}

		public ParticleOptions read(FriendlyByteBuf friendlyByteBuf) {
			return this.readParticle(friendlyByteBuf, friendlyByteBuf.readById(Registry.PARTICLE_TYPE));
		}

		private <T extends ParticleOptions> T readParticle(FriendlyByteBuf friendlyByteBuf, ParticleType<T> particleType) {
			return particleType.getDeserializer().fromNetwork(particleType, friendlyByteBuf);
		}
	};
	public static final EntityDataSerializer<Rotations> ROTATIONS = new EntityDataSerializer.ForValueType<Rotations>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Rotations rotations) {
			friendlyByteBuf.writeFloat(rotations.getX());
			friendlyByteBuf.writeFloat(rotations.getY());
			friendlyByteBuf.writeFloat(rotations.getZ());
		}

		public Rotations read(FriendlyByteBuf friendlyByteBuf) {
			return new Rotations(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
		}
	};
	public static final EntityDataSerializer<BlockPos> BLOCK_POS = EntityDataSerializer.simple(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);
	public static final EntityDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = EntityDataSerializer.optional(
		FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos
	);
	public static final EntityDataSerializer<Direction> DIRECTION = EntityDataSerializer.simpleEnum(Direction.class);
	public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.optional(FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);
	public static final EntityDataSerializer<Optional<GlobalPos>> OPTIONAL_GLOBAL_POS = EntityDataSerializer.optional(
		FriendlyByteBuf::writeGlobalPos, FriendlyByteBuf::readGlobalPos
	);
	public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = new EntityDataSerializer<CompoundTag>() {
		public void write(FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag) {
			friendlyByteBuf.writeNbt(compoundTag);
		}

		public CompoundTag read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readNbt();
		}

		public CompoundTag copy(CompoundTag compoundTag) {
			return compoundTag.copy();
		}
	};
	public static final EntityDataSerializer<VillagerData> VILLAGER_DATA = new EntityDataSerializer.ForValueType<VillagerData>() {
		public void write(FriendlyByteBuf friendlyByteBuf, VillagerData villagerData) {
			friendlyByteBuf.writeId(Registry.VILLAGER_TYPE, villagerData.getType());
			friendlyByteBuf.writeId(Registry.VILLAGER_PROFESSION, villagerData.getProfession());
			friendlyByteBuf.writeVarInt(villagerData.getLevel());
		}

		public VillagerData read(FriendlyByteBuf friendlyByteBuf) {
			return new VillagerData(
				friendlyByteBuf.readById(Registry.VILLAGER_TYPE), friendlyByteBuf.readById(Registry.VILLAGER_PROFESSION), friendlyByteBuf.readVarInt()
			);
		}
	};
	public static final EntityDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = new EntityDataSerializer.ForValueType<OptionalInt>() {
		public void write(FriendlyByteBuf friendlyByteBuf, OptionalInt optionalInt) {
			friendlyByteBuf.writeVarInt(optionalInt.orElse(-1) + 1);
		}

		public OptionalInt read(FriendlyByteBuf friendlyByteBuf) {
			int i = friendlyByteBuf.readVarInt();
			return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
		}
	};
	public static final EntityDataSerializer<Pose> POSE = EntityDataSerializer.simpleEnum(Pose.class);
	public static final EntityDataSerializer<CatVariant> CAT_VARIANT = EntityDataSerializer.simpleId(Registry.CAT_VARIANT);
	public static final EntityDataSerializer<FrogVariant> FROG_VARIANT = EntityDataSerializer.simpleId(Registry.FROG_VARIANT);
	public static final EntityDataSerializer<Holder<PaintingVariant>> PAINTING_VARIANT = EntityDataSerializer.simpleId(Registry.PAINTING_VARIANT.asHolderIdMap());

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
		registerSerializer(COMPOUND_TAG);
		registerSerializer(PARTICLE);
		registerSerializer(VILLAGER_DATA);
		registerSerializer(OPTIONAL_UNSIGNED_INT);
		registerSerializer(POSE);
		registerSerializer(CAT_VARIANT);
		registerSerializer(FROG_VARIANT);
		registerSerializer(OPTIONAL_GLOBAL_POS);
		registerSerializer(PAINTING_VARIANT);
	}
}
