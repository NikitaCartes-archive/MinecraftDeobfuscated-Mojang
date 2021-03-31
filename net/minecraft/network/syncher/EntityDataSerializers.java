/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.syncher;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EntityDataSerializers {
    private static final CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS = new CrudeIncrementalIntIdentityHashBiMap(16);
    public static final EntityDataSerializer<Byte> BYTE = new EntityDataSerializer<Byte>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Byte byte_) {
            friendlyByteBuf.writeByte(byte_.byteValue());
        }

        @Override
        public Byte read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readByte();
        }

        @Override
        public Byte copy(Byte byte_) {
            return byte_;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Integer> INT = new EntityDataSerializer<Integer>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Integer integer) {
            friendlyByteBuf.writeVarInt(integer);
        }

        @Override
        public Integer read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readVarInt();
        }

        @Override
        public Integer copy(Integer integer) {
            return integer;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Float> FLOAT = new EntityDataSerializer<Float>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Float float_) {
            friendlyByteBuf.writeFloat(float_.floatValue());
        }

        @Override
        public Float read(FriendlyByteBuf friendlyByteBuf) {
            return Float.valueOf(friendlyByteBuf.readFloat());
        }

        @Override
        public Float copy(Float float_) {
            return float_;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<String> STRING = new EntityDataSerializer<String>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, String string) {
            friendlyByteBuf.writeUtf(string);
        }

        @Override
        public String read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readUtf();
        }

        @Override
        public String copy(String string) {
            return string;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Component> COMPONENT = new EntityDataSerializer<Component>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Component component) {
            friendlyByteBuf.writeComponent(component);
        }

        @Override
        public Component read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readComponent();
        }

        @Override
        public Component copy(Component component) {
            return component;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Optional<Component>> OPTIONAL_COMPONENT = new EntityDataSerializer<Optional<Component>>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Optional<Component> optional) {
            if (optional.isPresent()) {
                friendlyByteBuf.writeBoolean(true);
                friendlyByteBuf.writeComponent(optional.get());
            } else {
                friendlyByteBuf.writeBoolean(false);
            }
        }

        @Override
        public Optional<Component> read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readBoolean() ? Optional.of(friendlyByteBuf.readComponent()) : Optional.empty();
        }

        @Override
        public Optional<Component> copy(Optional<Component> optional) {
            return optional;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<ItemStack> ITEM_STACK = new EntityDataSerializer<ItemStack>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, ItemStack itemStack) {
            friendlyByteBuf.writeItem(itemStack);
        }

        @Override
        public ItemStack read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readItem();
        }

        @Override
        public ItemStack copy(ItemStack itemStack) {
            return itemStack.copy();
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Optional<BlockState>> BLOCK_STATE = new EntityDataSerializer<Optional<BlockState>>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Optional<BlockState> optional) {
            if (optional.isPresent()) {
                friendlyByteBuf.writeVarInt(Block.getId(optional.get()));
            } else {
                friendlyByteBuf.writeVarInt(0);
            }
        }

        @Override
        public Optional<BlockState> read(FriendlyByteBuf friendlyByteBuf) {
            int i = friendlyByteBuf.readVarInt();
            if (i == 0) {
                return Optional.empty();
            }
            return Optional.of(Block.stateById(i));
        }

        @Override
        public Optional<BlockState> copy(Optional<BlockState> optional) {
            return optional;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Boolean> BOOLEAN = new EntityDataSerializer<Boolean>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Boolean boolean_) {
            friendlyByteBuf.writeBoolean(boolean_);
        }

        @Override
        public Boolean read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readBoolean();
        }

        @Override
        public Boolean copy(Boolean boolean_) {
            return boolean_;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<ParticleOptions> PARTICLE = new EntityDataSerializer<ParticleOptions>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, ParticleOptions particleOptions) {
            friendlyByteBuf.writeVarInt(Registry.PARTICLE_TYPE.getId(particleOptions.getType()));
            particleOptions.writeToNetwork(friendlyByteBuf);
        }

        @Override
        public ParticleOptions read(FriendlyByteBuf friendlyByteBuf) {
            return this.readParticle(friendlyByteBuf, (ParticleType)Registry.PARTICLE_TYPE.byId(friendlyByteBuf.readVarInt()));
        }

        private <T extends ParticleOptions> T readParticle(FriendlyByteBuf friendlyByteBuf, ParticleType<T> particleType) {
            return particleType.getDeserializer().fromNetwork(particleType, friendlyByteBuf);
        }

        @Override
        public ParticleOptions copy(ParticleOptions particleOptions) {
            return particleOptions;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Rotations> ROTATIONS = new EntityDataSerializer<Rotations>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Rotations rotations) {
            friendlyByteBuf.writeFloat(rotations.getX());
            friendlyByteBuf.writeFloat(rotations.getY());
            friendlyByteBuf.writeFloat(rotations.getZ());
        }

        @Override
        public Rotations read(FriendlyByteBuf friendlyByteBuf) {
            return new Rotations(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
        }

        @Override
        public Rotations copy(Rotations rotations) {
            return rotations;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<BlockPos> BLOCK_POS = new EntityDataSerializer<BlockPos>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, BlockPos blockPos) {
            friendlyByteBuf.writeBlockPos(blockPos);
        }

        @Override
        public BlockPos read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readBlockPos();
        }

        @Override
        public BlockPos copy(BlockPos blockPos) {
            return blockPos;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = new EntityDataSerializer<Optional<BlockPos>>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Optional<BlockPos> optional) {
            friendlyByteBuf.writeBoolean(optional.isPresent());
            if (optional.isPresent()) {
                friendlyByteBuf.writeBlockPos(optional.get());
            }
        }

        @Override
        public Optional<BlockPos> read(FriendlyByteBuf friendlyByteBuf) {
            if (!friendlyByteBuf.readBoolean()) {
                return Optional.empty();
            }
            return Optional.of(friendlyByteBuf.readBlockPos());
        }

        @Override
        public Optional<BlockPos> copy(Optional<BlockPos> optional) {
            return optional;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Direction> DIRECTION = new EntityDataSerializer<Direction>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Direction direction) {
            friendlyByteBuf.writeEnum(direction);
        }

        @Override
        public Direction read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readEnum(Direction.class);
        }

        @Override
        public Direction copy(Direction direction) {
            return direction;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = new EntityDataSerializer<Optional<UUID>>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Optional<UUID> optional) {
            friendlyByteBuf.writeBoolean(optional.isPresent());
            if (optional.isPresent()) {
                friendlyByteBuf.writeUUID(optional.get());
            }
        }

        @Override
        public Optional<UUID> read(FriendlyByteBuf friendlyByteBuf) {
            if (!friendlyByteBuf.readBoolean()) {
                return Optional.empty();
            }
            return Optional.of(friendlyByteBuf.readUUID());
        }

        @Override
        public Optional<UUID> copy(Optional<UUID> optional) {
            return optional;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = new EntityDataSerializer<CompoundTag>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag) {
            friendlyByteBuf.writeNbt(compoundTag);
        }

        @Override
        public CompoundTag read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readNbt();
        }

        @Override
        public CompoundTag copy(CompoundTag compoundTag) {
            return compoundTag.copy();
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<VillagerData> VILLAGER_DATA = new EntityDataSerializer<VillagerData>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, VillagerData villagerData) {
            friendlyByteBuf.writeVarInt(Registry.VILLAGER_TYPE.getId(villagerData.getType()));
            friendlyByteBuf.writeVarInt(Registry.VILLAGER_PROFESSION.getId(villagerData.getProfession()));
            friendlyByteBuf.writeVarInt(villagerData.getLevel());
        }

        @Override
        public VillagerData read(FriendlyByteBuf friendlyByteBuf) {
            return new VillagerData(Registry.VILLAGER_TYPE.byId(friendlyByteBuf.readVarInt()), Registry.VILLAGER_PROFESSION.byId(friendlyByteBuf.readVarInt()), friendlyByteBuf.readVarInt());
        }

        @Override
        public VillagerData copy(VillagerData villagerData) {
            return villagerData;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = new EntityDataSerializer<OptionalInt>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, OptionalInt optionalInt) {
            friendlyByteBuf.writeVarInt(optionalInt.orElse(-1) + 1);
        }

        @Override
        public OptionalInt read(FriendlyByteBuf friendlyByteBuf) {
            int i = friendlyByteBuf.readVarInt();
            return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
        }

        @Override
        public OptionalInt copy(OptionalInt optionalInt) {
            return optionalInt;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Pose> POSE = new EntityDataSerializer<Pose>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, Pose pose) {
            friendlyByteBuf.writeEnum(pose);
        }

        @Override
        public Pose read(FriendlyByteBuf friendlyByteBuf) {
            return friendlyByteBuf.readEnum(Pose.class);
        }

        @Override
        public Pose copy(Pose pose) {
            return pose;
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
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

    private EntityDataSerializers() {
    }

    static {
        EntityDataSerializers.registerSerializer(BYTE);
        EntityDataSerializers.registerSerializer(INT);
        EntityDataSerializers.registerSerializer(FLOAT);
        EntityDataSerializers.registerSerializer(STRING);
        EntityDataSerializers.registerSerializer(COMPONENT);
        EntityDataSerializers.registerSerializer(OPTIONAL_COMPONENT);
        EntityDataSerializers.registerSerializer(ITEM_STACK);
        EntityDataSerializers.registerSerializer(BOOLEAN);
        EntityDataSerializers.registerSerializer(ROTATIONS);
        EntityDataSerializers.registerSerializer(BLOCK_POS);
        EntityDataSerializers.registerSerializer(OPTIONAL_BLOCK_POS);
        EntityDataSerializers.registerSerializer(DIRECTION);
        EntityDataSerializers.registerSerializer(OPTIONAL_UUID);
        EntityDataSerializers.registerSerializer(BLOCK_STATE);
        EntityDataSerializers.registerSerializer(COMPOUND_TAG);
        EntityDataSerializers.registerSerializer(PARTICLE);
        EntityDataSerializers.registerSerializer(VILLAGER_DATA);
        EntityDataSerializers.registerSerializer(OPTIONAL_UNSIGNED_INT);
        EntityDataSerializers.registerSerializer(POSE);
    }
}

