/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SerializableUUID;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.TagTypes;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.material.FluidState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class NbtUtils {
    private static final Comparator<ListTag> YXZ_LISTTAG_INT_COMPARATOR = Comparator.comparingInt(listTag -> listTag.getInt(1)).thenComparingInt(listTag -> listTag.getInt(0)).thenComparingInt(listTag -> listTag.getInt(2));
    private static final Comparator<ListTag> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.comparingDouble(listTag -> listTag.getDouble(1)).thenComparingDouble(listTag -> listTag.getDouble(0)).thenComparingDouble(listTag -> listTag.getDouble(2));
    public static final String SNBT_DATA_TAG = "data";
    private static final char PROPERTIES_START = '{';
    private static final char PROPERTIES_END = '}';
    private static final String ELEMENT_SEPARATOR = ",";
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final Splitter COMMA_SPLITTER = Splitter.on(",");
    private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int INDENT = 2;
    private static final int NOT_FOUND = -1;

    private NbtUtils() {
    }

    @Nullable
    public static GameProfile readGameProfile(CompoundTag compoundTag) {
        String string = null;
        UUID uUID = null;
        if (compoundTag.contains("Name", 8)) {
            string = compoundTag.getString("Name");
        }
        if (compoundTag.hasUUID("Id")) {
            uUID = compoundTag.getUUID("Id");
        }
        try {
            GameProfile gameProfile = new GameProfile(uUID, string);
            if (compoundTag.contains("Properties", 10)) {
                CompoundTag compoundTag2 = compoundTag.getCompound("Properties");
                for (String string2 : compoundTag2.getAllKeys()) {
                    ListTag listTag = compoundTag2.getList(string2, 10);
                    for (int i = 0; i < listTag.size(); ++i) {
                        CompoundTag compoundTag3 = listTag.getCompound(i);
                        String string3 = compoundTag3.getString("Value");
                        if (compoundTag3.contains("Signature", 8)) {
                            gameProfile.getProperties().put(string2, new Property(string2, string3, compoundTag3.getString("Signature")));
                            continue;
                        }
                        gameProfile.getProperties().put(string2, new Property(string2, string3));
                    }
                }
            }
            return gameProfile;
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static CompoundTag writeGameProfile(CompoundTag compoundTag, GameProfile gameProfile) {
        if (!StringUtil.isNullOrEmpty(gameProfile.getName())) {
            compoundTag.putString("Name", gameProfile.getName());
        }
        if (gameProfile.getId() != null) {
            compoundTag.putUUID("Id", gameProfile.getId());
        }
        if (!gameProfile.getProperties().isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            for (String string : gameProfile.getProperties().keySet()) {
                ListTag listTag = new ListTag();
                for (Property property : gameProfile.getProperties().get(string)) {
                    CompoundTag compoundTag3 = new CompoundTag();
                    compoundTag3.putString("Value", property.getValue());
                    if (property.hasSignature()) {
                        compoundTag3.putString("Signature", property.getSignature());
                    }
                    listTag.add(compoundTag3);
                }
                compoundTag2.put(string, listTag);
            }
            compoundTag.put("Properties", compoundTag2);
        }
        return compoundTag;
    }

    @VisibleForTesting
    public static boolean compareNbt(@Nullable Tag tag, @Nullable Tag tag2, boolean bl) {
        if (tag == tag2) {
            return true;
        }
        if (tag == null) {
            return true;
        }
        if (tag2 == null) {
            return false;
        }
        if (!tag.getClass().equals(tag2.getClass())) {
            return false;
        }
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            CompoundTag compoundTag2 = (CompoundTag)tag2;
            for (String string : compoundTag.getAllKeys()) {
                Tag tag3 = compoundTag.get(string);
                if (NbtUtils.compareNbt(tag3, compoundTag2.get(string), bl)) continue;
                return false;
            }
            return true;
        }
        if (tag instanceof ListTag && bl) {
            ListTag listTag = (ListTag)tag;
            ListTag listTag2 = (ListTag)tag2;
            if (listTag.isEmpty()) {
                return listTag2.isEmpty();
            }
            for (int i = 0; i < listTag.size(); ++i) {
                Tag tag4 = listTag.get(i);
                boolean bl2 = false;
                for (int j = 0; j < listTag2.size(); ++j) {
                    if (!NbtUtils.compareNbt(tag4, listTag2.get(j), bl)) continue;
                    bl2 = true;
                    break;
                }
                if (bl2) continue;
                return false;
            }
            return true;
        }
        return tag.equals(tag2);
    }

    public static IntArrayTag createUUID(UUID uUID) {
        return new IntArrayTag(SerializableUUID.uuidToIntArray(uUID));
    }

    public static UUID loadUUID(Tag tag) {
        if (tag.getType() != IntArrayTag.TYPE) {
            throw new IllegalArgumentException("Expected UUID-Tag to be of type " + IntArrayTag.TYPE.getName() + ", but found " + tag.getType().getName() + ".");
        }
        int[] is = ((IntArrayTag)tag).getAsIntArray();
        if (is.length != 4) {
            throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + is.length + ".");
        }
        return SerializableUUID.uuidFromIntArray(is);
    }

    public static BlockPos readBlockPos(CompoundTag compoundTag) {
        return new BlockPos(compoundTag.getInt("X"), compoundTag.getInt("Y"), compoundTag.getInt("Z"));
    }

    public static CompoundTag writeBlockPos(BlockPos blockPos) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("X", blockPos.getX());
        compoundTag.putInt("Y", blockPos.getY());
        compoundTag.putInt("Z", blockPos.getZ());
        return compoundTag;
    }

    public static BlockState readBlockState(CompoundTag compoundTag) {
        if (!compoundTag.contains("Name", 8)) {
            return Blocks.AIR.defaultBlockState();
        }
        Block block = Registry.BLOCK.get(new ResourceLocation(compoundTag.getString("Name")));
        BlockState blockState = block.defaultBlockState();
        if (compoundTag.contains("Properties", 10)) {
            CompoundTag compoundTag2 = compoundTag.getCompound("Properties");
            StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
            for (String string : compoundTag2.getAllKeys()) {
                net.minecraft.world.level.block.state.properties.Property<?> property = stateDefinition.getProperty(string);
                if (property == null) continue;
                blockState = NbtUtils.setValueHelper(blockState, property, string, compoundTag2, compoundTag);
            }
        }
        return blockState;
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S stateHolder, net.minecraft.world.level.block.state.properties.Property<T> property, String string, CompoundTag compoundTag, CompoundTag compoundTag2) {
        Optional<T> optional = property.getValue(compoundTag.getString(string));
        if (optional.isPresent()) {
            return (S)((StateHolder)stateHolder.setValue(property, (Comparable)((Comparable)optional.get())));
        }
        LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", (Object)string, (Object)compoundTag.getString(string), (Object)compoundTag2.toString());
        return stateHolder;
    }

    public static CompoundTag writeBlockState(BlockState blockState) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", Registry.BLOCK.getKey(blockState.getBlock()).toString());
        ImmutableMap<net.minecraft.world.level.block.state.properties.Property<?>, Comparable<?>> immutableMap = blockState.getValues();
        if (!immutableMap.isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            for (Map.Entry entry : immutableMap.entrySet()) {
                net.minecraft.world.level.block.state.properties.Property property = (net.minecraft.world.level.block.state.properties.Property)entry.getKey();
                compoundTag2.putString(property.getName(), NbtUtils.getName(property, (Comparable)entry.getValue()));
            }
            compoundTag.put("Properties", compoundTag2);
        }
        return compoundTag;
    }

    public static CompoundTag writeFluidState(FluidState fluidState) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", Registry.FLUID.getKey(fluidState.getType()).toString());
        ImmutableMap<net.minecraft.world.level.block.state.properties.Property<?>, Comparable<?>> immutableMap = fluidState.getValues();
        if (!immutableMap.isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            for (Map.Entry entry : immutableMap.entrySet()) {
                net.minecraft.world.level.block.state.properties.Property property = (net.minecraft.world.level.block.state.properties.Property)entry.getKey();
                compoundTag2.putString(property.getName(), NbtUtils.getName(property, (Comparable)entry.getValue()));
            }
            compoundTag.put("Properties", compoundTag2);
        }
        return compoundTag;
    }

    private static <T extends Comparable<T>> String getName(net.minecraft.world.level.block.state.properties.Property<T> property, Comparable<?> comparable) {
        return property.getName(comparable);
    }

    public static String prettyPrint(Tag tag) {
        return NbtUtils.prettyPrint(tag, false);
    }

    public static String prettyPrint(Tag tag, boolean bl) {
        return NbtUtils.prettyPrint(new StringBuilder(), tag, 0, bl).toString();
    }

    public static StringBuilder prettyPrint(StringBuilder stringBuilder, Tag tag, int i, boolean bl) {
        switch (tag.getId()) {
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 8: {
                stringBuilder.append(tag);
                break;
            }
            case 0: {
                break;
            }
            case 7: {
                ByteArrayTag byteArrayTag = (ByteArrayTag)tag;
                byte[] bs = byteArrayTag.getAsByteArray();
                int j = bs.length;
                NbtUtils.indent(i, stringBuilder).append("byte[").append(j).append("] {\n");
                if (bl) {
                    NbtUtils.indent(i + 1, stringBuilder);
                    for (int k = 0; k < bs.length; ++k) {
                        if (k != 0) {
                            stringBuilder.append(',');
                        }
                        if (k % 16 == 0 && k / 16 > 0) {
                            stringBuilder.append('\n');
                            if (k < bs.length) {
                                NbtUtils.indent(i + 1, stringBuilder);
                            }
                        } else if (k != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format("0x%02X", bs[k] & 0xFF));
                    }
                } else {
                    NbtUtils.indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(i, stringBuilder).append('}');
                break;
            }
            case 9: {
                ListTag listTag = (ListTag)tag;
                int l = listTag.size();
                byte j = listTag.getElementType();
                String string = j == 0 ? "undefined" : TagTypes.getType(j).getPrettyName();
                NbtUtils.indent(i, stringBuilder).append("list<").append(string).append(">[").append(l).append("] [");
                if (l != 0) {
                    stringBuilder.append('\n');
                }
                for (int m = 0; m < l; ++m) {
                    if (m != 0) {
                        stringBuilder.append(",\n");
                    }
                    NbtUtils.indent(i + 1, stringBuilder);
                    NbtUtils.prettyPrint(stringBuilder, listTag.get(m), i + 1, bl);
                }
                if (l != 0) {
                    stringBuilder.append('\n');
                }
                NbtUtils.indent(i, stringBuilder).append(']');
                break;
            }
            case 11: {
                IntArrayTag intArrayTag = (IntArrayTag)tag;
                int[] is = intArrayTag.getAsIntArray();
                int j = 0;
                int[] string = is;
                int m = string.length;
                for (int k = 0; k < m; ++k) {
                    int n = string[k];
                    j = Math.max(j, String.format("%X", n).length());
                }
                int k = is.length;
                NbtUtils.indent(i, stringBuilder).append("int[").append(k).append("] {\n");
                if (bl) {
                    NbtUtils.indent(i + 1, stringBuilder);
                    for (m = 0; m < is.length; ++m) {
                        if (m != 0) {
                            stringBuilder.append(',');
                        }
                        if (m % 16 == 0 && m / 16 > 0) {
                            stringBuilder.append('\n');
                            if (m < is.length) {
                                NbtUtils.indent(i + 1, stringBuilder);
                            }
                        } else if (m != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format("0x%0" + j + "X", is[m]));
                    }
                } else {
                    NbtUtils.indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(i, stringBuilder).append('}');
                break;
            }
            case 10: {
                CompoundTag compoundTag = (CompoundTag)tag;
                ArrayList<String> list = Lists.newArrayList(compoundTag.getAllKeys());
                Collections.sort(list);
                NbtUtils.indent(i, stringBuilder).append('{');
                if (stringBuilder.length() - stringBuilder.lastIndexOf("\n") > 2 * (i + 1)) {
                    stringBuilder.append('\n');
                    NbtUtils.indent(i + 1, stringBuilder);
                }
                int j = list.stream().mapToInt(String::length).max().orElse(0);
                String string = Strings.repeat(" ", j);
                for (int m = 0; m < list.size(); ++m) {
                    if (m != 0) {
                        stringBuilder.append(",\n");
                    }
                    String string2 = (String)list.get(m);
                    NbtUtils.indent(i + 1, stringBuilder).append('\"').append(string2).append('\"').append(string, 0, string.length() - string2.length()).append(": ");
                    NbtUtils.prettyPrint(stringBuilder, compoundTag.get(string2), i + 1, bl);
                }
                if (!list.isEmpty()) {
                    stringBuilder.append('\n');
                }
                NbtUtils.indent(i, stringBuilder).append('}');
                break;
            }
            case 12: {
                int n;
                LongArrayTag longArrayTag = (LongArrayTag)tag;
                long[] ls = longArrayTag.getAsLongArray();
                long o = 0L;
                long[] m = ls;
                int n2 = m.length;
                for (n = 0; n < n2; ++n) {
                    long p = m[n];
                    o = Math.max(o, (long)String.format("%X", p).length());
                }
                long q = ls.length;
                NbtUtils.indent(i, stringBuilder).append("long[").append(q).append("] {\n");
                if (bl) {
                    NbtUtils.indent(i + 1, stringBuilder);
                    for (n = 0; n < ls.length; ++n) {
                        if (n != 0) {
                            stringBuilder.append(',');
                        }
                        if (n % 16 == 0 && n / 16 > 0) {
                            stringBuilder.append('\n');
                            if (n < ls.length) {
                                NbtUtils.indent(i + 1, stringBuilder);
                            }
                        } else if (n != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format("0x%0" + o + "X", ls[n]));
                    }
                } else {
                    NbtUtils.indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(i, stringBuilder).append('}');
                break;
            }
            default: {
                stringBuilder.append("<UNKNOWN :(>");
            }
        }
        return stringBuilder;
    }

    private static StringBuilder indent(int i, StringBuilder stringBuilder) {
        int j = stringBuilder.lastIndexOf("\n") + 1;
        int k = stringBuilder.length() - j;
        for (int l = 0; l < 2 * i - k; ++l) {
            stringBuilder.append(' ');
        }
        return stringBuilder;
    }

    public static CompoundTag update(DataFixer dataFixer, DataFixTypes dataFixTypes, CompoundTag compoundTag, int i) {
        return NbtUtils.update(dataFixer, dataFixTypes, compoundTag, i, SharedConstants.getCurrentVersion().getWorldVersion());
    }

    public static CompoundTag update(DataFixer dataFixer, DataFixTypes dataFixTypes, CompoundTag compoundTag, int i, int j) {
        return dataFixer.update(dataFixTypes.getType(), new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag), i, j).getValue();
    }

    public static Component toPrettyComponent(Tag tag) {
        return new TextComponentTagVisitor("", 0).visit(tag);
    }

    public static String structureToSnbt(CompoundTag compoundTag) {
        return new SnbtPrinterTagVisitor().visit(NbtUtils.packStructureTemplate(compoundTag));
    }

    public static CompoundTag snbtToStructure(String string) throws CommandSyntaxException {
        return NbtUtils.unpackStructureTemplate(TagParser.parseTag(string));
    }

    @VisibleForTesting
    static CompoundTag packStructureTemplate(CompoundTag compoundTag2) {
        ListTag listTag4;
        ListTag listTag32;
        boolean bl = compoundTag2.contains("palettes", 9);
        ListTag listTag = bl ? compoundTag2.getList("palettes", 9).getList(0) : compoundTag2.getList("palette", 10);
        ListTag listTag2 = listTag.stream().map(CompoundTag.class::cast).map(NbtUtils::packBlockState).map(StringTag::valueOf).collect(Collectors.toCollection(ListTag::new));
        compoundTag2.put("palette", listTag2);
        if (bl) {
            listTag32 = new ListTag();
            listTag4 = compoundTag2.getList("palettes", 9);
            listTag4.stream().map(ListTag.class::cast).forEach(listTag3 -> {
                CompoundTag compoundTag = new CompoundTag();
                for (int i = 0; i < listTag3.size(); ++i) {
                    compoundTag.putString(listTag2.getString(i), NbtUtils.packBlockState(listTag3.getCompound(i)));
                }
                listTag32.add(compoundTag);
            });
            compoundTag2.put("palettes", listTag32);
        }
        if (compoundTag2.contains("entities", 10)) {
            listTag32 = compoundTag2.getList("entities", 10);
            listTag4 = listTag32.stream().map(CompoundTag.class::cast).sorted(Comparator.comparing(compoundTag -> compoundTag.getList("pos", 6), YXZ_LISTTAG_DOUBLE_COMPARATOR)).collect(Collectors.toCollection(ListTag::new));
            compoundTag2.put("entities", listTag4);
        }
        listTag32 = compoundTag2.getList("blocks", 10).stream().map(CompoundTag.class::cast).sorted(Comparator.comparing(compoundTag -> compoundTag.getList("pos", 3), YXZ_LISTTAG_INT_COMPARATOR)).peek(compoundTag -> compoundTag.putString("state", listTag2.getString(compoundTag.getInt("state")))).collect(Collectors.toCollection(ListTag::new));
        compoundTag2.put(SNBT_DATA_TAG, listTag32);
        compoundTag2.remove("blocks");
        return compoundTag2;
    }

    @VisibleForTesting
    static CompoundTag unpackStructureTemplate(CompoundTag compoundTag2) {
        ListTag listTag = compoundTag2.getList("palette", 8);
        Map map = listTag.stream().map(StringTag.class::cast).map(StringTag::getAsString).collect(ImmutableMap.toImmutableMap(Function.identity(), NbtUtils::unpackBlockState));
        if (compoundTag2.contains("palettes", 9)) {
            compoundTag2.put("palettes", compoundTag2.getList("palettes", 10).stream().map(CompoundTag.class::cast).map(compoundTag -> map.keySet().stream().map(compoundTag::getString).map(NbtUtils::unpackBlockState).collect(Collectors.toCollection(ListTag::new))).collect(Collectors.toCollection(ListTag::new)));
            compoundTag2.remove("palette");
        } else {
            compoundTag2.put("palette", map.values().stream().collect(Collectors.toCollection(ListTag::new)));
        }
        if (compoundTag2.contains(SNBT_DATA_TAG, 9)) {
            Object2IntOpenHashMap<String> object2IntMap = new Object2IntOpenHashMap<String>();
            object2IntMap.defaultReturnValue(-1);
            for (int i = 0; i < listTag.size(); ++i) {
                object2IntMap.put(listTag.getString(i), i);
            }
            ListTag listTag2 = compoundTag2.getList(SNBT_DATA_TAG, 10);
            for (int j = 0; j < listTag2.size(); ++j) {
                CompoundTag compoundTag22 = listTag2.getCompound(j);
                String string = compoundTag22.getString("state");
                int k = object2IntMap.getInt(string);
                if (k == -1) {
                    throw new IllegalStateException("Entry " + string + " missing from palette");
                }
                compoundTag22.putInt("state", k);
            }
            compoundTag2.put("blocks", listTag2);
            compoundTag2.remove(SNBT_DATA_TAG);
        }
        return compoundTag2;
    }

    @VisibleForTesting
    static String packBlockState(CompoundTag compoundTag) {
        StringBuilder stringBuilder = new StringBuilder(compoundTag.getString("Name"));
        if (compoundTag.contains("Properties", 10)) {
            CompoundTag compoundTag2 = compoundTag.getCompound("Properties");
            String string2 = compoundTag2.getAllKeys().stream().sorted().map(string -> string + ':' + compoundTag2.get((String)string).getAsString()).collect(Collectors.joining(ELEMENT_SEPARATOR));
            stringBuilder.append('{').append(string2).append('}');
        }
        return stringBuilder.toString();
    }

    @VisibleForTesting
    static CompoundTag unpackBlockState(String string) {
        String string22;
        CompoundTag compoundTag = new CompoundTag();
        int i = string.indexOf(123);
        if (i >= 0) {
            string22 = string.substring(0, i);
            CompoundTag compoundTag2 = new CompoundTag();
            if (i + 2 <= string.length()) {
                String string3 = string.substring(i + 1, string.indexOf(125, i));
                COMMA_SPLITTER.split(string3).forEach(string2 -> {
                    List<String> list = COLON_SPLITTER.splitToList((CharSequence)string2);
                    if (list.size() == 2) {
                        compoundTag2.putString(list.get(0), list.get(1));
                    } else {
                        LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", (Object)string);
                    }
                });
                compoundTag.put("Properties", compoundTag2);
            }
        } else {
            string22 = string;
        }
        compoundTag.putString("Name", string22);
        return compoundTag;
    }
}

