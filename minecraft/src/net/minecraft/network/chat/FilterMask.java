package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.BitSet;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.StringUtils;

public class FilterMask {
	public static final Codec<FilterMask> CODEC = StringRepresentable.fromEnum(FilterMask.Type::values).dispatch(FilterMask::type, FilterMask.Type::codec);
	public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), FilterMask.Type.FULLY_FILTERED);
	public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), FilterMask.Type.PASS_THROUGH);
	public static final Style FILTERED_STYLE = Style.EMPTY
		.withColor(ChatFormatting.DARK_GRAY)
		.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.filtered")));
	static final MapCodec<FilterMask> PASS_THROUGH_CODEC = MapCodec.unit(PASS_THROUGH);
	static final MapCodec<FilterMask> FULLY_FILTERED_CODEC = MapCodec.unit(FULLY_FILTERED);
	static final MapCodec<FilterMask> PARTIALLY_FILTERED_CODEC = ExtraCodecs.BIT_SET.<FilterMask>xmap(FilterMask::new, FilterMask::mask).fieldOf("value");
	private static final char HASH = '#';
	private final BitSet mask;
	private final FilterMask.Type type;

	private FilterMask(BitSet bitSet, FilterMask.Type type) {
		this.mask = bitSet;
		this.type = type;
	}

	private FilterMask(BitSet bitSet) {
		this.mask = bitSet;
		this.type = FilterMask.Type.PARTIALLY_FILTERED;
	}

	public FilterMask(int i) {
		this(new BitSet(i), FilterMask.Type.PARTIALLY_FILTERED);
	}

	private FilterMask.Type type() {
		return this.type;
	}

	private BitSet mask() {
		return this.mask;
	}

	public static FilterMask read(FriendlyByteBuf friendlyByteBuf) {
		FilterMask.Type type = friendlyByteBuf.readEnum(FilterMask.Type.class);

		return switch (type) {
			case PASS_THROUGH -> PASS_THROUGH;
			case FULLY_FILTERED -> FULLY_FILTERED;
			case PARTIALLY_FILTERED -> new FilterMask(friendlyByteBuf.readBitSet(), FilterMask.Type.PARTIALLY_FILTERED);
		};
	}

	public static void write(FriendlyByteBuf friendlyByteBuf, FilterMask filterMask) {
		friendlyByteBuf.writeEnum(filterMask.type);
		if (filterMask.type == FilterMask.Type.PARTIALLY_FILTERED) {
			friendlyByteBuf.writeBitSet(filterMask.mask);
		}
	}

	public void setFiltered(int i) {
		this.mask.set(i);
	}

	@Nullable
	public String apply(String string) {
		return switch (this.type) {
			case PASS_THROUGH -> string;
			case FULLY_FILTERED -> null;
			case PARTIALLY_FILTERED -> {
				char[] cs = string.toCharArray();

				for (int i = 0; i < cs.length && i < this.mask.length(); i++) {
					if (this.mask.get(i)) {
						cs[i] = '#';
					}
				}

				yield new String(cs);
			}
		};
	}

	@Nullable
	public Component applyWithFormatting(String string) {
		return switch (this.type) {
			case PASS_THROUGH -> Component.literal(string);
			case FULLY_FILTERED -> null;
			case PARTIALLY_FILTERED -> {
				MutableComponent mutableComponent = Component.empty();
				int i = 0;
				boolean bl = this.mask.get(0);

				while (true) {
					int j = bl ? this.mask.nextClearBit(i) : this.mask.nextSetBit(i);
					j = j < 0 ? string.length() : j;
					if (j == i) {
						yield mutableComponent;
					}

					if (bl) {
						mutableComponent.append(Component.literal(StringUtils.repeat('#', j - i)).withStyle(FILTERED_STYLE));
					} else {
						mutableComponent.append(string.substring(i, j));
					}

					bl = !bl;
					i = j;
				}
			}
		};
	}

	public boolean isEmpty() {
		return this.type == FilterMask.Type.PASS_THROUGH;
	}

	public boolean isFullyFiltered() {
		return this.type == FilterMask.Type.FULLY_FILTERED;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			FilterMask filterMask = (FilterMask)object;
			return this.mask.equals(filterMask.mask) && this.type == filterMask.type;
		} else {
			return false;
		}
	}

	public int hashCode() {
		int i = this.mask.hashCode();
		return 31 * i + this.type.hashCode();
	}

	static enum Type implements StringRepresentable {
		PASS_THROUGH("pass_through", () -> FilterMask.PASS_THROUGH_CODEC),
		FULLY_FILTERED("fully_filtered", () -> FilterMask.FULLY_FILTERED_CODEC),
		PARTIALLY_FILTERED("partially_filtered", () -> FilterMask.PARTIALLY_FILTERED_CODEC);

		private final String serializedName;
		private final Supplier<MapCodec<FilterMask>> codec;

		private Type(final String string2, final Supplier<MapCodec<FilterMask>> supplier) {
			this.serializedName = string2;
			this.codec = supplier;
		}

		@Override
		public String getSerializedName() {
			return this.serializedName;
		}

		private MapCodec<FilterMask> codec() {
			return (MapCodec<FilterMask>)this.codec.get();
		}
	}
}
