package net.minecraft.network.syncher;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EntityDataSerializers {
	private static final CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS = new CrudeIncrementalIntIdentityHashBiMap<>(16);
	public static final EntityDataSerializer<Byte> BYTE = new EntityDataSerializer<Byte>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Byte byte_) {
			friendlyByteBuf.writeByte(byte_);
		}

		public Byte read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readByte();
		}

		public Byte copy(Byte byte_) {
			return byte_;
		}
	};
	public static final EntityDataSerializer<Integer> INT = new EntityDataSerializer<Integer>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Integer integer) {
			friendlyByteBuf.writeVarInt(integer);
		}

		public Integer read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readVarInt();
		}

		public Integer copy(Integer integer) {
			return integer;
		}
	};
	public static final EntityDataSerializer<Float> FLOAT = new EntityDataSerializer<Float>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Float float_) {
			friendlyByteBuf.writeFloat(float_);
		}

		public Float read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readFloat();
		}

		public Float copy(Float float_) {
			return float_;
		}
	};
	public static final EntityDataSerializer<String> STRING = new EntityDataSerializer<String>() {
		public void write(FriendlyByteBuf friendlyByteBuf, String string) {
			friendlyByteBuf.writeUtf(string);
		}

		public String read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readUtf(32767);
		}

		public String copy(String string) {
			return string;
		}
	};
	public static final EntityDataSerializer<Component> COMPONENT = new EntityDataSerializer<Component>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Component component) {
			friendlyByteBuf.writeComponent(component);
		}

		public Component read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readComponent();
		}

		public Component copy(Component component) {
			return component;
		}
	};
	public static final EntityDataSerializer<Optional<Component>> OPTIONAL_COMPONENT = new EntityDataSerializer<Optional<Component>>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Optional<Component> optional) {
			if (optional.isPresent()) {
				friendlyByteBuf.writeBoolean(true);
				friendlyByteBuf.writeComponent((Component)optional.get());
			} else {
				friendlyByteBuf.writeBoolean(false);
			}
		}

		public Optional<Component> read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readBoolean() ? Optional.of(friendlyByteBuf.readComponent()) : Optional.empty();
		}

		public Optional<Component> copy(Optional<Component> optional) {
			return optional;
		}
	};
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
	public static final EntityDataSerializer<Optional<BlockState>> BLOCK_STATE = new EntityDataSerializer<Optional<BlockState>>() {
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

		public Optional<BlockState> copy(Optional<BlockState> optional) {
			return optional;
		}
	};
	public static final EntityDataSerializer<Boolean> BOOLEAN = new EntityDataSerializer<Boolean>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Boolean boolean_) {
			friendlyByteBuf.writeBoolean(boolean_);
		}

		public Boolean read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readBoolean();
		}

		public Boolean copy(Boolean boolean_) {
			return boolean_;
		}
	};
	public static final EntityDataSerializer<ParticleOptions> PARTICLE = new EntityDataSerializer<ParticleOptions>() {
		public void write(FriendlyByteBuf friendlyByteBuf, ParticleOptions particleOptions) {
			friendlyByteBuf.writeVarInt(Registry.PARTICLE_TYPE.getId(particleOptions.getType()));
			particleOptions.writeToNetwork(friendlyByteBuf);
		}

		public ParticleOptions read(FriendlyByteBuf friendlyByteBuf) {
			return this.readParticle(friendlyByteBuf, (ParticleType<ParticleOptions>)Registry.PARTICLE_TYPE.byId(friendlyByteBuf.readVarInt()));
		}

		private <T extends ParticleOptions> T readParticle(FriendlyByteBuf friendlyByteBuf, ParticleType<T> particleType) {
			return particleType.getDeserializer().fromNetwork(particleType, friendlyByteBuf);
		}

		public ParticleOptions copy(ParticleOptions particleOptions) {
			return particleOptions;
		}
	};
	public static final EntityDataSerializer<Rotations> ROTATIONS = new EntityDataSerializer<Rotations>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Rotations rotations) {
			friendlyByteBuf.writeFloat(rotations.getX());
			friendlyByteBuf.writeFloat(rotations.getY());
			friendlyByteBuf.writeFloat(rotations.getZ());
		}

		public Rotations read(FriendlyByteBuf friendlyByteBuf) {
			return new Rotations(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
		}

		public Rotations copy(Rotations rotations) {
			return rotations;
		}
	};
	public static final EntityDataSerializer<BlockPos> BLOCK_POS = new EntityDataSerializer<BlockPos>() {
		public void write(FriendlyByteBuf friendlyByteBuf, BlockPos blockPos) {
			friendlyByteBuf.writeBlockPos(blockPos);
		}

		public BlockPos read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readBlockPos();
		}

		public BlockPos copy(BlockPos blockPos) {
			return blockPos;
		}
	};
	public static final EntityDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = new EntityDataSerializer<Optional<BlockPos>>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Optional<BlockPos> optional) {
			friendlyByteBuf.writeBoolean(optional.isPresent());
			if (optional.isPresent()) {
				friendlyByteBuf.writeBlockPos((BlockPos)optional.get());
			}
		}

		public Optional<BlockPos> read(FriendlyByteBuf friendlyByteBuf) {
			return !friendlyByteBuf.readBoolean() ? Optional.empty() : Optional.of(friendlyByteBuf.readBlockPos());
		}

		public Optional<BlockPos> copy(Optional<BlockPos> optional) {
			return optional;
		}
	};
	public static final EntityDataSerializer<Direction> DIRECTION = new EntityDataSerializer<Direction>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Direction direction) {
			friendlyByteBuf.writeEnum(direction);
		}

		public Direction read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readEnum(Direction.class);
		}

		public Direction copy(Direction direction) {
			return direction;
		}
	};
	public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = new EntityDataSerializer<Optional<UUID>>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Optional<UUID> optional) {
			friendlyByteBuf.writeBoolean(optional.isPresent());
			if (optional.isPresent()) {
				friendlyByteBuf.writeUUID((UUID)optional.get());
			}
		}

		public Optional<UUID> read(FriendlyByteBuf friendlyByteBuf) {
			return !friendlyByteBuf.readBoolean() ? Optional.empty() : Optional.of(friendlyByteBuf.readUUID());
		}

		public Optional<UUID> copy(Optional<UUID> optional) {
			return optional;
		}
	};
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
	public static final EntityDataSerializer<VillagerData> VILLAGER_DATA = new EntityDataSerializer<VillagerData>() {
		public void write(FriendlyByteBuf friendlyByteBuf, VillagerData villagerData) {
			friendlyByteBuf.writeVarInt(Registry.VILLAGER_TYPE.getId(villagerData.getType()));
			friendlyByteBuf.writeVarInt(Registry.VILLAGER_PROFESSION.getId(villagerData.getProfession()));
			friendlyByteBuf.writeVarInt(villagerData.getLevel());
		}

		public VillagerData read(FriendlyByteBuf friendlyByteBuf) {
			return new VillagerData(
				Registry.VILLAGER_TYPE.byId(friendlyByteBuf.readVarInt()), Registry.VILLAGER_PROFESSION.byId(friendlyByteBuf.readVarInt()), friendlyByteBuf.readVarInt()
			);
		}

		public VillagerData copy(VillagerData villagerData) {
			return villagerData;
		}
	};
	public static final EntityDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = new EntityDataSerializer<OptionalInt>() {
		public void write(FriendlyByteBuf friendlyByteBuf, OptionalInt optionalInt) {
			friendlyByteBuf.writeVarInt(optionalInt.orElse(-1) + 1);
		}

		public OptionalInt read(FriendlyByteBuf friendlyByteBuf) {
			int i = friendlyByteBuf.readVarInt();
			return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
		}

		public OptionalInt copy(OptionalInt optionalInt) {
			return optionalInt;
		}
	};
	public static final EntityDataSerializer<Pose> POSE = new EntityDataSerializer<Pose>() {
		public void write(FriendlyByteBuf friendlyByteBuf, Pose pose) {
			friendlyByteBuf.writeEnum(pose);
		}

		public Pose read(FriendlyByteBuf friendlyByteBuf) {
			return friendlyByteBuf.readEnum(Pose.class);
		}

		public Pose copy(Pose pose) {
			return pose;
		}
	};

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
	}
}
