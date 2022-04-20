/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;

public class TextComponentTagVisitor
implements TagVisitor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int INLINE_LIST_THRESHOLD = 8;
    private static final ByteCollection INLINE_ELEMENT_TYPES = new ByteOpenHashSet(Arrays.asList((byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6));
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_KEY = ChatFormatting.AQUA;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_STRING = ChatFormatting.GREEN;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER = ChatFormatting.GOLD;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER_TYPE = ChatFormatting.RED;
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String NAME_VALUE_SEPARATOR = String.valueOf(':');
    private static final String ELEMENT_SEPARATOR = String.valueOf(',');
    private static final String LIST_OPEN = "[";
    private static final String LIST_CLOSE = "]";
    private static final String LIST_TYPE_SEPARATOR = ";";
    private static final String ELEMENT_SPACING = " ";
    private static final String STRUCT_OPEN = "{";
    private static final String STRUCT_CLOSE = "}";
    private static final String NEWLINE = "\n";
    private final String indentation;
    private final int depth;
    private Component result = CommonComponents.EMPTY;

    public TextComponentTagVisitor(String string, int i) {
        this.indentation = string;
        this.depth = i;
    }

    public Component visit(Tag tag) {
        tag.accept(this);
        return this.result;
    }

    @Override
    public void visitString(StringTag stringTag) {
        String string = StringTag.quoteAndEscape(stringTag.getAsString());
        String string2 = string.substring(0, 1);
        MutableComponent component = Component.literal(string.substring(1, string.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_STRING);
        this.result = Component.literal(string2).append(component).append(string2);
    }

    @Override
    public void visitByte(ByteTag byteTag) {
        MutableComponent component = Component.literal("b").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = Component.literal(String.valueOf(byteTag.getAsNumber())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitShort(ShortTag shortTag) {
        MutableComponent component = Component.literal("s").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = Component.literal(String.valueOf(shortTag.getAsNumber())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitInt(IntTag intTag) {
        this.result = Component.literal(String.valueOf(intTag.getAsNumber())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitLong(LongTag longTag) {
        MutableComponent component = Component.literal("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = Component.literal(String.valueOf(longTag.getAsNumber())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitFloat(FloatTag floatTag) {
        MutableComponent component = Component.literal("f").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = Component.literal(String.valueOf(floatTag.getAsFloat())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitDouble(DoubleTag doubleTag) {
        MutableComponent component = Component.literal("d").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = Component.literal(String.valueOf(doubleTag.getAsDouble())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitByteArray(ByteArrayTag byteArrayTag) {
        MutableComponent component = Component.literal("B").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent mutableComponent = Component.literal(LIST_OPEN).append(component).append(LIST_TYPE_SEPARATOR);
        byte[] bs = byteArrayTag.getAsByteArray();
        for (int i = 0; i < bs.length; ++i) {
            MutableComponent mutableComponent2 = Component.literal(String.valueOf(bs[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            mutableComponent.append(ELEMENT_SPACING).append(mutableComponent2).append(component);
            if (i == bs.length - 1) continue;
            mutableComponent.append(ELEMENT_SEPARATOR);
        }
        mutableComponent.append(LIST_CLOSE);
        this.result = mutableComponent;
    }

    @Override
    public void visitIntArray(IntArrayTag intArrayTag) {
        MutableComponent component = Component.literal("I").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent mutableComponent = Component.literal(LIST_OPEN).append(component).append(LIST_TYPE_SEPARATOR);
        int[] is = intArrayTag.getAsIntArray();
        for (int i = 0; i < is.length; ++i) {
            mutableComponent.append(ELEMENT_SPACING).append(Component.literal(String.valueOf(is[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
            if (i == is.length - 1) continue;
            mutableComponent.append(ELEMENT_SEPARATOR);
        }
        mutableComponent.append(LIST_CLOSE);
        this.result = mutableComponent;
    }

    @Override
    public void visitLongArray(LongArrayTag longArrayTag) {
        MutableComponent component = Component.literal("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent mutableComponent = Component.literal(LIST_OPEN).append(component).append(LIST_TYPE_SEPARATOR);
        long[] ls = longArrayTag.getAsLongArray();
        for (int i = 0; i < ls.length; ++i) {
            MutableComponent component2 = Component.literal(String.valueOf(ls[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            mutableComponent.append(ELEMENT_SPACING).append(component2).append(component);
            if (i == ls.length - 1) continue;
            mutableComponent.append(ELEMENT_SEPARATOR);
        }
        mutableComponent.append(LIST_CLOSE);
        this.result = mutableComponent;
    }

    @Override
    public void visitList(ListTag listTag) {
        if (listTag.isEmpty()) {
            this.result = Component.literal("[]");
            return;
        }
        if (INLINE_ELEMENT_TYPES.contains(listTag.getElementType()) && listTag.size() <= 8) {
            String string = ELEMENT_SEPARATOR + ELEMENT_SPACING;
            MutableComponent mutableComponent = Component.literal(LIST_OPEN);
            for (int i = 0; i < listTag.size(); ++i) {
                if (i != 0) {
                    mutableComponent.append(string);
                }
                mutableComponent.append(new TextComponentTagVisitor(this.indentation, this.depth).visit(listTag.get(i)));
            }
            mutableComponent.append(LIST_CLOSE);
            this.result = mutableComponent;
            return;
        }
        MutableComponent mutableComponent2 = Component.literal(LIST_OPEN);
        if (!this.indentation.isEmpty()) {
            mutableComponent2.append(NEWLINE);
        }
        for (int j = 0; j < listTag.size(); ++j) {
            MutableComponent mutableComponent3 = Component.literal(Strings.repeat(this.indentation, this.depth + 1));
            mutableComponent3.append(new TextComponentTagVisitor(this.indentation, this.depth + 1).visit(listTag.get(j)));
            if (j != listTag.size() - 1) {
                mutableComponent3.append(ELEMENT_SEPARATOR).append(this.indentation.isEmpty() ? ELEMENT_SPACING : NEWLINE);
            }
            mutableComponent2.append(mutableComponent3);
        }
        if (!this.indentation.isEmpty()) {
            mutableComponent2.append(NEWLINE).append(Strings.repeat(this.indentation, this.depth));
        }
        mutableComponent2.append(LIST_CLOSE);
        this.result = mutableComponent2;
    }

    @Override
    public void visitCompound(CompoundTag compoundTag) {
        if (compoundTag.isEmpty()) {
            this.result = Component.literal("{}");
            return;
        }
        MutableComponent mutableComponent = Component.literal(STRUCT_OPEN);
        Collection<String> collection = compoundTag.getAllKeys();
        if (LOGGER.isDebugEnabled()) {
            ArrayList<String> list = Lists.newArrayList(compoundTag.getAllKeys());
            Collections.sort(list);
            collection = list;
        }
        if (!this.indentation.isEmpty()) {
            mutableComponent.append(NEWLINE);
        }
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            String string = (String)iterator.next();
            MutableComponent mutableComponent2 = Component.literal(Strings.repeat(this.indentation, this.depth + 1)).append(TextComponentTagVisitor.handleEscapePretty(string)).append(NAME_VALUE_SEPARATOR).append(ELEMENT_SPACING).append(new TextComponentTagVisitor(this.indentation, this.depth + 1).visit(compoundTag.get(string)));
            if (iterator.hasNext()) {
                mutableComponent2.append(ELEMENT_SEPARATOR).append(this.indentation.isEmpty() ? ELEMENT_SPACING : NEWLINE);
            }
            mutableComponent.append(mutableComponent2);
        }
        if (!this.indentation.isEmpty()) {
            mutableComponent.append(NEWLINE).append(Strings.repeat(this.indentation, this.depth));
        }
        mutableComponent.append(STRUCT_CLOSE);
        this.result = mutableComponent;
    }

    protected static Component handleEscapePretty(String string) {
        if (SIMPLE_VALUE.matcher(string).matches()) {
            return Component.literal(string).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        }
        String string2 = StringTag.quoteAndEscape(string);
        String string3 = string2.substring(0, 1);
        MutableComponent component = Component.literal(string2.substring(1, string2.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        return Component.literal(string3).append(component).append(string3);
    }

    @Override
    public void visitEnd(EndTag endTag) {
        this.result = CommonComponents.EMPTY;
    }
}

