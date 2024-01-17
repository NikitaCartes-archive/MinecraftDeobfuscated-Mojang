package net.minecraft.network.codec;

import com.mojang.datafixers.util.Function3;
import io.netty.buffer.ByteBuf;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface StreamCodec<B, V> extends StreamDecoder<B, V>, StreamEncoder<B, V> {
	static <B, V> StreamCodec<B, V> of(StreamEncoder<B, V> streamEncoder, StreamDecoder<B, V> streamDecoder) {
		return new StreamCodec<B, V>() {
			@Override
			public V decode(B object) {
				return streamDecoder.decode(object);
			}

			@Override
			public void encode(B object, V object2) {
				streamEncoder.encode(object, object2);
			}
		};
	}

	static <B, V> StreamCodec<B, V> ofMember(StreamMemberEncoder<B, V> streamMemberEncoder, StreamDecoder<B, V> streamDecoder) {
		return new StreamCodec<B, V>() {
			@Override
			public V decode(B object) {
				return streamDecoder.decode(object);
			}

			@Override
			public void encode(B object, V object2) {
				streamMemberEncoder.encode(object2, object);
			}
		};
	}

	static <B, V> StreamCodec<B, V> unit(V object) {
		return new StreamCodec<B, V>() {
			@Override
			public V decode(B object) {
				return object;
			}

			@Override
			public void encode(B object, V object2) {
				if (!object2.equals(object)) {
					throw new IllegalStateException("Can't encode '" + object2 + "', expected '" + object + "'");
				}
			}
		};
	}

	default <O> StreamCodec<B, O> apply(StreamCodec.CodecOperation<B, V, O> codecOperation) {
		return codecOperation.apply(this);
	}

	default <O> StreamCodec<B, O> map(Function<? super V, ? extends O> function, Function<? super O, ? extends V> function2) {
		return new StreamCodec<B, O>() {
			@Override
			public O decode(B object) {
				return (O)function.apply(StreamCodec.this.decode(object));
			}

			@Override
			public void encode(B object, O object2) {
				StreamCodec.this.encode(object, (V)function2.apply(object2));
			}
		};
	}

	default <O extends ByteBuf> StreamCodec<O, V> mapStream(Function<O, ? extends B> function) {
		return new StreamCodec<O, V>() {
			public V decode(O byteBuf) {
				B object = (B)function.apply(byteBuf);
				return StreamCodec.this.decode(object);
			}

			public void encode(O byteBuf, V object) {
				B object2 = (B)function.apply(byteBuf);
				StreamCodec.this.encode(object2, object);
			}
		};
	}

	default <U> StreamCodec<B, U> dispatch(Function<? super U, ? extends V> function, Function<? super V, ? extends StreamCodec<? super B, ? extends U>> function2) {
		return new StreamCodec<B, U>() {
			@Override
			public U decode(B object) {
				V object2 = StreamCodec.this.decode(object);
				StreamCodec<? super B, ? extends U> streamCodec = (StreamCodec<? super B, ? extends U>)function2.apply(object2);
				return (U)streamCodec.decode(object);
			}

			@Override
			public void encode(B object, U object2) {
				V object3 = (V)function.apply(object2);
				StreamCodec<B, U> streamCodec = (StreamCodec<B, U>)function2.apply(object3);
				StreamCodec.this.encode(object, object3);
				streamCodec.encode(object, object2);
			}
		};
	}

	static <B, C, T1> StreamCodec<B, C> composite(StreamCodec<? super B, T1> streamCodec, Function<C, T1> function, Function<T1, C> function2) {
		return new StreamCodec<B, C>() {
			@Override
			public C decode(B object) {
				T1 object2 = streamCodec.decode(object);
				return (C)function2.apply(object2);
			}

			@Override
			public void encode(B object, C object2) {
				streamCodec.encode(object, (T1)function.apply(object2));
			}
		};
	}

	static <B, C, T1, T2> StreamCodec<B, C> composite(
		StreamCodec<? super B, T1> streamCodec,
		Function<C, T1> function,
		StreamCodec<? super B, T2> streamCodec2,
		Function<C, T2> function2,
		BiFunction<T1, T2, C> biFunction
	) {
		return new StreamCodec<B, C>() {
			@Override
			public C decode(B object) {
				T1 object2 = streamCodec.decode(object);
				T2 object3 = streamCodec2.decode(object);
				return (C)biFunction.apply(object2, object3);
			}

			@Override
			public void encode(B object, C object2) {
				streamCodec.encode(object, (T1)function.apply(object2));
				streamCodec2.encode(object, (T2)function2.apply(object2));
			}
		};
	}

	static <B, C, T1, T2, T3> StreamCodec<B, C> composite(
		StreamCodec<? super B, T1> streamCodec,
		Function<C, T1> function,
		StreamCodec<? super B, T2> streamCodec2,
		Function<C, T2> function2,
		StreamCodec<? super B, T3> streamCodec3,
		Function<C, T3> function3,
		Function3<T1, T2, T3, C> function32
	) {
		return new StreamCodec<B, C>() {
			@Override
			public C decode(B object) {
				T1 object2 = streamCodec.decode(object);
				T2 object3 = streamCodec2.decode(object);
				T3 object4 = streamCodec3.decode(object);
				return function32.apply(object2, object3, object4);
			}

			@Override
			public void encode(B object, C object2) {
				streamCodec.encode(object, (T1)function.apply(object2));
				streamCodec2.encode(object, (T2)function2.apply(object2));
				streamCodec3.encode(object, (T3)function3.apply(object2));
			}
		};
	}

	default <S extends B> StreamCodec<S, V> cast() {
		return this;
	}

	@FunctionalInterface
	public interface CodecOperation<B, S, T> {
		StreamCodec<B, T> apply(StreamCodec<B, S> streamCodec);
	}
}
