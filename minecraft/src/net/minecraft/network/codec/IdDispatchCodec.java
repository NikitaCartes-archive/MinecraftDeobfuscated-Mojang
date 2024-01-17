package net.minecraft.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.VarInt;

public class IdDispatchCodec<B extends ByteBuf, V, T> implements StreamCodec<B, V> {
	private static final int UNKNOWN_TYPE = -1;
	private final Function<V, ? extends T> typeGetter;
	private final List<IdDispatchCodec.Entry<B, V, T>> byId;
	private final Object2IntMap<T> toId;

	IdDispatchCodec(Function<V, ? extends T> function, List<IdDispatchCodec.Entry<B, V, T>> list, Object2IntMap<T> object2IntMap) {
		this.typeGetter = function;
		this.byId = list;
		this.toId = object2IntMap;
	}

	public V decode(B byteBuf) {
		int i = VarInt.read(byteBuf);
		if (i >= 0 && i < this.byId.size()) {
			IdDispatchCodec.Entry<B, V, T> entry = (IdDispatchCodec.Entry<B, V, T>)this.byId.get(i);

			try {
				return (V)entry.serializer.decode(byteBuf);
			} catch (Exception var5) {
				throw new DecoderException("Failed to decode packet '" + entry.type + "'", var5);
			}
		} else {
			throw new DecoderException("Received unknown packet id " + i);
		}
	}

	public void encode(B byteBuf, V object) {
		T object2 = (T)this.typeGetter.apply(object);
		int i = this.toId.getOrDefault(object2, -1);
		if (i == -1) {
			throw new EncoderException("Sending unknown packet '" + object2 + "'");
		} else {
			VarInt.write(byteBuf, i);
			IdDispatchCodec.Entry<B, V, T> entry = (IdDispatchCodec.Entry<B, V, T>)this.byId.get(i);

			try {
				StreamCodec<? super B, V> streamCodec = (StreamCodec<? super B, V>)entry.serializer;
				streamCodec.encode(byteBuf, object);
			} catch (Exception var7) {
				throw new EncoderException("Failed to encode packet '" + object2 + "'", var7);
			}
		}
	}

	public static <B extends ByteBuf, V, T> IdDispatchCodec.Builder<B, V, T> builder(Function<V, ? extends T> function) {
		return new IdDispatchCodec.Builder<>(function);
	}

	public static class Builder<B extends ByteBuf, V, T> {
		private final List<IdDispatchCodec.Entry<B, V, T>> entries = new ArrayList();
		private final Function<V, ? extends T> typeGetter;

		Builder(Function<V, ? extends T> function) {
			this.typeGetter = function;
		}

		public IdDispatchCodec.Builder<B, V, T> add(T object, StreamCodec<? super B, ? extends V> streamCodec) {
			this.entries.add(new IdDispatchCodec.Entry<>(streamCodec, object));
			return this;
		}

		public IdDispatchCodec<B, V, T> build() {
			Object2IntOpenHashMap<T> object2IntOpenHashMap = new Object2IntOpenHashMap<>();
			object2IntOpenHashMap.defaultReturnValue(-2);

			for (IdDispatchCodec.Entry<B, V, T> entry : this.entries) {
				int i = object2IntOpenHashMap.size();
				int j = object2IntOpenHashMap.putIfAbsent(entry.type, i);
				if (j != -2) {
					throw new IllegalStateException("Duplicate registration for type " + entry.type);
				}
			}

			return new IdDispatchCodec<>(this.typeGetter, List.copyOf(this.entries), object2IntOpenHashMap);
		}
	}

	static record Entry<B, V, T>(StreamCodec<? super B, ? extends V> serializer, T type) {
	}
}
