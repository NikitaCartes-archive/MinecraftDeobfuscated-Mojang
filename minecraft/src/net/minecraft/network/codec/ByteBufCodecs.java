package net.minecraft.network.codec;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface ByteBufCodecs {
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
	StreamCodec<ByteBuf, Tag> TAG = tagCodec(NbtAccounter::unlimitedHeap);
	StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = tagCodec(NbtAccounter::unlimitedHeap).map(tag -> {
		if (tag instanceof CompoundTag) {
			return (CompoundTag)tag;
		} else {
			throw new DecoderException("Not a compound tag: " + tag);
		}
	}, compoundTag -> compoundTag);
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

	static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec) {
		return TAG.map(
			tag -> Util.getOrThrow(codec.parse(NbtOps.INSTANCE, tag), string -> new DecoderException("Failed to decode: " + string + " " + tag)),
			object -> Util.getOrThrow(codec.encodeStart(NbtOps.INSTANCE, (T)object), string -> new EncoderException("Failed to encode: " + string + " " + object))
		);
	}

	static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> codec) {
		return new StreamCodec<RegistryFriendlyByteBuf, T>() {
			public T decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
				Tag tag = ByteBufCodecs.TAG.decode(registryFriendlyByteBuf);
				RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, registryFriendlyByteBuf.registryAccess());
				return Util.getOrThrow(codec.parse(registryOps, tag), string -> new DecoderException("Failed to decode: " + string + " " + tag));
			}

			public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, T object) {
				RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, registryFriendlyByteBuf.registryAccess());
				Tag tag = Util.getOrThrow(codec.encodeStart(registryOps, object), string -> new EncoderException("Failed to encode: " + string + " " + object));
				ByteBufCodecs.TAG.encode(registryFriendlyByteBuf, tag);
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

	static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> intFunction, StreamCodec<? super B, V> streamCodec) {
		return new StreamCodec<B, C>() {
			public C decode(B byteBuf) {
				int i = VarInt.read(byteBuf);
				C collection = (C)intFunction.apply(i);

				for (int j = 0; j < i; j++) {
					collection.add(streamCodec.decode(byteBuf));
				}

				return collection;
			}

			public void encode(B byteBuf, C collection) {
				VarInt.write(byteBuf, collection.size());

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

	static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
		IntFunction<? extends M> intFunction, StreamCodec<? super B, K> streamCodec, StreamCodec<? super B, V> streamCodec2
	) {
		return new StreamCodec<B, M>() {
			public void encode(B byteBuf, M map) {
				VarInt.write(byteBuf, map.size());
				map.forEach((object, object2) -> {
					streamCodec.encode(byteBuf, (K)object);
					streamCodec2.encode(byteBuf, (V)object2);
				});
			}

			public M decode(B byteBuf) {
				int i = VarInt.read(byteBuf);
				M map = (M)intFunction.apply(i);

				for (int j = 0; j < i; j++) {
					K object = streamCodec.decode(byteBuf);
					V object2 = streamCodec2.decode(byteBuf);
					map.put(object, object2);
				}

				return map;
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
}
