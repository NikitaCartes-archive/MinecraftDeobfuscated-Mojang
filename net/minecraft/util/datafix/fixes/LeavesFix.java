/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.datafix.PackedBitStorage;
import net.minecraft.util.datafix.fixes.References;
import org.jetbrains.annotations.Nullable;

public class LeavesFix
extends DataFix {
    private static final int NORTH_WEST_MASK = 128;
    private static final int WEST_MASK = 64;
    private static final int SOUTH_WEST_MASK = 32;
    private static final int SOUTH_MASK = 16;
    private static final int SOUTH_EAST_MASK = 8;
    private static final int EAST_MASK = 4;
    private static final int NORTH_EAST_MASK = 2;
    private static final int NORTH_MASK = 1;
    private static final int[][] DIRECTIONS = new int[][]{{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
    private static final int DECAY_DISTANCE = 7;
    private static final int SIZE_BITS = 12;
    private static final int SIZE = 4096;
    static final Object2IntMap<String> LEAVES = DataFixUtils.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> {
        object2IntOpenHashMap.put("minecraft:acacia_leaves", 0);
        object2IntOpenHashMap.put("minecraft:birch_leaves", 1);
        object2IntOpenHashMap.put("minecraft:dark_oak_leaves", 2);
        object2IntOpenHashMap.put("minecraft:jungle_leaves", 3);
        object2IntOpenHashMap.put("minecraft:oak_leaves", 4);
        object2IntOpenHashMap.put("minecraft:spruce_leaves", 5);
    });
    static final Set<String> LOGS = ImmutableSet.of("minecraft:acacia_bark", "minecraft:birch_bark", "minecraft:dark_oak_bark", "minecraft:jungle_bark", "minecraft:oak_bark", "minecraft:spruce_bark", new String[]{"minecraft:acacia_log", "minecraft:birch_log", "minecraft:dark_oak_log", "minecraft:jungle_log", "minecraft:oak_log", "minecraft:spruce_log", "minecraft:stripped_acacia_log", "minecraft:stripped_birch_log", "minecraft:stripped_dark_oak_log", "minecraft:stripped_jungle_log", "minecraft:stripped_oak_log", "minecraft:stripped_spruce_log"});

    public LeavesFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> opticFinder = type.findField("Level");
        OpticFinder<?> opticFinder2 = opticFinder.type().findField("Sections");
        Type<?> type2 = opticFinder2.type();
        if (!(type2 instanceof List.ListType)) {
            throw new IllegalStateException("Expecting sections to be a list.");
        }
        Type type3 = ((List.ListType)type2).getElement();
        OpticFinder opticFinder3 = DSL.typeFinder(type3);
        return this.fixTypeEverywhereTyped("Leaves fix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> {
            int[] is = new int[]{0};
            Typed<?> typed22 = typed.updateTyped(opticFinder2, typed2 -> {
                int m;
                int l;
                Int2ObjectOpenHashMap<LeavesSection> int2ObjectMap = new Int2ObjectOpenHashMap<LeavesSection>(typed2.getAllTyped(opticFinder3).stream().map(typed -> new LeavesSection((Typed<?>)typed, this.getInputSchema())).collect(Collectors.toMap(Section::getIndex, leavesSection -> leavesSection)));
                if (int2ObjectMap.values().stream().allMatch(Section::isSkippable)) {
                    return typed2;
                }
                ArrayList<IntOpenHashSet> list = Lists.newArrayList();
                for (int i = 0; i < 7; ++i) {
                    list.add(new IntOpenHashSet());
                }
                for (LeavesSection leavesSection2 : int2ObjectMap.values()) {
                    if (leavesSection2.isSkippable()) continue;
                    for (int j = 0; j < 4096; ++j) {
                        int k = leavesSection2.getBlock(j);
                        if (leavesSection2.isLog(k)) {
                            ((IntSet)list.get(0)).add(leavesSection2.getIndex() << 12 | j);
                            continue;
                        }
                        if (!leavesSection2.isLeaf(k)) continue;
                        l = this.getX(j);
                        m = this.getZ(j);
                        is[0] = is[0] | LeavesFix.getSideMask(l == 0, l == 15, m == 0, m == 15);
                    }
                }
                for (int i = 1; i < 7; ++i) {
                    IntSet intSet = (IntSet)list.get(i - 1);
                    IntSet intSet2 = (IntSet)list.get(i);
                    IntIterator intIterator = intSet.iterator();
                    while (intIterator.hasNext()) {
                        l = intIterator.nextInt();
                        m = this.getX(l);
                        int n = this.getY(l);
                        int o = this.getZ(l);
                        for (int[] js : DIRECTIONS) {
                            int u;
                            int s;
                            int t;
                            LeavesSection leavesSection2;
                            int p = m + js[0];
                            int q = n + js[1];
                            int r = o + js[2];
                            if (p < 0 || p > 15 || r < 0 || r > 15 || q < 0 || q > 255 || (leavesSection2 = (LeavesSection)int2ObjectMap.get(q >> 4)) == null || leavesSection2.isSkippable() || !leavesSection2.isLeaf(t = leavesSection2.getBlock(s = LeavesFix.getIndex(p, q & 0xF, r))) || (u = leavesSection2.getDistance(t)) <= i) continue;
                            leavesSection2.setDistance(s, t, i);
                            intSet2.add(LeavesFix.getIndex(p, q, r));
                        }
                    }
                }
                return typed2.updateTyped(opticFinder3, typed -> ((LeavesSection)int2ObjectMap.get(typed.get(DSL.remainderFinder()).get("Y").asInt(0))).write((Typed<?>)typed));
            });
            if (is[0] != 0) {
                typed22 = typed22.update(DSL.remainderFinder(), dynamic -> {
                    Dynamic dynamic2 = DataFixUtils.orElse(dynamic.get("UpgradeData").result(), dynamic.emptyMap());
                    return dynamic.set("UpgradeData", dynamic2.set("Sides", dynamic.createByte((byte)(dynamic2.get("Sides").asByte((byte)0) | is[0]))));
                });
            }
            return typed22;
        }));
    }

    public static int getIndex(int i, int j, int k) {
        return j << 8 | k << 4 | i;
    }

    private int getX(int i) {
        return i & 0xF;
    }

    private int getY(int i) {
        return i >> 8 & 0xFF;
    }

    private int getZ(int i) {
        return i >> 4 & 0xF;
    }

    public static int getSideMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        int i = 0;
        if (bl3) {
            i = bl2 ? (i |= 2) : (bl ? (i |= 0x80) : (i |= 1));
        } else if (bl4) {
            i = bl ? (i |= 0x20) : (bl2 ? (i |= 8) : (i |= 0x10));
        } else if (bl2) {
            i |= 4;
        } else if (bl) {
            i |= 0x40;
        }
        return i;
    }

    public static final class LeavesSection
    extends Section {
        private static final String PERSISTENT = "persistent";
        private static final String DECAYABLE = "decayable";
        private static final String DISTANCE = "distance";
        @Nullable
        private IntSet leaveIds;
        @Nullable
        private IntSet logIds;
        @Nullable
        private Int2IntMap stateToIdMap;

        public LeavesSection(Typed<?> typed, Schema schema) {
            super(typed, schema);
        }

        @Override
        protected boolean skippable() {
            this.leaveIds = new IntOpenHashSet();
            this.logIds = new IntOpenHashSet();
            this.stateToIdMap = new Int2IntOpenHashMap();
            for (int i = 0; i < this.palette.size(); ++i) {
                Dynamic dynamic = (Dynamic)this.palette.get(i);
                String string = dynamic.get("Name").asString("");
                if (LEAVES.containsKey(string)) {
                    boolean bl = Objects.equals(dynamic.get("Properties").get(DECAYABLE).asString(""), "false");
                    this.leaveIds.add(i);
                    this.stateToIdMap.put(this.getStateId(string, bl, 7), i);
                    this.palette.set(i, this.makeLeafTag(dynamic, string, bl, 7));
                }
                if (!LOGS.contains(string)) continue;
                this.logIds.add(i);
            }
            return this.leaveIds.isEmpty() && this.logIds.isEmpty();
        }

        private Dynamic<?> makeLeafTag(Dynamic<?> dynamic, String string, boolean bl, int i) {
            Dynamic dynamic2 = dynamic.emptyMap();
            dynamic2 = dynamic2.set(PERSISTENT, dynamic2.createString(bl ? "true" : "false"));
            dynamic2 = dynamic2.set(DISTANCE, dynamic2.createString(Integer.toString(i)));
            Dynamic dynamic3 = dynamic.emptyMap();
            dynamic3 = dynamic3.set("Properties", dynamic2);
            dynamic3 = dynamic3.set("Name", dynamic3.createString(string));
            return dynamic3;
        }

        public boolean isLog(int i) {
            return this.logIds.contains(i);
        }

        public boolean isLeaf(int i) {
            return this.leaveIds.contains(i);
        }

        int getDistance(int i) {
            if (this.isLog(i)) {
                return 0;
            }
            return Integer.parseInt(((Dynamic)this.palette.get(i)).get("Properties").get(DISTANCE).asString(""));
        }

        void setDistance(int i, int j, int k) {
            int m;
            boolean bl;
            Dynamic dynamic = (Dynamic)this.palette.get(j);
            String string = dynamic.get("Name").asString("");
            int l = this.getStateId(string, bl = Objects.equals(dynamic.get("Properties").get(PERSISTENT).asString(""), "true"), k);
            if (!this.stateToIdMap.containsKey(l)) {
                m = this.palette.size();
                this.leaveIds.add(m);
                this.stateToIdMap.put(l, m);
                this.palette.add(this.makeLeafTag(dynamic, string, bl, k));
            }
            m = this.stateToIdMap.get(l);
            if (1 << this.storage.getBits() <= m) {
                PackedBitStorage packedBitStorage = new PackedBitStorage(this.storage.getBits() + 1, 4096);
                for (int n = 0; n < 4096; ++n) {
                    packedBitStorage.set(n, this.storage.get(n));
                }
                this.storage = packedBitStorage;
            }
            this.storage.set(i, m);
        }
    }

    public static abstract class Section {
        protected static final String BLOCK_STATES_TAG = "BlockStates";
        protected static final String NAME_TAG = "Name";
        protected static final String PROPERTIES_TAG = "Properties";
        private final Type<Pair<String, Dynamic<?>>> blockStateType = DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType());
        protected final OpticFinder<List<Pair<String, Dynamic<?>>>> paletteFinder = DSL.fieldFinder("Palette", DSL.list(this.blockStateType));
        protected final List<Dynamic<?>> palette;
        protected final int index;
        @Nullable
        protected PackedBitStorage storage;

        public Section(Typed<?> typed, Schema schema) {
            if (!Objects.equals(schema.getType(References.BLOCK_STATE), this.blockStateType)) {
                throw new IllegalStateException("Block state type is not what was expected.");
            }
            Optional<List<Pair<String, Dynamic<?>>>> optional = typed.getOptional(this.paletteFinder);
            this.palette = optional.map(list -> list.stream().map(Pair::getSecond).collect(Collectors.toList())).orElse(ImmutableList.of());
            Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
            this.index = dynamic.get("Y").asInt(0);
            this.readStorage(dynamic);
        }

        protected void readStorage(Dynamic<?> dynamic) {
            if (this.skippable()) {
                this.storage = null;
            } else {
                long[] ls = dynamic.get(BLOCK_STATES_TAG).asLongStream().toArray();
                int i = Math.max(4, DataFixUtils.ceillog2(this.palette.size()));
                this.storage = new PackedBitStorage(i, 4096, ls);
            }
        }

        public Typed<?> write(Typed<?> typed) {
            if (this.isSkippable()) {
                return typed;
            }
            return typed.update(DSL.remainderFinder(), dynamic -> dynamic.set(BLOCK_STATES_TAG, dynamic.createLongList(Arrays.stream(this.storage.getRaw())))).set(this.paletteFinder, this.palette.stream().map(dynamic -> Pair.of(References.BLOCK_STATE.typeName(), dynamic)).collect(Collectors.toList()));
        }

        public boolean isSkippable() {
            return this.storage == null;
        }

        public int getBlock(int i) {
            return this.storage.get(i);
        }

        protected int getStateId(String string, boolean bl, int i) {
            return LEAVES.get(string) << 5 | (bl ? 16 : 0) | i;
        }

        int getIndex() {
            return this.index;
        }

        protected abstract boolean skippable();
    }
}

