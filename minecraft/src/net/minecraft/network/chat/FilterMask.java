package net.minecraft.network.chat;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.StringUtils;

public class FilterMask {
	public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), FilterMask.Type.FULLY_FILTERED);
	public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), FilterMask.Type.PASS_THROUGH);
	public static final Style FILTERED_STYLE = Style.EMPTY
		.withColor(ChatFormatting.DARK_GRAY)
		.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.filtered")));
	private static final char HASH = '#';
	private final BitSet mask;
	private final FilterMask.Type type;

	private FilterMask(BitSet bitSet, FilterMask.Type type) {
		this.mask = bitSet;
		this.type = type;
	}

	public FilterMask(int i) {
		this(new BitSet(i), FilterMask.Type.PARTIALLY_FILTERED);
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

	static enum Type {
		PASS_THROUGH,
		FULLY_FILTERED,
		PARTIALLY_FILTERED;
	}
}
