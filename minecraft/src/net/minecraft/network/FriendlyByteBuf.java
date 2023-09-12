package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FriendlyByteBuf extends ByteBuf {
	public static final int DEFAULT_NBT_QUOTA = 2097152;
	private final ByteBuf source;
	public static final short MAX_STRING_LENGTH = 32767;
	public static final int MAX_COMPONENT_STRING_LENGTH = 262144;
	private static final int PUBLIC_KEY_SIZE = 256;
	private static final int MAX_PUBLIC_KEY_HEADER_SIZE = 256;
	private static final int MAX_PUBLIC_KEY_LENGTH = 512;
	private static final Gson GSON = new Gson();

	public FriendlyByteBuf(ByteBuf byteBuf) {
		this.source = byteBuf;
	}

	@Deprecated
	public <T> T readWithCodecTrusted(DynamicOps<Tag> dynamicOps, Codec<T> codec) {
		return this.readWithCodec(dynamicOps, codec, NbtAccounter.unlimitedHeap());
	}

	@Deprecated
	public <T> T readWithCodec(DynamicOps<Tag> dynamicOps, Codec<T> codec, NbtAccounter nbtAccounter) {
		Tag tag = this.readNbt(nbtAccounter);
		return Util.getOrThrow(codec.parse(dynamicOps, tag), string -> new DecoderException("Failed to decode: " + string + " " + tag));
	}

	@Deprecated
	public <T> FriendlyByteBuf writeWithCodec(DynamicOps<Tag> dynamicOps, Codec<T> codec, T object) {
		Tag tag = Util.getOrThrow(codec.encodeStart(dynamicOps, object), string -> new EncoderException("Failed to encode: " + string + " " + object));
		this.writeNbt(tag);
		return this;
	}

	public <T> T readJsonWithCodec(Codec<T> codec) {
		JsonElement jsonElement = GsonHelper.fromJson(GSON, this.readUtf(), JsonElement.class);
		DataResult<T> dataResult = codec.parse(JsonOps.INSTANCE, jsonElement);
		return Util.getOrThrow(dataResult, string -> new DecoderException("Failed to decode json: " + string));
	}

	public <T> void writeJsonWithCodec(Codec<T> codec, T object) {
		DataResult<JsonElement> dataResult = codec.encodeStart(JsonOps.INSTANCE, object);
		this.writeUtf(GSON.toJson(Util.getOrThrow(dataResult, string -> new EncoderException("Failed to encode: " + string + " " + object))));
	}

	public <T> void writeId(IdMap<T> idMap, T object) {
		int i = idMap.getId(object);
		if (i == -1) {
			throw new IllegalArgumentException("Can't find id for '" + object + "' in map " + idMap);
		} else {
			this.writeVarInt(i);
		}
	}

	public <T> void writeId(IdMap<Holder<T>> idMap, Holder<T> holder, FriendlyByteBuf.Writer<T> writer) {
		switch (holder.kind()) {
			case REFERENCE:
				int i = idMap.getId(holder);
				if (i == -1) {
					throw new IllegalArgumentException("Can't find id for '" + holder.value() + "' in map " + idMap);
				}

				this.writeVarInt(i + 1);
				break;
			case DIRECT:
				this.writeVarInt(0);
				writer.accept(this, holder.value());
		}
	}

	@Nullable
	public <T> T readById(IdMap<T> idMap) {
		int i = this.readVarInt();
		return idMap.byId(i);
	}

	public <T> Holder<T> readById(IdMap<Holder<T>> idMap, FriendlyByteBuf.Reader<T> reader) {
		int i = this.readVarInt();
		if (i == 0) {
			return Holder.direct((T)reader.apply(this));
		} else {
			Holder<T> holder = idMap.byId(i - 1);
			if (holder == null) {
				throw new IllegalArgumentException("Can't find element with id " + i);
			} else {
				return holder;
			}
		}
	}

	public static <T> IntFunction<T> limitValue(IntFunction<T> intFunction, int i) {
		return j -> {
			if (j > i) {
				throw new DecoderException("Value " + j + " is larger than limit " + i);
			} else {
				return intFunction.apply(j);
			}
		};
	}

	public <T, C extends Collection<T>> C readCollection(IntFunction<C> intFunction, FriendlyByteBuf.Reader<T> reader) {
		int i = this.readVarInt();
		C collection = (C)intFunction.apply(i);

		for (int j = 0; j < i; j++) {
			collection.add(reader.apply(this));
		}

		return collection;
	}

	public <T> void writeCollection(Collection<T> collection, FriendlyByteBuf.Writer<T> writer) {
		this.writeVarInt(collection.size());

		for (T object : collection) {
			writer.accept(this, object);
		}
	}

	public <T> List<T> readList(FriendlyByteBuf.Reader<T> reader) {
		return this.readCollection(Lists::newArrayListWithCapacity, reader);
	}

	public IntList readIntIdList() {
		int i = this.readVarInt();
		IntList intList = new IntArrayList();

		for (int j = 0; j < i; j++) {
			intList.add(this.readVarInt());
		}

		return intList;
	}

	public void writeIntIdList(IntList intList) {
		this.writeVarInt(intList.size());
		intList.forEach(this::writeVarInt);
	}

	public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> intFunction, FriendlyByteBuf.Reader<K> reader, FriendlyByteBuf.Reader<V> reader2) {
		int i = this.readVarInt();
		M map = (M)intFunction.apply(i);

		for (int j = 0; j < i; j++) {
			K object = (K)reader.apply(this);
			V object2 = (V)reader2.apply(this);
			map.put(object, object2);
		}

		return map;
	}

	public <K, V> Map<K, V> readMap(FriendlyByteBuf.Reader<K> reader, FriendlyByteBuf.Reader<V> reader2) {
		return this.readMap(Maps::newHashMapWithExpectedSize, reader, reader2);
	}

	public <K, V> void writeMap(Map<K, V> map, FriendlyByteBuf.Writer<K> writer, FriendlyByteBuf.Writer<V> writer2) {
		this.writeVarInt(map.size());
		map.forEach((object, object2) -> {
			writer.accept(this, object);
			writer2.accept(this, object2);
		});
	}

	public void readWithCount(Consumer<FriendlyByteBuf> consumer) {
		int i = this.readVarInt();

		for (int j = 0; j < i; j++) {
			consumer.accept(this);
		}
	}

	public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumSet, Class<E> class_) {
		E[] enums = (E[])class_.getEnumConstants();
		BitSet bitSet = new BitSet(enums.length);

		for (int i = 0; i < enums.length; i++) {
			bitSet.set(i, enumSet.contains(enums[i]));
		}

		this.writeFixedBitSet(bitSet, enums.length);
	}

	public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> class_) {
		E[] enums = (E[])class_.getEnumConstants();
		BitSet bitSet = this.readFixedBitSet(enums.length);
		EnumSet<E> enumSet = EnumSet.noneOf(class_);

		for (int i = 0; i < enums.length; i++) {
			if (bitSet.get(i)) {
				enumSet.add(enums[i]);
			}
		}

		return enumSet;
	}

	public <T> void writeOptional(Optional<T> optional, FriendlyByteBuf.Writer<T> writer) {
		if (optional.isPresent()) {
			this.writeBoolean(true);
			writer.accept(this, optional.get());
		} else {
			this.writeBoolean(false);
		}
	}

	public <T> Optional<T> readOptional(FriendlyByteBuf.Reader<T> reader) {
		return this.readBoolean() ? Optional.of(reader.apply(this)) : Optional.empty();
	}

	@Nullable
	public <T> T readNullable(FriendlyByteBuf.Reader<T> reader) {
		return (T)(this.readBoolean() ? reader.apply(this) : null);
	}

	public <T> void writeNullable(@Nullable T object, FriendlyByteBuf.Writer<T> writer) {
		if (object != null) {
			this.writeBoolean(true);
			writer.accept(this, object);
		} else {
			this.writeBoolean(false);
		}
	}

	public <L, R> void writeEither(Either<L, R> either, FriendlyByteBuf.Writer<L> writer, FriendlyByteBuf.Writer<R> writer2) {
		either.ifLeft(object -> {
			this.writeBoolean(true);
			writer.accept(this, object);
		}).ifRight(object -> {
			this.writeBoolean(false);
			writer2.accept(this, object);
		});
	}

	public <L, R> Either<L, R> readEither(FriendlyByteBuf.Reader<L> reader, FriendlyByteBuf.Reader<R> reader2) {
		return this.readBoolean() ? Either.left((L)reader.apply(this)) : Either.right((R)reader2.apply(this));
	}

	public byte[] readByteArray() {
		return this.readByteArray(this.readableBytes());
	}

	public FriendlyByteBuf writeByteArray(byte[] bs) {
		this.writeVarInt(bs.length);
		this.writeBytes(bs);
		return this;
	}

	public byte[] readByteArray(int i) {
		int j = this.readVarInt();
		if (j > i) {
			throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + i);
		} else {
			byte[] bs = new byte[j];
			this.readBytes(bs);
			return bs;
		}
	}

	public FriendlyByteBuf writeVarIntArray(int[] is) {
		this.writeVarInt(is.length);

		for (int i : is) {
			this.writeVarInt(i);
		}

		return this;
	}

	public int[] readVarIntArray() {
		return this.readVarIntArray(this.readableBytes());
	}

	public int[] readVarIntArray(int i) {
		int j = this.readVarInt();
		if (j > i) {
			throw new DecoderException("VarIntArray with size " + j + " is bigger than allowed " + i);
		} else {
			int[] is = new int[j];

			for (int k = 0; k < is.length; k++) {
				is[k] = this.readVarInt();
			}

			return is;
		}
	}

	public FriendlyByteBuf writeLongArray(long[] ls) {
		this.writeVarInt(ls.length);

		for (long l : ls) {
			this.writeLong(l);
		}

		return this;
	}

	public long[] readLongArray() {
		return this.readLongArray(null);
	}

	public long[] readLongArray(@Nullable long[] ls) {
		return this.readLongArray(ls, this.readableBytes() / 8);
	}

	public long[] readLongArray(@Nullable long[] ls, int i) {
		int j = this.readVarInt();
		if (ls == null || ls.length != j) {
			if (j > i) {
				throw new DecoderException("LongArray with size " + j + " is bigger than allowed " + i);
			}

			ls = new long[j];
		}

		for (int k = 0; k < ls.length; k++) {
			ls[k] = this.readLong();
		}

		return ls;
	}

	public BlockPos readBlockPos() {
		return BlockPos.of(this.readLong());
	}

	public FriendlyByteBuf writeBlockPos(BlockPos blockPos) {
		this.writeLong(blockPos.asLong());
		return this;
	}

	public ChunkPos readChunkPos() {
		return new ChunkPos(this.readLong());
	}

	public FriendlyByteBuf writeChunkPos(ChunkPos chunkPos) {
		this.writeLong(chunkPos.toLong());
		return this;
	}

	public SectionPos readSectionPos() {
		return SectionPos.of(this.readLong());
	}

	public FriendlyByteBuf writeSectionPos(SectionPos sectionPos) {
		this.writeLong(sectionPos.asLong());
		return this;
	}

	public GlobalPos readGlobalPos() {
		ResourceKey<Level> resourceKey = this.readResourceKey(Registries.DIMENSION);
		BlockPos blockPos = this.readBlockPos();
		return GlobalPos.of(resourceKey, blockPos);
	}

	public void writeGlobalPos(GlobalPos globalPos) {
		this.writeResourceKey(globalPos.dimension());
		this.writeBlockPos(globalPos.pos());
	}

	public Vector3f readVector3f() {
		return new Vector3f(this.readFloat(), this.readFloat(), this.readFloat());
	}

	public void writeVector3f(Vector3f vector3f) {
		this.writeFloat(vector3f.x());
		this.writeFloat(vector3f.y());
		this.writeFloat(vector3f.z());
	}

	public Quaternionf readQuaternion() {
		return new Quaternionf(this.readFloat(), this.readFloat(), this.readFloat(), this.readFloat());
	}

	public void writeQuaternion(Quaternionf quaternionf) {
		this.writeFloat(quaternionf.x);
		this.writeFloat(quaternionf.y);
		this.writeFloat(quaternionf.z);
		this.writeFloat(quaternionf.w);
	}

	public Vec3 readVec3() {
		return new Vec3(this.readDouble(), this.readDouble(), this.readDouble());
	}

	public void writeVec3(Vec3 vec3) {
		this.writeDouble(vec3.x());
		this.writeDouble(vec3.y());
		this.writeDouble(vec3.z());
	}

	public Component readComponent() {
		Component component = Component.Serializer.fromJson(this.readUtf(262144));
		if (component == null) {
			throw new DecoderException("Received unexpected null component");
		} else {
			return component;
		}
	}

	public FriendlyByteBuf writeComponent(Component component) {
		return this.writeUtf(Component.Serializer.toJson(component), 262144);
	}

	public <T extends Enum<T>> T readEnum(Class<T> class_) {
		return (T)class_.getEnumConstants()[this.readVarInt()];
	}

	public FriendlyByteBuf writeEnum(Enum<?> enum_) {
		return this.writeVarInt(enum_.ordinal());
	}

	public <T> T readById(IntFunction<T> intFunction) {
		int i = this.readVarInt();
		return (T)intFunction.apply(i);
	}

	public <T> FriendlyByteBuf writeById(ToIntFunction<T> toIntFunction, T object) {
		int i = toIntFunction.applyAsInt(object);
		return this.writeVarInt(i);
	}

	public int readVarInt() {
		return VarInt.read(this.source);
	}

	public long readVarLong() {
		return VarLong.read(this.source);
	}

	public FriendlyByteBuf writeUUID(UUID uUID) {
		this.writeLong(uUID.getMostSignificantBits());
		this.writeLong(uUID.getLeastSignificantBits());
		return this;
	}

	public UUID readUUID() {
		return new UUID(this.readLong(), this.readLong());
	}

	public FriendlyByteBuf writeVarInt(int i) {
		VarInt.write(this.source, i);
		return this;
	}

	public FriendlyByteBuf writeVarLong(long l) {
		VarLong.write(this.source, l);
		return this;
	}

	public FriendlyByteBuf writeNbt(@Nullable Tag tag) {
		if (tag == null) {
			tag = EndTag.INSTANCE;
		}

		try {
			NbtIo.writeAnyTag(tag, new ByteBufOutputStream(this));
			return this;
		} catch (IOException var3) {
			throw new EncoderException(var3);
		}
	}

	@Nullable
	public CompoundTag readNbt() {
		Tag tag = this.readNbt(NbtAccounter.create(2097152L));
		if (tag != null && !(tag instanceof CompoundTag)) {
			throw new DecoderException("Not a compound tag: " + tag);
		} else {
			return (CompoundTag)tag;
		}
	}

	@Nullable
	public Tag readNbt(NbtAccounter nbtAccounter) {
		try {
			Tag tag = NbtIo.readAnyTag(new ByteBufInputStream(this), nbtAccounter);
			return tag.getId() == 0 ? null : tag;
		} catch (IOException var3) {
			throw new EncoderException(var3);
		}
	}

	public FriendlyByteBuf writeItem(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			this.writeBoolean(false);
		} else {
			this.writeBoolean(true);
			Item item = itemStack.getItem();
			this.writeId(BuiltInRegistries.ITEM, item);
			this.writeByte(itemStack.getCount());
			CompoundTag compoundTag = null;
			if (item.canBeDepleted() || item.shouldOverrideMultiplayerNbt()) {
				compoundTag = itemStack.getTag();
			}

			this.writeNbt(compoundTag);
		}

		return this;
	}

	public ItemStack readItem() {
		if (!this.readBoolean()) {
			return ItemStack.EMPTY;
		} else {
			Item item = this.readById(BuiltInRegistries.ITEM);
			int i = this.readByte();
			ItemStack itemStack = new ItemStack(item, i);
			itemStack.setTag(this.readNbt());
			return itemStack;
		}
	}

	public String readUtf() {
		return this.readUtf(32767);
	}

	public String readUtf(int i) {
		return Utf8String.read(this.source, i);
	}

	public FriendlyByteBuf writeUtf(String string) {
		return this.writeUtf(string, 32767);
	}

	public FriendlyByteBuf writeUtf(String string, int i) {
		Utf8String.write(this.source, string, i);
		return this;
	}

	public ResourceLocation readResourceLocation() {
		return new ResourceLocation(this.readUtf(32767));
	}

	public FriendlyByteBuf writeResourceLocation(ResourceLocation resourceLocation) {
		this.writeUtf(resourceLocation.toString());
		return this;
	}

	public <T> ResourceKey<T> readResourceKey(ResourceKey<? extends Registry<T>> resourceKey) {
		ResourceLocation resourceLocation = this.readResourceLocation();
		return ResourceKey.create(resourceKey, resourceLocation);
	}

	public void writeResourceKey(ResourceKey<?> resourceKey) {
		this.writeResourceLocation(resourceKey.location());
	}

	public <T> ResourceKey<? extends Registry<T>> readRegistryKey() {
		ResourceLocation resourceLocation = this.readResourceLocation();
		return ResourceKey.createRegistryKey(resourceLocation);
	}

	public Date readDate() {
		return new Date(this.readLong());
	}

	public FriendlyByteBuf writeDate(Date date) {
		this.writeLong(date.getTime());
		return this;
	}

	public Instant readInstant() {
		return Instant.ofEpochMilli(this.readLong());
	}

	public void writeInstant(Instant instant) {
		this.writeLong(instant.toEpochMilli());
	}

	public PublicKey readPublicKey() {
		try {
			return Crypt.byteToPublicKey(this.readByteArray(512));
		} catch (CryptException var2) {
			throw new DecoderException("Malformed public key bytes", var2);
		}
	}

	public FriendlyByteBuf writePublicKey(PublicKey publicKey) {
		this.writeByteArray(publicKey.getEncoded());
		return this;
	}

	public BlockHitResult readBlockHitResult() {
		BlockPos blockPos = this.readBlockPos();
		Direction direction = this.readEnum(Direction.class);
		float f = this.readFloat();
		float g = this.readFloat();
		float h = this.readFloat();
		boolean bl = this.readBoolean();
		return new BlockHitResult(
			new Vec3((double)blockPos.getX() + (double)f, (double)blockPos.getY() + (double)g, (double)blockPos.getZ() + (double)h), direction, blockPos, bl
		);
	}

	public void writeBlockHitResult(BlockHitResult blockHitResult) {
		BlockPos blockPos = blockHitResult.getBlockPos();
		this.writeBlockPos(blockPos);
		this.writeEnum(blockHitResult.getDirection());
		Vec3 vec3 = blockHitResult.getLocation();
		this.writeFloat((float)(vec3.x - (double)blockPos.getX()));
		this.writeFloat((float)(vec3.y - (double)blockPos.getY()));
		this.writeFloat((float)(vec3.z - (double)blockPos.getZ()));
		this.writeBoolean(blockHitResult.isInside());
	}

	public BitSet readBitSet() {
		return BitSet.valueOf(this.readLongArray());
	}

	public void writeBitSet(BitSet bitSet) {
		this.writeLongArray(bitSet.toLongArray());
	}

	public BitSet readFixedBitSet(int i) {
		byte[] bs = new byte[Mth.positiveCeilDiv(i, 8)];
		this.readBytes(bs);
		return BitSet.valueOf(bs);
	}

	public void writeFixedBitSet(BitSet bitSet, int i) {
		if (bitSet.length() > i) {
			throw new EncoderException("BitSet is larger than expected size (" + bitSet.length() + ">" + i + ")");
		} else {
			byte[] bs = bitSet.toByteArray();
			this.writeBytes(Arrays.copyOf(bs, Mth.positiveCeilDiv(i, 8)));
		}
	}

	public GameProfile readGameProfile() {
		UUID uUID = this.readUUID();
		String string = this.readUtf(16);
		GameProfile gameProfile = new GameProfile(uUID, string);
		gameProfile.getProperties().putAll(this.readGameProfileProperties());
		return gameProfile;
	}

	public void writeGameProfile(GameProfile gameProfile) {
		this.writeUUID(gameProfile.getId());
		this.writeUtf(gameProfile.getName());
		this.writeGameProfileProperties(gameProfile.getProperties());
	}

	public PropertyMap readGameProfileProperties() {
		PropertyMap propertyMap = new PropertyMap();
		this.readWithCount(friendlyByteBuf -> {
			Property property = this.readProperty();
			propertyMap.put(property.name(), property);
		});
		return propertyMap;
	}

	public void writeGameProfileProperties(PropertyMap propertyMap) {
		this.writeCollection(propertyMap.values(), FriendlyByteBuf::writeProperty);
	}

	public Property readProperty() {
		String string = this.readUtf();
		String string2 = this.readUtf();
		String string3 = this.readNullable(FriendlyByteBuf::readUtf);
		return new Property(string, string2, string3);
	}

	public void writeProperty(Property property) {
		this.writeUtf(property.name());
		this.writeUtf(property.value());
		this.writeNullable(property.signature(), FriendlyByteBuf::writeUtf);
	}

	@Override
	public boolean isContiguous() {
		return this.source.isContiguous();
	}

	@Override
	public int maxFastWritableBytes() {
		return this.source.maxFastWritableBytes();
	}

	@Override
	public int capacity() {
		return this.source.capacity();
	}

	public FriendlyByteBuf capacity(int i) {
		this.source.capacity(i);
		return this;
	}

	@Override
	public int maxCapacity() {
		return this.source.maxCapacity();
	}

	@Override
	public ByteBufAllocator alloc() {
		return this.source.alloc();
	}

	@Override
	public ByteOrder order() {
		return this.source.order();
	}

	@Override
	public ByteBuf order(ByteOrder byteOrder) {
		return this.source.order(byteOrder);
	}

	@Override
	public ByteBuf unwrap() {
		return this.source;
	}

	@Override
	public boolean isDirect() {
		return this.source.isDirect();
	}

	@Override
	public boolean isReadOnly() {
		return this.source.isReadOnly();
	}

	@Override
	public ByteBuf asReadOnly() {
		return this.source.asReadOnly();
	}

	@Override
	public int readerIndex() {
		return this.source.readerIndex();
	}

	public FriendlyByteBuf readerIndex(int i) {
		this.source.readerIndex(i);
		return this;
	}

	@Override
	public int writerIndex() {
		return this.source.writerIndex();
	}

	public FriendlyByteBuf writerIndex(int i) {
		this.source.writerIndex(i);
		return this;
	}

	public FriendlyByteBuf setIndex(int i, int j) {
		this.source.setIndex(i, j);
		return this;
	}

	@Override
	public int readableBytes() {
		return this.source.readableBytes();
	}

	@Override
	public int writableBytes() {
		return this.source.writableBytes();
	}

	@Override
	public int maxWritableBytes() {
		return this.source.maxWritableBytes();
	}

	@Override
	public boolean isReadable() {
		return this.source.isReadable();
	}

	@Override
	public boolean isReadable(int i) {
		return this.source.isReadable(i);
	}

	@Override
	public boolean isWritable() {
		return this.source.isWritable();
	}

	@Override
	public boolean isWritable(int i) {
		return this.source.isWritable(i);
	}

	public FriendlyByteBuf clear() {
		this.source.clear();
		return this;
	}

	public FriendlyByteBuf markReaderIndex() {
		this.source.markReaderIndex();
		return this;
	}

	public FriendlyByteBuf resetReaderIndex() {
		this.source.resetReaderIndex();
		return this;
	}

	public FriendlyByteBuf markWriterIndex() {
		this.source.markWriterIndex();
		return this;
	}

	public FriendlyByteBuf resetWriterIndex() {
		this.source.resetWriterIndex();
		return this;
	}

	public FriendlyByteBuf discardReadBytes() {
		this.source.discardReadBytes();
		return this;
	}

	public FriendlyByteBuf discardSomeReadBytes() {
		this.source.discardSomeReadBytes();
		return this;
	}

	public FriendlyByteBuf ensureWritable(int i) {
		this.source.ensureWritable(i);
		return this;
	}

	@Override
	public int ensureWritable(int i, boolean bl) {
		return this.source.ensureWritable(i, bl);
	}

	@Override
	public boolean getBoolean(int i) {
		return this.source.getBoolean(i);
	}

	@Override
	public byte getByte(int i) {
		return this.source.getByte(i);
	}

	@Override
	public short getUnsignedByte(int i) {
		return this.source.getUnsignedByte(i);
	}

	@Override
	public short getShort(int i) {
		return this.source.getShort(i);
	}

	@Override
	public short getShortLE(int i) {
		return this.source.getShortLE(i);
	}

	@Override
	public int getUnsignedShort(int i) {
		return this.source.getUnsignedShort(i);
	}

	@Override
	public int getUnsignedShortLE(int i) {
		return this.source.getUnsignedShortLE(i);
	}

	@Override
	public int getMedium(int i) {
		return this.source.getMedium(i);
	}

	@Override
	public int getMediumLE(int i) {
		return this.source.getMediumLE(i);
	}

	@Override
	public int getUnsignedMedium(int i) {
		return this.source.getUnsignedMedium(i);
	}

	@Override
	public int getUnsignedMediumLE(int i) {
		return this.source.getUnsignedMediumLE(i);
	}

	@Override
	public int getInt(int i) {
		return this.source.getInt(i);
	}

	@Override
	public int getIntLE(int i) {
		return this.source.getIntLE(i);
	}

	@Override
	public long getUnsignedInt(int i) {
		return this.source.getUnsignedInt(i);
	}

	@Override
	public long getUnsignedIntLE(int i) {
		return this.source.getUnsignedIntLE(i);
	}

	@Override
	public long getLong(int i) {
		return this.source.getLong(i);
	}

	@Override
	public long getLongLE(int i) {
		return this.source.getLongLE(i);
	}

	@Override
	public char getChar(int i) {
		return this.source.getChar(i);
	}

	@Override
	public float getFloat(int i) {
		return this.source.getFloat(i);
	}

	@Override
	public double getDouble(int i) {
		return this.source.getDouble(i);
	}

	public FriendlyByteBuf getBytes(int i, ByteBuf byteBuf) {
		this.source.getBytes(i, byteBuf);
		return this;
	}

	public FriendlyByteBuf getBytes(int i, ByteBuf byteBuf, int j) {
		this.source.getBytes(i, byteBuf, j);
		return this;
	}

	public FriendlyByteBuf getBytes(int i, ByteBuf byteBuf, int j, int k) {
		this.source.getBytes(i, byteBuf, j, k);
		return this;
	}

	public FriendlyByteBuf getBytes(int i, byte[] bs) {
		this.source.getBytes(i, bs);
		return this;
	}

	public FriendlyByteBuf getBytes(int i, byte[] bs, int j, int k) {
		this.source.getBytes(i, bs, j, k);
		return this;
	}

	public FriendlyByteBuf getBytes(int i, ByteBuffer byteBuffer) {
		this.source.getBytes(i, byteBuffer);
		return this;
	}

	public FriendlyByteBuf getBytes(int i, OutputStream outputStream, int j) throws IOException {
		this.source.getBytes(i, outputStream, j);
		return this;
	}

	@Override
	public int getBytes(int i, GatheringByteChannel gatheringByteChannel, int j) throws IOException {
		return this.source.getBytes(i, gatheringByteChannel, j);
	}

	@Override
	public int getBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
		return this.source.getBytes(i, fileChannel, l, j);
	}

	@Override
	public CharSequence getCharSequence(int i, int j, Charset charset) {
		return this.source.getCharSequence(i, j, charset);
	}

	public FriendlyByteBuf setBoolean(int i, boolean bl) {
		this.source.setBoolean(i, bl);
		return this;
	}

	public FriendlyByteBuf setByte(int i, int j) {
		this.source.setByte(i, j);
		return this;
	}

	public FriendlyByteBuf setShort(int i, int j) {
		this.source.setShort(i, j);
		return this;
	}

	public FriendlyByteBuf setShortLE(int i, int j) {
		this.source.setShortLE(i, j);
		return this;
	}

	public FriendlyByteBuf setMedium(int i, int j) {
		this.source.setMedium(i, j);
		return this;
	}

	public FriendlyByteBuf setMediumLE(int i, int j) {
		this.source.setMediumLE(i, j);
		return this;
	}

	public FriendlyByteBuf setInt(int i, int j) {
		this.source.setInt(i, j);
		return this;
	}

	public FriendlyByteBuf setIntLE(int i, int j) {
		this.source.setIntLE(i, j);
		return this;
	}

	public FriendlyByteBuf setLong(int i, long l) {
		this.source.setLong(i, l);
		return this;
	}

	public FriendlyByteBuf setLongLE(int i, long l) {
		this.source.setLongLE(i, l);
		return this;
	}

	public FriendlyByteBuf setChar(int i, int j) {
		this.source.setChar(i, j);
		return this;
	}

	public FriendlyByteBuf setFloat(int i, float f) {
		this.source.setFloat(i, f);
		return this;
	}

	public FriendlyByteBuf setDouble(int i, double d) {
		this.source.setDouble(i, d);
		return this;
	}

	public FriendlyByteBuf setBytes(int i, ByteBuf byteBuf) {
		this.source.setBytes(i, byteBuf);
		return this;
	}

	public FriendlyByteBuf setBytes(int i, ByteBuf byteBuf, int j) {
		this.source.setBytes(i, byteBuf, j);
		return this;
	}

	public FriendlyByteBuf setBytes(int i, ByteBuf byteBuf, int j, int k) {
		this.source.setBytes(i, byteBuf, j, k);
		return this;
	}

	public FriendlyByteBuf setBytes(int i, byte[] bs) {
		this.source.setBytes(i, bs);
		return this;
	}

	public FriendlyByteBuf setBytes(int i, byte[] bs, int j, int k) {
		this.source.setBytes(i, bs, j, k);
		return this;
	}

	public FriendlyByteBuf setBytes(int i, ByteBuffer byteBuffer) {
		this.source.setBytes(i, byteBuffer);
		return this;
	}

	@Override
	public int setBytes(int i, InputStream inputStream, int j) throws IOException {
		return this.source.setBytes(i, inputStream, j);
	}

	@Override
	public int setBytes(int i, ScatteringByteChannel scatteringByteChannel, int j) throws IOException {
		return this.source.setBytes(i, scatteringByteChannel, j);
	}

	@Override
	public int setBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
		return this.source.setBytes(i, fileChannel, l, j);
	}

	public FriendlyByteBuf setZero(int i, int j) {
		this.source.setZero(i, j);
		return this;
	}

	@Override
	public int setCharSequence(int i, CharSequence charSequence, Charset charset) {
		return this.source.setCharSequence(i, charSequence, charset);
	}

	@Override
	public boolean readBoolean() {
		return this.source.readBoolean();
	}

	@Override
	public byte readByte() {
		return this.source.readByte();
	}

	@Override
	public short readUnsignedByte() {
		return this.source.readUnsignedByte();
	}

	@Override
	public short readShort() {
		return this.source.readShort();
	}

	@Override
	public short readShortLE() {
		return this.source.readShortLE();
	}

	@Override
	public int readUnsignedShort() {
		return this.source.readUnsignedShort();
	}

	@Override
	public int readUnsignedShortLE() {
		return this.source.readUnsignedShortLE();
	}

	@Override
	public int readMedium() {
		return this.source.readMedium();
	}

	@Override
	public int readMediumLE() {
		return this.source.readMediumLE();
	}

	@Override
	public int readUnsignedMedium() {
		return this.source.readUnsignedMedium();
	}

	@Override
	public int readUnsignedMediumLE() {
		return this.source.readUnsignedMediumLE();
	}

	@Override
	public int readInt() {
		return this.source.readInt();
	}

	@Override
	public int readIntLE() {
		return this.source.readIntLE();
	}

	@Override
	public long readUnsignedInt() {
		return this.source.readUnsignedInt();
	}

	@Override
	public long readUnsignedIntLE() {
		return this.source.readUnsignedIntLE();
	}

	@Override
	public long readLong() {
		return this.source.readLong();
	}

	@Override
	public long readLongLE() {
		return this.source.readLongLE();
	}

	@Override
	public char readChar() {
		return this.source.readChar();
	}

	@Override
	public float readFloat() {
		return this.source.readFloat();
	}

	@Override
	public double readDouble() {
		return this.source.readDouble();
	}

	@Override
	public ByteBuf readBytes(int i) {
		return this.source.readBytes(i);
	}

	@Override
	public ByteBuf readSlice(int i) {
		return this.source.readSlice(i);
	}

	@Override
	public ByteBuf readRetainedSlice(int i) {
		return this.source.readRetainedSlice(i);
	}

	public FriendlyByteBuf readBytes(ByteBuf byteBuf) {
		this.source.readBytes(byteBuf);
		return this;
	}

	public FriendlyByteBuf readBytes(ByteBuf byteBuf, int i) {
		this.source.readBytes(byteBuf, i);
		return this;
	}

	public FriendlyByteBuf readBytes(ByteBuf byteBuf, int i, int j) {
		this.source.readBytes(byteBuf, i, j);
		return this;
	}

	public FriendlyByteBuf readBytes(byte[] bs) {
		this.source.readBytes(bs);
		return this;
	}

	public FriendlyByteBuf readBytes(byte[] bs, int i, int j) {
		this.source.readBytes(bs, i, j);
		return this;
	}

	public FriendlyByteBuf readBytes(ByteBuffer byteBuffer) {
		this.source.readBytes(byteBuffer);
		return this;
	}

	public FriendlyByteBuf readBytes(OutputStream outputStream, int i) throws IOException {
		this.source.readBytes(outputStream, i);
		return this;
	}

	@Override
	public int readBytes(GatheringByteChannel gatheringByteChannel, int i) throws IOException {
		return this.source.readBytes(gatheringByteChannel, i);
	}

	@Override
	public CharSequence readCharSequence(int i, Charset charset) {
		return this.source.readCharSequence(i, charset);
	}

	@Override
	public int readBytes(FileChannel fileChannel, long l, int i) throws IOException {
		return this.source.readBytes(fileChannel, l, i);
	}

	public FriendlyByteBuf skipBytes(int i) {
		this.source.skipBytes(i);
		return this;
	}

	public FriendlyByteBuf writeBoolean(boolean bl) {
		this.source.writeBoolean(bl);
		return this;
	}

	public FriendlyByteBuf writeByte(int i) {
		this.source.writeByte(i);
		return this;
	}

	public FriendlyByteBuf writeShort(int i) {
		this.source.writeShort(i);
		return this;
	}

	public FriendlyByteBuf writeShortLE(int i) {
		this.source.writeShortLE(i);
		return this;
	}

	public FriendlyByteBuf writeMedium(int i) {
		this.source.writeMedium(i);
		return this;
	}

	public FriendlyByteBuf writeMediumLE(int i) {
		this.source.writeMediumLE(i);
		return this;
	}

	public FriendlyByteBuf writeInt(int i) {
		this.source.writeInt(i);
		return this;
	}

	public FriendlyByteBuf writeIntLE(int i) {
		this.source.writeIntLE(i);
		return this;
	}

	public FriendlyByteBuf writeLong(long l) {
		this.source.writeLong(l);
		return this;
	}

	public FriendlyByteBuf writeLongLE(long l) {
		this.source.writeLongLE(l);
		return this;
	}

	public FriendlyByteBuf writeChar(int i) {
		this.source.writeChar(i);
		return this;
	}

	public FriendlyByteBuf writeFloat(float f) {
		this.source.writeFloat(f);
		return this;
	}

	public FriendlyByteBuf writeDouble(double d) {
		this.source.writeDouble(d);
		return this;
	}

	public FriendlyByteBuf writeBytes(ByteBuf byteBuf) {
		this.source.writeBytes(byteBuf);
		return this;
	}

	public FriendlyByteBuf writeBytes(ByteBuf byteBuf, int i) {
		this.source.writeBytes(byteBuf, i);
		return this;
	}

	public FriendlyByteBuf writeBytes(ByteBuf byteBuf, int i, int j) {
		this.source.writeBytes(byteBuf, i, j);
		return this;
	}

	public FriendlyByteBuf writeBytes(byte[] bs) {
		this.source.writeBytes(bs);
		return this;
	}

	public FriendlyByteBuf writeBytes(byte[] bs, int i, int j) {
		this.source.writeBytes(bs, i, j);
		return this;
	}

	public FriendlyByteBuf writeBytes(ByteBuffer byteBuffer) {
		this.source.writeBytes(byteBuffer);
		return this;
	}

	@Override
	public int writeBytes(InputStream inputStream, int i) throws IOException {
		return this.source.writeBytes(inputStream, i);
	}

	@Override
	public int writeBytes(ScatteringByteChannel scatteringByteChannel, int i) throws IOException {
		return this.source.writeBytes(scatteringByteChannel, i);
	}

	@Override
	public int writeBytes(FileChannel fileChannel, long l, int i) throws IOException {
		return this.source.writeBytes(fileChannel, l, i);
	}

	public FriendlyByteBuf writeZero(int i) {
		this.source.writeZero(i);
		return this;
	}

	@Override
	public int writeCharSequence(CharSequence charSequence, Charset charset) {
		return this.source.writeCharSequence(charSequence, charset);
	}

	@Override
	public int indexOf(int i, int j, byte b) {
		return this.source.indexOf(i, j, b);
	}

	@Override
	public int bytesBefore(byte b) {
		return this.source.bytesBefore(b);
	}

	@Override
	public int bytesBefore(int i, byte b) {
		return this.source.bytesBefore(i, b);
	}

	@Override
	public int bytesBefore(int i, int j, byte b) {
		return this.source.bytesBefore(i, j, b);
	}

	@Override
	public int forEachByte(ByteProcessor byteProcessor) {
		return this.source.forEachByte(byteProcessor);
	}

	@Override
	public int forEachByte(int i, int j, ByteProcessor byteProcessor) {
		return this.source.forEachByte(i, j, byteProcessor);
	}

	@Override
	public int forEachByteDesc(ByteProcessor byteProcessor) {
		return this.source.forEachByteDesc(byteProcessor);
	}

	@Override
	public int forEachByteDesc(int i, int j, ByteProcessor byteProcessor) {
		return this.source.forEachByteDesc(i, j, byteProcessor);
	}

	@Override
	public ByteBuf copy() {
		return this.source.copy();
	}

	@Override
	public ByteBuf copy(int i, int j) {
		return this.source.copy(i, j);
	}

	@Override
	public ByteBuf slice() {
		return this.source.slice();
	}

	@Override
	public ByteBuf retainedSlice() {
		return this.source.retainedSlice();
	}

	@Override
	public ByteBuf slice(int i, int j) {
		return this.source.slice(i, j);
	}

	@Override
	public ByteBuf retainedSlice(int i, int j) {
		return this.source.retainedSlice(i, j);
	}

	@Override
	public ByteBuf duplicate() {
		return this.source.duplicate();
	}

	@Override
	public ByteBuf retainedDuplicate() {
		return this.source.retainedDuplicate();
	}

	@Override
	public int nioBufferCount() {
		return this.source.nioBufferCount();
	}

	@Override
	public ByteBuffer nioBuffer() {
		return this.source.nioBuffer();
	}

	@Override
	public ByteBuffer nioBuffer(int i, int j) {
		return this.source.nioBuffer(i, j);
	}

	@Override
	public ByteBuffer internalNioBuffer(int i, int j) {
		return this.source.internalNioBuffer(i, j);
	}

	@Override
	public ByteBuffer[] nioBuffers() {
		return this.source.nioBuffers();
	}

	@Override
	public ByteBuffer[] nioBuffers(int i, int j) {
		return this.source.nioBuffers(i, j);
	}

	@Override
	public boolean hasArray() {
		return this.source.hasArray();
	}

	@Override
	public byte[] array() {
		return this.source.array();
	}

	@Override
	public int arrayOffset() {
		return this.source.arrayOffset();
	}

	@Override
	public boolean hasMemoryAddress() {
		return this.source.hasMemoryAddress();
	}

	@Override
	public long memoryAddress() {
		return this.source.memoryAddress();
	}

	@Override
	public String toString(Charset charset) {
		return this.source.toString(charset);
	}

	@Override
	public String toString(int i, int j, Charset charset) {
		return this.source.toString(i, j, charset);
	}

	@Override
	public int hashCode() {
		return this.source.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return this.source.equals(object);
	}

	@Override
	public int compareTo(ByteBuf byteBuf) {
		return this.source.compareTo(byteBuf);
	}

	@Override
	public String toString() {
		return this.source.toString();
	}

	public FriendlyByteBuf retain(int i) {
		this.source.retain(i);
		return this;
	}

	public FriendlyByteBuf retain() {
		this.source.retain();
		return this;
	}

	public FriendlyByteBuf touch() {
		this.source.touch();
		return this;
	}

	public FriendlyByteBuf touch(Object object) {
		this.source.touch(object);
		return this;
	}

	@Override
	public int refCnt() {
		return this.source.refCnt();
	}

	@Override
	public boolean release() {
		return this.source.release();
	}

	@Override
	public boolean release(int i) {
		return this.source.release(i);
	}

	@FunctionalInterface
	public interface Reader<T> extends Function<FriendlyByteBuf, T> {
		default FriendlyByteBuf.Reader<Optional<T>> asOptional() {
			return friendlyByteBuf -> friendlyByteBuf.readOptional(this);
		}
	}

	@FunctionalInterface
	public interface Writer<T> extends BiConsumer<FriendlyByteBuf, T> {
		default FriendlyByteBuf.Writer<Optional<T>> asOptional() {
			return (friendlyByteBuf, optional) -> friendlyByteBuf.writeOptional(optional, this);
		}
	}
}
