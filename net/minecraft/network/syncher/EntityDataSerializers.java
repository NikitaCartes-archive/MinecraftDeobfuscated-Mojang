/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.syncher;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityDataSerializers {
    private static final CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS = CrudeIncrementalIntIdentityHashBiMap.create(16);
    public static final EntityDataSerializer<Byte> BYTE = EntityDataSerializer.simple((friendlyByteBuf, byte_) -> friendlyByteBuf.writeByte(byte_.byteValue()), FriendlyByteBuf::readByte);
    public static final EntityDataSerializer<Integer> INT = EntityDataSerializer.simple(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt);
    public static final EntityDataSerializer<Long> LONG = EntityDataSerializer.simple(FriendlyByteBuf::writeVarLong, FriendlyByteBuf::readVarLong);
    public static final EntityDataSerializer<Float> FLOAT = EntityDataSerializer.simple(FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat);
    public static final EntityDataSerializer<String> STRING = EntityDataSerializer.simple(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);
    public static final EntityDataSerializer<Component> COMPONENT = EntityDataSerializer.simple(FriendlyByteBuf::writeComponent, FriendlyByteBuf::readComponent);
    public static final EntityDataSerializer<Optional<Component>> OPTIONAL_COMPONENT = EntityDataSerializer.optional(FriendlyByteBuf::writeComponent, FriendlyByteBuf::readComponent);
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
    public static final EntityDataSerializer<BlockState> BLOCK_STATE = EntityDataSerializer.simpleId(Block.BLOCK_STATE_REGISTRY);
    public static final EntityDataSerializer<Optional<BlockState>> OPTIONAL_BLOCK_STATE = new EntityDataSerializer.ForValueType<Optional<BlockState>>(){

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
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Boolean> BOOLEAN = EntityDataSerializer.simple(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean);
    public static final EntityDataSerializer<ParticleOptions> PARTICLE = new EntityDataSerializer.ForValueType<ParticleOptions>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, ParticleOptions particleOptions) {
            friendlyByteBuf.writeId(BuiltInRegistries.PARTICLE_TYPE, particleOptions.getType());
            particleOptions.writeToNetwork(friendlyByteBuf);
        }

        @Override
        public ParticleOptions read(FriendlyByteBuf friendlyByteBuf) {
            return this.readParticle(friendlyByteBuf, friendlyByteBuf.readById(BuiltInRegistries.PARTICLE_TYPE));
        }

        private <T extends ParticleOptions> T readParticle(FriendlyByteBuf friendlyByteBuf, ParticleType<T> particleType) {
            return particleType.getDeserializer().fromNetwork(particleType, friendlyByteBuf);
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Rotations> ROTATIONS = new EntityDataSerializer.ForValueType<Rotations>(){

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
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<BlockPos> BLOCK_POS = EntityDataSerializer.simple(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);
    public static final EntityDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = EntityDataSerializer.optional(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);
    public static final EntityDataSerializer<Direction> DIRECTION = EntityDataSerializer.simpleEnum(Direction.class);
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.optional(FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);
    public static final EntityDataSerializer<Optional<GlobalPos>> OPTIONAL_GLOBAL_POS = EntityDataSerializer.optional(FriendlyByteBuf::writeGlobalPos, FriendlyByteBuf::readGlobalPos);
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
    public static final EntityDataSerializer<VillagerData> VILLAGER_DATA = new EntityDataSerializer.ForValueType<VillagerData>(){

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, VillagerData villagerData) {
            friendlyByteBuf.writeId(BuiltInRegistries.VILLAGER_TYPE, villagerData.getType());
            friendlyByteBuf.writeId(BuiltInRegistries.VILLAGER_PROFESSION, villagerData.getProfession());
            friendlyByteBuf.writeVarInt(villagerData.getLevel());
        }

        @Override
        public VillagerData read(FriendlyByteBuf friendlyByteBuf) {
            return new VillagerData(friendlyByteBuf.readById(BuiltInRegistries.VILLAGER_TYPE), friendlyByteBuf.readById(BuiltInRegistries.VILLAGER_PROFESSION), friendlyByteBuf.readVarInt());
        }

        @Override
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = new EntityDataSerializer.ForValueType<OptionalInt>(){

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
        public /* synthetic */ Object read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    };
    public static final EntityDataSerializer<Pose> POSE = EntityDataSerializer.simpleEnum(Pose.class);
    public static final EntityDataSerializer<CatVariant> CAT_VARIANT = EntityDataSerializer.simpleId(BuiltInRegistries.CAT_VARIANT);
    public static final EntityDataSerializer<FrogVariant> FROG_VARIANT = EntityDataSerializer.simpleId(BuiltInRegistries.FROG_VARIANT);
    public static final EntityDataSerializer<Holder<PaintingVariant>> PAINTING_VARIANT = EntityDataSerializer.simpleId(BuiltInRegistries.PAINTING_VARIANT.asHolderIdMap());
    public static final EntityDataSerializer<Vector3f> VECTOR3 = EntityDataSerializer.simple(FriendlyByteBuf::writeVector3f, FriendlyByteBuf::readVector3f);
    public static final EntityDataSerializer<Quaternionf> QUATERNION = EntityDataSerializer.simple(FriendlyByteBuf::writeQuaternion, FriendlyByteBuf::readQuaternion);

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
        EntityDataSerializers.registerSerializer(LONG);
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
        EntityDataSerializers.registerSerializer(OPTIONAL_BLOCK_STATE);
        EntityDataSerializers.registerSerializer(COMPOUND_TAG);
        EntityDataSerializers.registerSerializer(PARTICLE);
        EntityDataSerializers.registerSerializer(VILLAGER_DATA);
        EntityDataSerializers.registerSerializer(OPTIONAL_UNSIGNED_INT);
        EntityDataSerializers.registerSerializer(POSE);
        EntityDataSerializers.registerSerializer(CAT_VARIANT);
        EntityDataSerializers.registerSerializer(FROG_VARIANT);
        EntityDataSerializers.registerSerializer(OPTIONAL_GLOBAL_POS);
        EntityDataSerializers.registerSerializer(PAINTING_VARIANT);
        EntityDataSerializers.registerSerializer(VECTOR3);
        EntityDataSerializers.registerSerializer(QUATERNION);
    }
}

