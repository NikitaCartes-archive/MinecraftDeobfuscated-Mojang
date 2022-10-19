/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.BitSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class FilterMask {
    public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), Type.FULLY_FILTERED);
    public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), Type.PASS_THROUGH);
    public static final Style FILTERED_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.filtered")));
    private static final char HASH = '#';
    private final BitSet mask;
    private final Type type;

    private FilterMask(BitSet bitSet, Type type) {
        this.mask = bitSet;
        this.type = type;
    }

    public FilterMask(int i) {
        this(new BitSet(i), Type.PARTIALLY_FILTERED);
    }

    public static FilterMask read(FriendlyByteBuf friendlyByteBuf) {
        Type type = friendlyByteBuf.readEnum(Type.class);
        return switch (type) {
            default -> throw new IncompatibleClassChangeError();
            case Type.PASS_THROUGH -> PASS_THROUGH;
            case Type.FULLY_FILTERED -> FULLY_FILTERED;
            case Type.PARTIALLY_FILTERED -> new FilterMask(friendlyByteBuf.readBitSet(), Type.PARTIALLY_FILTERED);
        };
    }

    public static void write(FriendlyByteBuf friendlyByteBuf, FilterMask filterMask) {
        friendlyByteBuf.writeEnum(filterMask.type);
        if (filterMask.type == Type.PARTIALLY_FILTERED) {
            friendlyByteBuf.writeBitSet(filterMask.mask);
        }
    }

    public void setFiltered(int i) {
        this.mask.set(i);
    }

    @Nullable
    public String apply(String string) {
        return switch (this.type) {
            default -> throw new IncompatibleClassChangeError();
            case Type.FULLY_FILTERED -> null;
            case Type.PASS_THROUGH -> string;
            case Type.PARTIALLY_FILTERED -> {
                char[] cs = string.toCharArray();
                for (int i = 0; i < cs.length && i < this.mask.length(); ++i) {
                    if (!this.mask.get(i)) continue;
                    cs[i] = 35;
                }
                yield new String(cs);
            }
        };
    }

    @Nullable
    public Component applyWithFormatting(String string) {
        return switch (this.type) {
            default -> throw new IncompatibleClassChangeError();
            case Type.FULLY_FILTERED -> null;
            case Type.PASS_THROUGH -> Component.literal(string);
            case Type.PARTIALLY_FILTERED -> {
                MutableComponent mutableComponent = Component.empty();
                int i = 0;
                boolean bl = this.mask.get(0);
                while (true) {
                    int j = bl ? this.mask.nextClearBit(i) : this.mask.nextSetBit(i);
                    int v1 = j = j < 0 ? string.length() : j;
                    if (j == i) break;
                    if (bl) {
                        mutableComponent.append(Component.literal(StringUtils.repeat('#', j - i)).withStyle(FILTERED_STYLE));
                    } else {
                        mutableComponent.append(string.substring(i, j));
                    }
                    bl = !bl;
                    i = j;
                }
                yield mutableComponent;
            }
        };
    }

    public boolean isEmpty() {
        return this.type == Type.PASS_THROUGH;
    }

    public boolean isFullyFiltered() {
        return this.type == Type.FULLY_FILTERED;
    }

    static enum Type {
        PASS_THROUGH,
        FULLY_FILTERED,
        PARTIALLY_FILTERED;

    }
}

