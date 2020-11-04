/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextComponentTagVisitor
implements TagVisitor {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ByteCollection INLINE_ELEMENT_TYPES = new ByteOpenHashSet(Arrays.asList((byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6));
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_KEY = ChatFormatting.AQUA;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_STRING = ChatFormatting.GREEN;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER = ChatFormatting.GOLD;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER_TYPE = ChatFormatting.RED;
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String NAME_VALUE_SEPARATOR = String.valueOf(':');
    private static final String ELEMENT_SEPARATOR = String.valueOf(',');
    private final String indentation;
    private final int depth;
    private Component result;

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
        MutableComponent component = new TextComponent(string.substring(1, string.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_STRING);
        this.result = new TextComponent(string2).append(component).append(string2);
    }

    @Override
    public void visitByte(ByteTag byteTag) {
        MutableComponent component = new TextComponent("b").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = new TextComponent(String.valueOf(byteTag.getAsNumber())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitShort(ShortTag shortTag) {
        MutableComponent component = new TextComponent("s").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = new TextComponent(String.valueOf(shortTag.getAsNumber())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitInt(IntTag intTag) {
        this.result = new TextComponent(String.valueOf(intTag.getAsNumber())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitLong(LongTag longTag) {
        MutableComponent component = new TextComponent("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = new TextComponent(String.valueOf(longTag.getAsNumber())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitFloat(FloatTag floatTag) {
        MutableComponent component = new TextComponent("f").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = new TextComponent(String.valueOf(floatTag.getAsFloat())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitDouble(DoubleTag doubleTag) {
        MutableComponent component = new TextComponent("d").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = new TextComponent(String.valueOf(doubleTag.getAsDouble())).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitByteArray(ByteArrayTag byteArrayTag) {
        MutableComponent component = new TextComponent("B").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent mutableComponent = new TextComponent("[").append(component).append(";");
        byte[] bs = byteArrayTag.getAsByteArray();
        for (int i = 0; i < bs.length; ++i) {
            MutableComponent mutableComponent2 = new TextComponent(String.valueOf(bs[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            mutableComponent.append(" ").append(mutableComponent2).append(component);
            if (i == bs.length - 1) continue;
            mutableComponent.append(ELEMENT_SEPARATOR);
        }
        mutableComponent.append("]");
        this.result = mutableComponent;
    }

    @Override
    public void visitIntArray(IntArrayTag intArrayTag) {
        MutableComponent component = new TextComponent("I").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent mutableComponent = new TextComponent("[").append(component).append(";");
        int[] is = intArrayTag.getAsIntArray();
        for (int i = 0; i < is.length; ++i) {
            mutableComponent.append(" ").append(new TextComponent(String.valueOf(is[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
            if (i == is.length - 1) continue;
            mutableComponent.append(ELEMENT_SEPARATOR);
        }
        mutableComponent.append("]");
        this.result = mutableComponent;
    }

    @Override
    public void visitLongArray(LongArrayTag longArrayTag) {
        MutableComponent component = new TextComponent("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent mutableComponent = new TextComponent("[").append(component).append(";");
        long[] ls = longArrayTag.getAsLongArray();
        for (int i = 0; i < ls.length; ++i) {
            MutableComponent component2 = new TextComponent(String.valueOf(ls[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            mutableComponent.append(" ").append(component2).append(component);
            if (i == ls.length - 1) continue;
            mutableComponent.append(ELEMENT_SEPARATOR);
        }
        mutableComponent.append("]");
        this.result = mutableComponent;
    }

    @Override
    public void visitList(ListTag listTag) {
        if (listTag.isEmpty()) {
            this.result = new TextComponent("[]");
            return;
        }
        if (INLINE_ELEMENT_TYPES.contains(listTag.getElementType()) && listTag.size() <= 8) {
            String string = ELEMENT_SEPARATOR + " ";
            TextComponent mutableComponent = new TextComponent("[");
            for (int i = 0; i < listTag.size(); ++i) {
                if (i != 0) {
                    mutableComponent.append(string);
                }
                mutableComponent.append(new TextComponentTagVisitor(this.indentation, this.depth).visit(listTag.get(i)));
            }
            mutableComponent.append("]");
            this.result = mutableComponent;
            return;
        }
        TextComponent mutableComponent2 = new TextComponent("[");
        if (!this.indentation.isEmpty()) {
            mutableComponent2.append("\n");
        }
        for (int j = 0; j < listTag.size(); ++j) {
            TextComponent mutableComponent3 = new TextComponent(Strings.repeat(this.indentation, this.depth + 1));
            mutableComponent3.append(new TextComponentTagVisitor(this.indentation, this.depth + 1).visit(listTag.get(j)));
            if (j != listTag.size() - 1) {
                mutableComponent3.append(ELEMENT_SEPARATOR).append(this.indentation.isEmpty() ? " " : "\n");
            }
            mutableComponent2.append(mutableComponent3);
        }
        if (!this.indentation.isEmpty()) {
            mutableComponent2.append("\n").append(Strings.repeat(this.indentation, this.depth));
        }
        mutableComponent2.append("]");
        this.result = mutableComponent2;
    }

    @Override
    public void visitCompound(CompoundTag compoundTag) {
        if (compoundTag.isEmpty()) {
            this.result = new TextComponent("{}");
            return;
        }
        TextComponent mutableComponent = new TextComponent("{");
        Collection<String> collection = compoundTag.getAllKeys();
        if (LOGGER.isDebugEnabled()) {
            ArrayList<String> list = Lists.newArrayList(compoundTag.getAllKeys());
            Collections.sort(list);
            collection = list;
        }
        if (!this.indentation.isEmpty()) {
            mutableComponent.append("\n");
        }
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            String string = (String)iterator.next();
            MutableComponent mutableComponent2 = new TextComponent(Strings.repeat(this.indentation, this.depth + 1)).append(TextComponentTagVisitor.handleEscapePretty(string)).append(NAME_VALUE_SEPARATOR).append(" ").append(new TextComponentTagVisitor(this.indentation, this.depth + 1).visit(compoundTag.get(string)));
            if (iterator.hasNext()) {
                mutableComponent2.append(ELEMENT_SEPARATOR).append(this.indentation.isEmpty() ? " " : "\n");
            }
            mutableComponent.append(mutableComponent2);
        }
        if (!this.indentation.isEmpty()) {
            mutableComponent.append("\n").append(Strings.repeat(this.indentation, this.depth));
        }
        mutableComponent.append("}");
        this.result = mutableComponent;
    }

    protected static Component handleEscapePretty(String string) {
        if (SIMPLE_VALUE.matcher(string).matches()) {
            return new TextComponent(string).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        }
        String string2 = StringTag.quoteAndEscape(string);
        String string3 = string2.substring(0, 1);
        MutableComponent component = new TextComponent(string2.substring(1, string2.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        return new TextComponent(string3).append(component).append(string3);
    }

    @Override
    public void visitEnd(EndTag endTag) {
        this.result = TextComponent.EMPTY;
    }
}

