package net.minecraft.network.codec;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface ByteBufCodecs {
	int MAX_INITIAL_COLLECTION_SIZE = 65536;
	StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<ByteBuf, Boolean>() {
		public Boolean decode(ByteBuf byteBuf) {
			return byteBuf.readBoolean();
		}

		public void encode(ByteBuf byteBuf, Boolean boolean_) {
			byteBuf.writeBoolean(boolean_);
		}
	};
	StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<ByteBuf, Byte>() {
		public Byte decode(ByteBuf byteBuf) {
			return byteBuf.readByte();
		}

		public void encode(ByteBuf byteBuf, Byte byte_) {
			byteBuf.writeByte(byte_);
		}
	};
	StreamCodec<ByteBuf, Short> SHORT = new StreamCodec<ByteBuf, Short>() {
		public Short decode(ByteBuf byteBuf) {
			return byteBuf.readShort();
		}

		public void encode(ByteBuf byteBuf, Short short_) {
			byteBuf.writeShort(short_);
		}
	};
	StreamCodec<ByteBuf, Character> CHAR = new StreamCodec<ByteBuf, Character>() {
		public Character decode(ByteBuf byteBuf) {
			return byteBuf.readChar();
		}

		public void encode(ByteBuf byteBuf, Character character) {
			byteBuf.writeChar(character);
		}
	};
	StreamCodec<ByteBuf, Integer> INT = new StreamCodec<ByteBuf, Integer>() {
		public Integer decode(ByteBuf byteBuf) {
			return byteBuf.readInt();
		}

		public void encode(ByteBuf byteBuf, Integer integer) {
			byteBuf.writeInt(integer);
		}
	};
	StreamCodec<ByteBuf, Integer> VAR_INT = new StreamCodec<ByteBuf, Integer>() {
		public Integer decode(ByteBuf byteBuf) {
			return VarInt.read(byteBuf);
		}

		public void encode(ByteBuf byteBuf, Integer integer) {
			VarInt.write(byteBuf, integer);
		}
	};
	StreamCodec<ByteBuf, Long> VAR_LONG = new StreamCodec<ByteBuf, Long>() {
		public Long decode(ByteBuf byteBuf) {
			return VarLong.read(byteBuf);
		}

		public void encode(ByteBuf byteBuf, Long long_) {
			VarLong.write(byteBuf, long_);
		}
	};
	StreamCodec<ByteBuf, Float> FLOAT = new StreamCodec<ByteBuf, Float>() {
		public Float decode(ByteBuf byteBuf) {
			return byteBuf.readFloat();
		}

		public void encode(ByteBuf byteBuf, Float float_) {
			byteBuf.writeFloat(float_);
		}
	};
	StreamCodec<ByteBuf, Double> DOUBLE = new StreamCodec<ByteBuf, Double>() {
		public Double decode(ByteBuf byteBuf) {
			return byteBuf.readDouble();
		}

		public void encode(ByteBuf byteBuf, Double double_) {
			byteBuf.writeDouble(double_);
		}
	};
	StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = new StreamCodec<ByteBuf, byte[]>() {
		public byte[] decode(ByteBuf byteBuf) {
			return FriendlyByteBuf.readByteArray(byteBuf);
		}

		public void encode(ByteBuf byteBuf, byte[] bs) {
			FriendlyByteBuf.writeByteArray(byteBuf, bs);
		}
	};
	StreamCodec<ByteBuf, String> STRING_UTF8 = stringUtf8(32767);
	StreamCodec<ByteBuf, Tag> TAG = tagCodec(() -> NbtAccounter.create(2097152L));
	StreamCodec<ByteBuf, Tag> TRUSTED_TAG = tagCodec(NbtAccounter::unlimitedHeap);
	StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = compoundTagCodec(() -> NbtAccounter.create(2097152L));
	StreamCodec<ByteBuf, CompoundTag> TRUSTED_COMPOUND_TAG = compoundTagCodec(NbtAccounter::unlimitedHeap);
	StreamCodec<ByteBuf, Optional<CompoundTag>> OPTIONAL_COMPOUND_TAG = new StreamCodec<ByteBuf, Optional<CompoundTag>>() {
		public Optional<CompoundTag> decode(ByteBuf byteBuf) {
			return Optional.ofNullable(FriendlyByteBuf.readNbt(byteBuf));
		}

		public void encode(ByteBuf byteBuf, Optional<CompoundTag> optional) {
			FriendlyByteBuf.writeNbt(byteBuf, (Tag)optional.orElse(null));
		}
	};
	StreamCodec<ByteBuf, Vector3f> VECTOR3F = new StreamCodec<ByteBuf, Vector3f>() {
		public Vector3f decode(ByteBuf byteBuf) {
			return FriendlyByteBuf.readVector3f(byteBuf);
		}

		public void encode(ByteBuf byteBuf, Vector3f vector3f) {
			FriendlyByteBuf.writeVector3f(byteBuf, vector3f);
		}
	};
	StreamCodec<ByteBuf, Quaternionf> QUATERNIONF = new StreamCodec<ByteBuf, Quaternionf>() {
		public Quaternionf decode(ByteBuf byteBuf) {
			return FriendlyByteBuf.readQuaternion(byteBuf);
		}

		public void encode(ByteBuf byteBuf, Quaternionf quaternionf) {
			FriendlyByteBuf.writeQuaternion(byteBuf, quaternionf);
		}
	};
	StreamCodec<ByteBuf, PropertyMap> GAME_PROFILE_PROPERTIES = new StreamCodec<ByteBuf, PropertyMap>() {
		private static final int MAX_PROPERTY_NAME_LENGTH = 64;
		private static final int MAX_PROPERTY_VALUE_LENGTH = 32767;
		private static final int MAX_PROPERTY_SIGNATURE_LENGTH = 1024;
		private static final int MAX_PROPERTIES = 16;

		public PropertyMap decode(ByteBuf byteBuf) {
			int i = ByteBufCodecs.readCount(byteBuf, 16);
			PropertyMap propertyMap = new PropertyMap();

			for (int j = 0; j < i; j++) {
				String string = Utf8String.read(byteBuf, 64);
				String string2 = Utf8String.read(byteBuf, 32767);
				String string3 = FriendlyByteBuf.readNullable(byteBuf, byteBufx -> Utf8String.read(byteBufx, 1024));
				Property property = new Property(string, string2, string3);
				propertyMap.put(property.name(), property);
			}

			return propertyMap;
		}

		public void encode(ByteBuf byteBuf, PropertyMap propertyMap) {
			ByteBufCodecs.writeCount(byteBuf, propertyMap.size(), 16);

			for (Property property : propertyMap.values()) {
				Utf8String.write(byteBuf, property.name(), 64);
				Utf8String.write(byteBuf, property.value(), 32767);
				FriendlyByteBuf.writeNullable(byteBuf, property.signature(), (byteBufx, string) -> Utf8String.write(byteBufx, string, 1024));
			}
		}
	};
	StreamCodec<ByteBuf, GameProfile> GAME_PROFILE = new StreamCodec<ByteBuf, GameProfile>() {
		public GameProfile decode(ByteBuf byteBuf) {
			UUID uUID = UUIDUtil.STREAM_CODEC.decode(byteBuf);
			String string = Utf8String.read(byteBuf, 16);
			GameProfile gameProfile = new GameProfile(uUID, string);
			gameProfile.getProperties().putAll(ByteBufCodecs.GAME_PROFILE_PROPERTIES.decode(byteBuf));
			return gameProfile;
		}

		public void encode(ByteBuf byteBuf, GameProfile gameProfile) {
			UUIDUtil.STREAM_CODEC.encode(byteBuf, gameProfile.getId());
			Utf8String.write(byteBuf, gameProfile.getName(), 16);
			ByteBufCodecs.GAME_PROFILE_PROPERTIES.encode(byteBuf, gameProfile.getProperties());
		}
	};

	static StreamCodec<ByteBuf, byte[]> byteArray(int i) {
		return new StreamCodec<ByteBuf, byte[]>() {
			public byte[] decode(ByteBuf byteBuf) {
				return FriendlyByteBuf.readByteArray(byteBuf, i);
			}

			public void encode(ByteBuf byteBuf, byte[] bs) {
				if (bs.length > i) {
					throw new EncoderException("ByteArray with size " + bs.length + " is bigger than allowed " + i);
				} else {
					FriendlyByteBuf.writeByteArray(byteBuf, bs);
				}
			}
		};
	}

	static StreamCodec<ByteBuf, String> stringUtf8(int i) {
		return new StreamCodec<ByteBuf, String>() {
			public String decode(ByteBuf byteBuf) {
				return Utf8String.read(byteBuf, i);
			}

			public void encode(ByteBuf byteBuf, String string) {
				Utf8String.write(byteBuf, string, i);
			}
		};
	}

	static StreamCodec<ByteBuf, Tag> tagCodec(Supplier<NbtAccounter> supplier) {
		return new StreamCodec<ByteBuf, Tag>() {
			public Tag decode(ByteBuf byteBuf) {
				Tag tag = FriendlyByteBuf.readNbt(byteBuf, (NbtAccounter)supplier.get());
				if (tag == null) {
					throw new DecoderException("Expected non-null compound tag");
				} else {
					return tag;
				}
			}

			public void encode(ByteBuf byteBuf, Tag tag) {
				if (tag == EndTag.INSTANCE) {
					throw new EncoderException("Expected non-null compound tag");
				} else {
					FriendlyByteBuf.writeNbt(byteBuf, tag);
				}
			}
		};
	}

	static StreamCodec<ByteBuf, CompoundTag> compoundTagCodec(Supplier<NbtAccounter> supplier) {
		return tagCodec(supplier).map(tag -> {
			if (tag instanceof CompoundTag) {
				return (CompoundTag)tag;
			} else {
				throw new DecoderException("Not a compound tag: " + tag);
			}
		}, compoundTag -> compoundTag);
	}

	static <T> StreamCodec<ByteBuf, T> fromCodecTrusted(Codec<T> codec) {
		return fromCodec(codec, NbtAccounter::unlimitedHeap);
	}

	static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec) {
		return fromCodec(codec, () -> NbtAccounter.create(2097152L));
	}

	static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec, Supplier<NbtAccounter> supplier) {
		return tagCodec(supplier)
			.map(
				tag -> Util.getOrThrow(codec.parse(NbtOps.INSTANCE, tag), string -> new DecoderException("Failed to decode: " + string + " " + tag)),
				object -> Util.getOrThrow(codec.encodeStart(NbtOps.INSTANCE, (T)object), string -> new EncoderException("Failed to encode: " + string + " " + object))
			);
	}

	static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistriesTrusted(Codec<T> codec) {
		return fromCodecWithRegistries(codec, NbtAccounter::unlimitedHeap);
	}

	static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> codec) {
		return fromCodecWithRegistries(codec, () -> NbtAccounter.create(2097152L));
	}

	static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> codec, Supplier<NbtAccounter> supplier) {
		final StreamCodec<ByteBuf, Tag> streamCodec = tagCodec(supplier);
		return new StreamCodec<RegistryFriendlyByteBuf, T>() {
			public T decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
				Tag tag = streamCodec.decode(registryFriendlyByteBuf);
				RegistryOps<Tag> registryOps = registryFriendlyByteBuf.registryAccess().createSerializationContext(NbtOps.INSTANCE);
				return Util.getOrThrow(codec.parse(registryOps, tag), string -> new DecoderException("Failed to decode: " + string + " " + tag));
			}

			public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, T object) {
				RegistryOps<Tag> registryOps = registryFriendlyByteBuf.registryAccess().createSerializationContext(NbtOps.INSTANCE);
				Tag tag = Util.getOrThrow(codec.encodeStart(registryOps, object), string -> new EncoderException("Failed to encode: " + string + " " + object));
				streamCodec.encode(registryFriendlyByteBuf, tag);
			}
		};
	}

	static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(StreamCodec<B, V> streamCodec) {
		return new StreamCodec<B, Optional<V>>() {
			public Optional<V> decode(B byteBuf) {
				return byteBuf.readBoolean() ? Optional.of(streamCodec.decode(byteBuf)) : Optional.empty();
			}

			public void encode(B byteBuf, Optional<V> optional) {
				if (optional.isPresent()) {
					byteBuf.writeBoolean(true);
					streamCodec.encode(byteBuf, (V)optional.get());
				} else {
					byteBuf.writeBoolean(false);
				}
			}
		};
	}

	static int readCount(ByteBuf byteBuf, int i) {
		int j = VarInt.read(byteBuf);
		if (j > i) {
			throw new DecoderException(j + " elements exceeded max size of: " + i);
		} else {
			return j;
		}
	}

	static void writeCount(ByteBuf byteBuf, int i, int j) {
		if (i > j) {
			throw new EncoderException(i + " elements exceeded max size of: " + j);
		} else {
			VarInt.write(byteBuf, i);
		}
	}

	static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> intFunction, StreamCodec<? super B, V> streamCodec) {
		return collection(intFunction, streamCodec, Integer.MAX_VALUE);
	}

	static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> intFunction, StreamCodec<? super B, V> streamCodec, int i) {
		return new StreamCodec<B, C>() {
			public C decode(B byteBuf) {
				int i = ByteBufCodecs.readCount(byteBuf, i);
				C collection = (C)intFunction.apply(Math.min(i, 65536));

				for (int j = 0; j < i; j++) {
					collection.add(streamCodec.decode(byteBuf));
				}

				return collection;
			}

			public void encode(B byteBuf, C collection) {
				ByteBufCodecs.writeCount(byteBuf, collection.size(), i);

				for (V object : collection) {
					streamCodec.encode(byteBuf, object);
				}
			}
		};
	}

	static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(IntFunction<C> intFunction) {
		return streamCodec -> collection(intFunction, streamCodec);
	}

	static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
		return streamCodec -> collection(ArrayList::new, streamCodec);
	}

	static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list(int i) {
		return streamCodec -> collection(ArrayList::new, streamCodec, i);
	}

	static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
		IntFunction<? extends M> intFunction, StreamCodec<? super B, K> streamCodec, StreamCodec<? super B, V> streamCodec2
	) {
		return map(intFunction, streamCodec, streamCodec2, Integer.MAX_VALUE);
	}

	static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
		IntFunction<? extends M> intFunction, StreamCodec<? super B, K> streamCodec, StreamCodec<? super B, V> streamCodec2, int i
	) {
		return new StreamCodec<B, M>() {
			public void encode(B byteBuf, M map) {
				ByteBufCodecs.writeCount(byteBuf, map.size(), i);
				map.forEach((object, object2) -> {
					streamCodec.encode(byteBuf, (K)object);
					streamCodec2.encode(byteBuf, (V)object2);
				});
			}

			public M decode(B byteBuf) {
				int i = ByteBufCodecs.readCount(byteBuf, i);
				M map = (M)intFunction.apply(Math.min(i, 65536));

				for (int j = 0; j < i; j++) {
					K object = streamCodec.decode(byteBuf);
					V object2 = streamCodec2.decode(byteBuf);
					map.put(object, object2);
				}

				return map;
			}
		};
	}

	static <B extends ByteBuf, L, R> StreamCodec<B, Either<L, R>> either(StreamCodec<? super B, L> streamCodec, StreamCodec<? super B, R> streamCodec2) {
		return new StreamCodec<B, Either<L, R>>() {
			public Either<L, R> decode(B byteBuf) {
				return byteBuf.readBoolean() ? Either.left(streamCodec.decode(byteBuf)) : Either.right(streamCodec2.decode(byteBuf));
			}

			public void encode(B byteBuf, Either<L, R> either) {
				either.ifLeft(object -> {
					byteBuf.writeBoolean(true);
					streamCodec.encode(byteBuf, (L)object);
				}).ifRight(object -> {
					byteBuf.writeBoolean(false);
					streamCodec2.encode(byteBuf, (R)object);
				});
			}
		};
	}

	static <T> StreamCodec<ByteBuf, T> idMapper(IntFunction<T> intFunction, ToIntFunction<T> toIntFunction) {
		return new StreamCodec<ByteBuf, T>() {
			public T decode(ByteBuf byteBuf) {
				int i = VarInt.read(byteBuf);
				return (T)intFunction.apply(i);
			}

			public void encode(ByteBuf byteBuf, T object) {
				int i = toIntFunction.applyAsInt(object);
				VarInt.write(byteBuf, i);
			}
		};
	}

	static <T> StreamCodec<ByteBuf, T> idMapper(IdMap<T> idMap) {
		return idMapper(idMap::byIdOrThrow, idMap::getIdOrThrow);
	}

	private static <T, R> StreamCodec<RegistryFriendlyByteBuf, R> registry(
		ResourceKey<? extends Registry<T>> resourceKey, Function<Registry<T>, IdMap<R>> function
	) {
		return new StreamCodec<RegistryFriendlyByteBuf, R>() {
			private IdMap<R> getRegistryOrThrow(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
				return (IdMap<R>)function.apply(registryFriendlyByteBuf.registryAccess().registryOrThrow(resourceKey));
			}

			public R decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
				int i = VarInt.read(registryFriendlyByteBuf);
				return (R)this.getRegistryOrThrow(registryFriendlyByteBuf).byIdOrThrow(i);
			}

			public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, R object) {
				int i = this.getRegistryOrThrow(registryFriendlyByteBuf).getIdOrThrow(object);
				VarInt.write(registryFriendlyByteBuf, i);
			}
		};
	}

	static <T> StreamCodec<RegistryFriendlyByteBuf, T> registry(ResourceKey<? extends Registry<T>> resourceKey) {
		return registry(resourceKey, registry -> registry);
	}

	static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderRegistry(ResourceKey<? extends Registry<T>> resourceKey) {
		return registry(resourceKey, Registry::asHolderIdMap);
	}

	static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holder(
		ResourceKey<? extends Registry<T>> resourceKey, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec
	) {
		return new StreamCodec<RegistryFriendlyByteBuf, Holder<T>>() {
			private static final int DIRECT_HOLDER_ID = 0;

			private IdMap<Holder<T>> getRegistryOrThrow(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
				return registryFriendlyByteBuf.registryAccess().registryOrThrow(resourceKey).asHolderIdMap();
			}

			public Holder<T> decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
				int i = VarInt.read(registryFriendlyByteBuf);
				return i == 0 ? Holder.direct(streamCodec.decode(registryFriendlyByteBuf)) : (Holder)this.getRegistryOrThrow(registryFriendlyByteBuf).byIdOrThrow(i - 1);
			}

			public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, Holder<T> holder) {
				switch (holder.kind()) {
					case REFERENCE:
						int i = this.getRegistryOrThrow(registryFriendlyByteBuf).getIdOrThrow(holder);
						VarInt.write(registryFriendlyByteBuf, i + 1);
						break;
					case DIRECT:
						VarInt.write(registryFriendlyByteBuf, 0);
						streamCodec.encode(registryFriendlyByteBuf, holder.value());
				}
			}
		};
	}

	static <T> StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> holderSet(ResourceKey<? extends Registry<T>> resourceKey) {
		return new StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>>() {
			private static final int NAMED_SET = -1;
			private final StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderCodec = ByteBufCodecs.holderRegistry(resourceKey);

			public HolderSet<T> decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
				int i = VarInt.read(registryFriendlyByteBuf) - 1;
				if (i == -1) {
					Registry<T> registry = registryFriendlyByteBuf.registryAccess().registryOrThrow(resourceKey);
					return (HolderSet<T>)registry.getTag(TagKey.create(resourceKey, ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf))).orElseThrow();
				} else {
					List<Holder<T>> list = new ArrayList(Math.min(i, 65536));

					for (int j = 0; j < i; j++) {
						list.add(this.holderCodec.decode(registryFriendlyByteBuf));
					}

					return HolderSet.direct(list);
				}
			}

			public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, HolderSet<T> holderSet) {
				Optional<TagKey<T>> optional = holderSet.unwrapKey();
				if (optional.isPresent()) {
					VarInt.write(registryFriendlyByteBuf, 0);
					ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, ((TagKey)optional.get()).location());
				} else {
					VarInt.write(registryFriendlyByteBuf, holderSet.size() + 1);

					for (Holder<T> holder : holderSet) {
						this.holderCodec.encode(registryFriendlyByteBuf, holder);
					}
				}
			}
		};
	}
}
