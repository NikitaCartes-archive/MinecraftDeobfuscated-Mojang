package net.minecraft.network;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
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
import java.nio.charset.StandardCharsets;
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
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FriendlyByteBuf extends ByteBuf {
	private static final int MAX_VARINT_SIZE = 5;
	private static final int MAX_VARLONG_SIZE = 10;
	public static final int DEFAULT_NBT_QUOTA = 2097152;
	private final ByteBuf source;
	public static final short MAX_STRING_LENGTH = 32767;
	public static final int MAX_COMPONENT_STRING_LENGTH = 262144;
	private static final int PUBLIC_KEY_SIZE = 256;
	private static final int MAX_PUBLIC_KEY_HEADER_SIZE = 256;
	private static final int MAX_PUBLIC_KEY_LENGTH = 512;

	public FriendlyByteBuf(ByteBuf byteBuf) {
		this.source = byteBuf;
	}

	public static int getVarIntSize(int i) {
		for (int j = 1; j < 5; j++) {
			if ((i & -1 << j * 7) == 0) {
				return j;
			}
		}

		return 5;
	}

	public static int getVarLongSize(long l) {
		for (int i = 1; i < 10; i++) {
			if ((l & -1L << i * 7) == 0L) {
				return i;
			}
		}

		return 10;
	}

	@Deprecated
	public <T> T readWithCodec(DynamicOps<Tag> dynamicOps, Codec<T> codec) {
		CompoundTag compoundTag = this.readAnySizeNbt();
		return Util.getOrThrow(codec.parse(dynamicOps, compoundTag), string -> new DecoderException("Failed to decode: " + string + " " + compoundTag));
	}

	@Deprecated
	public <T> void writeWithCodec(DynamicOps<Tag> dynamicOps, Codec<T> codec, T object) {
		Tag tag = Util.getOrThrow(codec.encodeStart(dynamicOps, object), string -> new EncoderException("Failed to encode: " + string + " " + object));
		this.writeNbt((CompoundTag)tag);
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

	@VisibleForTesting
	public byte[] accessByteBufWithCorrectSize() {
		int i = this.writerIndex();
		byte[] bs = new byte[i];
		this.getBytes(0, bs);
		return bs;
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

	public int readVarInt() {
		int i = 0;
		int j = 0;

		byte b;
		do {
			b = this.readByte();
			i |= (b & 127) << j++ * 7;
			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}
		} while ((b & 128) == 128);

		return i;
	}

	public long readVarLong() {
		long l = 0L;
		int i = 0;

		byte b;
		do {
			b = this.readByte();
			l |= (long)(b & 127) << i++ * 7;
			if (i > 10) {
				throw new RuntimeException("VarLong too big");
			}
		} while ((b & 128) == 128);

		return l;
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
		while ((i & -128) != 0) {
			this.writeByte(i & 127 | 128);
			i >>>= 7;
		}

		this.writeByte(i);
		return this;
	}

	public FriendlyByteBuf writeVarLong(long l) {
		while ((l & -128L) != 0L) {
			this.writeByte((int)(l & 127L) | 128);
			l >>>= 7;
		}

		this.writeByte((int)l);
		return this;
	}

	public FriendlyByteBuf writeNbt(@Nullable CompoundTag compoundTag) {
		if (compoundTag == null) {
			this.writeByte(0);
		} else {
			try {
				NbtIo.write(compoundTag, new ByteBufOutputStream(this));
			} catch (IOException var3) {
				throw new EncoderException(var3);
			}
		}

		return this;
	}

	@Nullable
	public CompoundTag readNbt() {
		return this.readNbt(new NbtAccounter(2097152L));
	}

	@Nullable
	public CompoundTag readAnySizeNbt() {
		return this.readNbt(NbtAccounter.UNLIMITED);
	}

	@Nullable
	public CompoundTag readNbt(NbtAccounter nbtAccounter) {
		int i = this.readerIndex();
		byte b = this.readByte();
		if (b == 0) {
			return null;
		} else {
			this.readerIndex(i);

			try {
				return NbtIo.read(new ByteBufInputStream(this), nbtAccounter);
			} catch (IOException var5) {
				throw new EncoderException(var5);
			}
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
		int j = getMaxEncodedUtfLength(i);
		int k = this.readVarInt();
		if (k > j) {
			throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + k + " > " + j + ")");
		} else if (k < 0) {
			throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
		} else {
			String string = this.toString(this.readerIndex(), k, StandardCharsets.UTF_8);
			this.readerIndex(this.readerIndex() + k);
			if (string.length() > i) {
				throw new DecoderException("The received string length is longer than maximum allowed (" + string.length() + " > " + i + ")");
			} else {
				return string;
			}
		}
	}

	public FriendlyByteBuf writeUtf(String string) {
		return this.writeUtf(string, 32767);
	}

	public FriendlyByteBuf writeUtf(String string, int i) {
		if (string.length() > i) {
			throw new EncoderException("String too big (was " + string.length() + " characters, max " + i + ")");
		} else {
			byte[] bs = string.getBytes(StandardCharsets.UTF_8);
			int j = getMaxEncodedUtfLength(i);
			if (bs.length > j) {
				throw new EncoderException("String too big (was " + bs.length + " bytes encoded, max " + j + ")");
			} else {
				this.writeVarInt(bs.length);
				this.writeBytes(bs);
				return this;
			}
		}
	}

	private static int getMaxEncodedUtfLength(int i) {
		return i * 3;
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
			propertyMap.put(property.getName(), property);
		});
		return propertyMap;
	}

	public void writeGameProfileProperties(PropertyMap propertyMap) {
		this.writeCollection(propertyMap.values(), FriendlyByteBuf::writeProperty);
	}

	public Property readProperty() {
		String string = this.readUtf();
		String string2 = this.readUtf();
		if (this.readBoolean()) {
			String string3 = this.readUtf();
			return new Property(string, string2, string3);
		} else {
			return new Property(string, string2);
		}
	}

	public void writeProperty(Property property) {
		this.writeUtf(property.getName());
		this.writeUtf(property.getValue());
		if (property.hasSignature()) {
			this.writeBoolean(true);
			this.writeUtf(property.getSignature());
		} else {
			this.writeBoolean(false);
		}
	}

	@Override
	public int capacity() {
		return this.source.capacity();
	}

	@Override
	public ByteBuf capacity(int i) {
		return this.source.capacity(i);
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
		return this.source.unwrap();
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

	@Override
	public ByteBuf readerIndex(int i) {
		return this.source.readerIndex(i);
	}

	@Override
	public int writerIndex() {
		return this.source.writerIndex();
	}

	@Override
	public ByteBuf writerIndex(int i) {
		return this.source.writerIndex(i);
	}

	@Override
	public ByteBuf setIndex(int i, int j) {
		return this.source.setIndex(i, j);
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

	@Override
	public ByteBuf clear() {
		return this.source.clear();
	}

	@Override
	public ByteBuf markReaderIndex() {
		return this.source.markReaderIndex();
	}

	@Override
	public ByteBuf resetReaderIndex() {
		return this.source.resetReaderIndex();
	}

	@Override
	public ByteBuf markWriterIndex() {
		return this.source.markWriterIndex();
	}

	@Override
	public ByteBuf resetWriterIndex() {
		return this.source.resetWriterIndex();
	}

	@Override
	public ByteBuf discardReadBytes() {
		return this.source.discardReadBytes();
	}

	@Override
	public ByteBuf discardSomeReadBytes() {
		return this.source.discardSomeReadBytes();
	}

	@Override
	public ByteBuf ensureWritable(int i) {
		return this.source.ensureWritable(i);
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

	@Override
	public ByteBuf getBytes(int i, ByteBuf byteBuf) {
		return this.source.getBytes(i, byteBuf);
	}

	@Override
	public ByteBuf getBytes(int i, ByteBuf byteBuf, int j) {
		return this.source.getBytes(i, byteBuf, j);
	}

	@Override
	public ByteBuf getBytes(int i, ByteBuf byteBuf, int j, int k) {
		return this.source.getBytes(i, byteBuf, j, k);
	}

	@Override
	public ByteBuf getBytes(int i, byte[] bs) {
		return this.source.getBytes(i, bs);
	}

	@Override
	public ByteBuf getBytes(int i, byte[] bs, int j, int k) {
		return this.source.getBytes(i, bs, j, k);
	}

	@Override
	public ByteBuf getBytes(int i, ByteBuffer byteBuffer) {
		return this.source.getBytes(i, byteBuffer);
	}

	@Override
	public ByteBuf getBytes(int i, OutputStream outputStream, int j) throws IOException {
		return this.source.getBytes(i, outputStream, j);
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

	@Override
	public ByteBuf setBoolean(int i, boolean bl) {
		return this.source.setBoolean(i, bl);
	}

	@Override
	public ByteBuf setByte(int i, int j) {
		return this.source.setByte(i, j);
	}

	@Override
	public ByteBuf setShort(int i, int j) {
		return this.source.setShort(i, j);
	}

	@Override
	public ByteBuf setShortLE(int i, int j) {
		return this.source.setShortLE(i, j);
	}

	@Override
	public ByteBuf setMedium(int i, int j) {
		return this.source.setMedium(i, j);
	}

	@Override
	public ByteBuf setMediumLE(int i, int j) {
		return this.source.setMediumLE(i, j);
	}

	@Override
	public ByteBuf setInt(int i, int j) {
		return this.source.setInt(i, j);
	}

	@Override
	public ByteBuf setIntLE(int i, int j) {
		return this.source.setIntLE(i, j);
	}

	@Override
	public ByteBuf setLong(int i, long l) {
		return this.source.setLong(i, l);
	}

	@Override
	public ByteBuf setLongLE(int i, long l) {
		return this.source.setLongLE(i, l);
	}

	@Override
	public ByteBuf setChar(int i, int j) {
		return this.source.setChar(i, j);
	}

	@Override
	public ByteBuf setFloat(int i, float f) {
		return this.source.setFloat(i, f);
	}

	@Override
	public ByteBuf setDouble(int i, double d) {
		return this.source.setDouble(i, d);
	}

	@Override
	public ByteBuf setBytes(int i, ByteBuf byteBuf) {
		return this.source.setBytes(i, byteBuf);
	}

	@Override
	public ByteBuf setBytes(int i, ByteBuf byteBuf, int j) {
		return this.source.setBytes(i, byteBuf, j);
	}

	@Override
	public ByteBuf setBytes(int i, ByteBuf byteBuf, int j, int k) {
		return this.source.setBytes(i, byteBuf, j, k);
	}

	@Override
	public ByteBuf setBytes(int i, byte[] bs) {
		return this.source.setBytes(i, bs);
	}

	@Override
	public ByteBuf setBytes(int i, byte[] bs, int j, int k) {
		return this.source.setBytes(i, bs, j, k);
	}

	@Override
	public ByteBuf setBytes(int i, ByteBuffer byteBuffer) {
		return this.source.setBytes(i, byteBuffer);
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

	@Override
	public ByteBuf setZero(int i, int j) {
		return this.source.setZero(i, j);
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

	@Override
	public ByteBuf readBytes(ByteBuf byteBuf) {
		return this.source.readBytes(byteBuf);
	}

	@Override
	public ByteBuf readBytes(ByteBuf byteBuf, int i) {
		return this.source.readBytes(byteBuf, i);
	}

	@Override
	public ByteBuf readBytes(ByteBuf byteBuf, int i, int j) {
		return this.source.readBytes(byteBuf, i, j);
	}

	@Override
	public ByteBuf readBytes(byte[] bs) {
		return this.source.readBytes(bs);
	}

	@Override
	public ByteBuf readBytes(byte[] bs, int i, int j) {
		return this.source.readBytes(bs, i, j);
	}

	@Override
	public ByteBuf readBytes(ByteBuffer byteBuffer) {
		return this.source.readBytes(byteBuffer);
	}

	@Override
	public ByteBuf readBytes(OutputStream outputStream, int i) throws IOException {
		return this.source.readBytes(outputStream, i);
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

	@Override
	public ByteBuf skipBytes(int i) {
		return this.source.skipBytes(i);
	}

	@Override
	public ByteBuf writeBoolean(boolean bl) {
		return this.source.writeBoolean(bl);
	}

	@Override
	public ByteBuf writeByte(int i) {
		return this.source.writeByte(i);
	}

	@Override
	public ByteBuf writeShort(int i) {
		return this.source.writeShort(i);
	}

	@Override
	public ByteBuf writeShortLE(int i) {
		return this.source.writeShortLE(i);
	}

	@Override
	public ByteBuf writeMedium(int i) {
		return this.source.writeMedium(i);
	}

	@Override
	public ByteBuf writeMediumLE(int i) {
		return this.source.writeMediumLE(i);
	}

	@Override
	public ByteBuf writeInt(int i) {
		return this.source.writeInt(i);
	}

	@Override
	public ByteBuf writeIntLE(int i) {
		return this.source.writeIntLE(i);
	}

	@Override
	public ByteBuf writeLong(long l) {
		return this.source.writeLong(l);
	}

	@Override
	public ByteBuf writeLongLE(long l) {
		return this.source.writeLongLE(l);
	}

	@Override
	public ByteBuf writeChar(int i) {
		return this.source.writeChar(i);
	}

	@Override
	public ByteBuf writeFloat(float f) {
		return this.source.writeFloat(f);
	}

	@Override
	public ByteBuf writeDouble(double d) {
		return this.source.writeDouble(d);
	}

	@Override
	public ByteBuf writeBytes(ByteBuf byteBuf) {
		return this.source.writeBytes(byteBuf);
	}

	@Override
	public ByteBuf writeBytes(ByteBuf byteBuf, int i) {
		return this.source.writeBytes(byteBuf, i);
	}

	@Override
	public ByteBuf writeBytes(ByteBuf byteBuf, int i, int j) {
		return this.source.writeBytes(byteBuf, i, j);
	}

	@Override
	public ByteBuf writeBytes(byte[] bs) {
		return this.source.writeBytes(bs);
	}

	@Override
	public ByteBuf writeBytes(byte[] bs, int i, int j) {
		return this.source.writeBytes(bs, i, j);
	}

	@Override
	public ByteBuf writeBytes(ByteBuffer byteBuffer) {
		return this.source.writeBytes(byteBuffer);
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

	@Override
	public ByteBuf writeZero(int i) {
		return this.source.writeZero(i);
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

	@Override
	public ByteBuf retain(int i) {
		return this.source.retain(i);
	}

	@Override
	public ByteBuf retain() {
		return this.source.retain();
	}

	@Override
	public ByteBuf touch() {
		return this.source.touch();
	}

	@Override
	public ByteBuf touch(Object object) {
		return this.source.touch(object);
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
