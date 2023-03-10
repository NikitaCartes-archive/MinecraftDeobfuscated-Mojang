/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.chat.Component;

public class TagParser {
    public static final SimpleCommandExceptionType ERROR_TRAILING_DATA = new SimpleCommandExceptionType(Component.translatable("argument.nbt.trailing"));
    public static final SimpleCommandExceptionType ERROR_EXPECTED_KEY = new SimpleCommandExceptionType(Component.translatable("argument.nbt.expected.key"));
    public static final SimpleCommandExceptionType ERROR_EXPECTED_VALUE = new SimpleCommandExceptionType(Component.translatable("argument.nbt.expected.value"));
    public static final Dynamic2CommandExceptionType ERROR_INSERT_MIXED_LIST = new Dynamic2CommandExceptionType((object, object2) -> Component.translatable("argument.nbt.list.mixed", object, object2));
    public static final Dynamic2CommandExceptionType ERROR_INSERT_MIXED_ARRAY = new Dynamic2CommandExceptionType((object, object2) -> Component.translatable("argument.nbt.array.mixed", object, object2));
    public static final DynamicCommandExceptionType ERROR_INVALID_ARRAY = new DynamicCommandExceptionType(object -> Component.translatable("argument.nbt.array.invalid", object));
    public static final char ELEMENT_SEPARATOR = ',';
    public static final char NAME_VALUE_SEPARATOR = ':';
    private static final char LIST_OPEN = '[';
    private static final char LIST_CLOSE = ']';
    private static final char STRUCT_CLOSE = '}';
    private static final char STRUCT_OPEN = '{';
    private static final Pattern DOUBLE_PATTERN_NOSUFFIX = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", 2);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", 2);
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", 2);
    private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", 2);
    private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", 2);
    private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", 2);
    private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");
    private final StringReader reader;

    public static CompoundTag parseTag(String string) throws CommandSyntaxException {
        return new TagParser(new StringReader(string)).readSingleStruct();
    }

    @VisibleForTesting
    CompoundTag readSingleStruct() throws CommandSyntaxException {
        CompoundTag compoundTag = this.readStruct();
        this.reader.skipWhitespace();
        if (this.reader.canRead()) {
            throw ERROR_TRAILING_DATA.createWithContext(this.reader);
        }
        return compoundTag;
    }

    public TagParser(StringReader stringReader) {
        this.reader = stringReader;
    }

    protected String readKey() throws CommandSyntaxException {
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw ERROR_EXPECTED_KEY.createWithContext(this.reader);
        }
        return this.reader.readString();
    }

    protected Tag readTypedValue() throws CommandSyntaxException {
        this.reader.skipWhitespace();
        int i = this.reader.getCursor();
        if (StringReader.isQuotedStringStart(this.reader.peek())) {
            return StringTag.valueOf(this.reader.readQuotedString());
        }
        String string = this.reader.readUnquotedString();
        if (string.isEmpty()) {
            this.reader.setCursor(i);
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        }
        return this.type(string);
    }

    private Tag type(String string) {
        try {
            if (FLOAT_PATTERN.matcher(string).matches()) {
                return FloatTag.valueOf(Float.parseFloat(string.substring(0, string.length() - 1)));
            }
            if (BYTE_PATTERN.matcher(string).matches()) {
                return ByteTag.valueOf(Byte.parseByte(string.substring(0, string.length() - 1)));
            }
            if (LONG_PATTERN.matcher(string).matches()) {
                return LongTag.valueOf(Long.parseLong(string.substring(0, string.length() - 1)));
            }
            if (SHORT_PATTERN.matcher(string).matches()) {
                return ShortTag.valueOf(Short.parseShort(string.substring(0, string.length() - 1)));
            }
            if (INT_PATTERN.matcher(string).matches()) {
                return IntTag.valueOf(Integer.parseInt(string));
            }
            if (DOUBLE_PATTERN.matcher(string).matches()) {
                return DoubleTag.valueOf(Double.parseDouble(string.substring(0, string.length() - 1)));
            }
            if (DOUBLE_PATTERN_NOSUFFIX.matcher(string).matches()) {
                return DoubleTag.valueOf(Double.parseDouble(string));
            }
            if ("true".equalsIgnoreCase(string)) {
                return ByteTag.ONE;
            }
            if ("false".equalsIgnoreCase(string)) {
                return ByteTag.ZERO;
            }
        } catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        return StringTag.valueOf(string);
    }

    public Tag readValue() throws CommandSyntaxException {
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        }
        char c = this.reader.peek();
        if (c == '{') {
            return this.readStruct();
        }
        if (c == '[') {
            return this.readList();
        }
        return this.readTypedValue();
    }

    protected Tag readList() throws CommandSyntaxException {
        if (this.reader.canRead(3) && !StringReader.isQuotedStringStart(this.reader.peek(1)) && this.reader.peek(2) == ';') {
            return this.readArrayTag();
        }
        return this.readListTag();
    }

    public CompoundTag readStruct() throws CommandSyntaxException {
        this.expect('{');
        CompoundTag compoundTag = new CompoundTag();
        this.reader.skipWhitespace();
        while (this.reader.canRead() && this.reader.peek() != '}') {
            int i = this.reader.getCursor();
            String string = this.readKey();
            if (string.isEmpty()) {
                this.reader.setCursor(i);
                throw ERROR_EXPECTED_KEY.createWithContext(this.reader);
            }
            this.expect(':');
            compoundTag.put(string, this.readValue());
            if (!this.hasElementSeparator()) break;
            if (this.reader.canRead()) continue;
            throw ERROR_EXPECTED_KEY.createWithContext(this.reader);
        }
        this.expect('}');
        return compoundTag;
    }

    private Tag readListTag() throws CommandSyntaxException {
        this.expect('[');
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        }
        ListTag listTag = new ListTag();
        TagType<?> tagType = null;
        while (this.reader.peek() != ']') {
            int i = this.reader.getCursor();
            Tag tag = this.readValue();
            TagType<?> tagType2 = tag.getType();
            if (tagType == null) {
                tagType = tagType2;
            } else if (tagType2 != tagType) {
                this.reader.setCursor(i);
                throw ERROR_INSERT_MIXED_LIST.createWithContext(this.reader, tagType2.getPrettyName(), tagType.getPrettyName());
            }
            listTag.add(tag);
            if (!this.hasElementSeparator()) break;
            if (this.reader.canRead()) continue;
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        }
        this.expect(']');
        return listTag;
    }

    private Tag readArrayTag() throws CommandSyntaxException {
        this.expect('[');
        int i = this.reader.getCursor();
        char c = this.reader.read();
        this.reader.read();
        this.reader.skipWhitespace();
        if (!this.reader.canRead()) {
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        }
        if (c == 'B') {
            return new ByteArrayTag(this.readArray(ByteArrayTag.TYPE, ByteTag.TYPE));
        }
        if (c == 'L') {
            return new LongArrayTag(this.readArray(LongArrayTag.TYPE, LongTag.TYPE));
        }
        if (c == 'I') {
            return new IntArrayTag(this.readArray(IntArrayTag.TYPE, IntTag.TYPE));
        }
        this.reader.setCursor(i);
        throw ERROR_INVALID_ARRAY.createWithContext(this.reader, String.valueOf(c));
    }

    private <T extends Number> List<T> readArray(TagType<?> tagType, TagType<?> tagType2) throws CommandSyntaxException {
        ArrayList<Number> list = Lists.newArrayList();
        while (this.reader.peek() != ']') {
            int i = this.reader.getCursor();
            Tag tag = this.readValue();
            TagType<?> tagType3 = tag.getType();
            if (tagType3 != tagType2) {
                this.reader.setCursor(i);
                throw ERROR_INSERT_MIXED_ARRAY.createWithContext(this.reader, tagType3.getPrettyName(), tagType.getPrettyName());
            }
            if (tagType2 == ByteTag.TYPE) {
                list.add(((NumericTag)tag).getAsByte());
            } else if (tagType2 == LongTag.TYPE) {
                list.add(((NumericTag)tag).getAsLong());
            } else {
                list.add(((NumericTag)tag).getAsInt());
            }
            if (!this.hasElementSeparator()) break;
            if (this.reader.canRead()) continue;
            throw ERROR_EXPECTED_VALUE.createWithContext(this.reader);
        }
        this.expect(']');
        return list;
    }

    private boolean hasElementSeparator() {
        this.reader.skipWhitespace();
        if (this.reader.canRead() && this.reader.peek() == ',') {
            this.reader.skip();
            this.reader.skipWhitespace();
            return true;
        }
        return false;
    }

    private void expect(char c) throws CommandSyntaxException {
        this.reader.skipWhitespace();
        this.reader.expect(c);
    }
}

