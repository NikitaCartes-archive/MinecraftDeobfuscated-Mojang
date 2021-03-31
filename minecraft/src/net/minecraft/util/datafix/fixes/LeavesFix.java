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
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.datafix.PackedBitStorage;

public class LeavesFix extends DataFix {
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
	private static final Object2IntMap<String> LEAVES = DataFixUtils.make(new Object2IntOpenHashMap<>(), object2IntOpenHashMap -> {
		object2IntOpenHashMap.put("minecraft:acacia_leaves", 0);
		object2IntOpenHashMap.put("minecraft:birch_leaves", 1);
		object2IntOpenHashMap.put("minecraft:dark_oak_leaves", 2);
		object2IntOpenHashMap.put("minecraft:jungle_leaves", 3);
		object2IntOpenHashMap.put("minecraft:oak_leaves", 4);
		object2IntOpenHashMap.put("minecraft:spruce_leaves", 5);
	});
	private static final Set<String> LOGS = ImmutableSet.of(
		"minecraft:acacia_bark",
		"minecraft:birch_bark",
		"minecraft:dark_oak_bark",
		"minecraft:jungle_bark",
		"minecraft:oak_bark",
		"minecraft:spruce_bark",
		"minecraft:acacia_log",
		"minecraft:birch_log",
		"minecraft:dark_oak_log",
		"minecraft:jungle_log",
		"minecraft:oak_log",
		"minecraft:spruce_log",
		"minecraft:stripped_acacia_log",
		"minecraft:stripped_birch_log",
		"minecraft:stripped_dark_oak_log",
		"minecraft:stripped_jungle_log",
		"minecraft:stripped_oak_log",
		"minecraft:stripped_spruce_log"
	);

	public LeavesFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.CHUNK);
		OpticFinder<?> opticFinder = type.findField("Level");
		OpticFinder<?> opticFinder2 = opticFinder.type().findField("Sections");
		Type<?> type2 = opticFinder2.type();
		if (!(type2 instanceof ListType)) {
			throw new IllegalStateException("Expecting sections to be a list.");
		} else {
			Type<?> type3 = ((ListType)type2).getElement();
			OpticFinder<?> opticFinder3 = DSL.typeFinder(type3);
			return this.fixTypeEverywhereTyped(
				"Leaves fix",
				type,
				typed -> typed.updateTyped(
						opticFinder,
						typedx -> {
							int[] is = new int[]{0};
							Typed<?> typed2 = typedx.updateTyped(
								opticFinder2,
								typedxx -> {
									Int2ObjectMap<LeavesFix.LeavesSection> int2ObjectMap = new Int2ObjectOpenHashMap<>(
										(Map<? extends Integer, ? extends LeavesFix.LeavesSection>)typedxx.getAllTyped(opticFinder3)
											.stream()
											.map(typedxxx -> new LeavesFix.LeavesSection(typedxxx, this.getInputSchema()))
											.collect(Collectors.toMap(LeavesFix.Section::getIndex, leavesSection -> leavesSection))
									);
									if (int2ObjectMap.values().stream().allMatch(LeavesFix.Section::isSkippable)) {
										return typedxx;
									} else {
										List<IntSet> list = Lists.<IntSet>newArrayList();

										for (int i = 0; i < 7; i++) {
											list.add(new IntOpenHashSet());
										}

										for (LeavesFix.LeavesSection leavesSection : int2ObjectMap.values()) {
											if (!leavesSection.isSkippable()) {
												for (int j = 0; j < 4096; j++) {
													int k = leavesSection.getBlock(j);
													if (leavesSection.isLog(k)) {
														((IntSet)list.get(0)).add(leavesSection.getIndex() << 12 | j);
													} else if (leavesSection.isLeaf(k)) {
														int l = this.getX(j);
														int m = this.getZ(j);
														is[0] |= getSideMask(l == 0, l == 15, m == 0, m == 15);
													}
												}
											}
										}

										for (int i = 1; i < 7; i++) {
											IntSet intSet = (IntSet)list.get(i - 1);
											IntSet intSet2 = (IntSet)list.get(i);
											IntIterator intIterator = intSet.iterator();

											while (intIterator.hasNext()) {
												int l = intIterator.nextInt();
												int m = this.getX(l);
												int n = this.getY(l);
												int o = this.getZ(l);

												for (int[] js : DIRECTIONS) {
													int p = m + js[0];
													int q = n + js[1];
													int r = o + js[2];
													if (p >= 0 && p <= 15 && r >= 0 && r <= 15 && q >= 0 && q <= 255) {
														LeavesFix.LeavesSection leavesSection2 = int2ObjectMap.get(q >> 4);
														if (leavesSection2 != null && !leavesSection2.isSkippable()) {
															int s = getIndex(p, q & 15, r);
															int t = leavesSection2.getBlock(s);
															if (leavesSection2.isLeaf(t)) {
																int u = leavesSection2.getDistance(t);
																if (u > i) {
																	leavesSection2.setDistance(s, t, i);
																	intSet2.add(getIndex(p, q, r));
																}
															}
														}
													}
												}
											}
										}

										return typedxx.updateTyped(opticFinder3, typedxxx -> int2ObjectMap.get(typedxxx.get(DSL.remainderFinder()).get("Y").asInt(0)).write(typedxxx));
									}
								}
							);
							if (is[0] != 0) {
								typed2 = typed2.update(DSL.remainderFinder(), dynamic -> {
									Dynamic<?> dynamic2 = DataFixUtils.orElse(dynamic.get("UpgradeData").result(), dynamic.emptyMap());
									return dynamic.set("UpgradeData", dynamic2.set("Sides", dynamic.createByte((byte)(dynamic2.get("Sides").asByte((byte)0) | is[0]))));
								});
							}

							return typed2;
						}
					)
			);
		}
	}

	public static int getIndex(int i, int j, int k) {
		return j << 8 | k << 4 | i;
	}

	private int getX(int i) {
		return i & 15;
	}

	private int getY(int i) {
		return i >> 8 & 0xFF;
	}

	private int getZ(int i) {
		return i >> 4 & 15;
	}

	public static int getSideMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		int i = 0;
		if (bl3) {
			if (bl2) {
				i |= 2;
			} else if (bl) {
				i |= 128;
			} else {
				i |= 1;
			}
		} else if (bl4) {
			if (bl) {
				i |= 32;
			} else if (bl2) {
				i |= 8;
			} else {
				i |= 16;
			}
		} else if (bl2) {
			i |= 4;
		} else if (bl) {
			i |= 64;
		}

		return i;
	}

	public static final class LeavesSection extends LeavesFix.Section {
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

			for (int i = 0; i < this.palette.size(); i++) {
				Dynamic<?> dynamic = (Dynamic<?>)this.palette.get(i);
				String string = dynamic.get("Name").asString("");
				if (LeavesFix.LEAVES.containsKey(string)) {
					boolean bl = Objects.equals(dynamic.get("Properties").get("decayable").asString(""), "false");
					this.leaveIds.add(i);
					this.stateToIdMap.put(this.getStateId(string, bl, 7), i);
					this.palette.set(i, this.makeLeafTag(dynamic, string, bl, 7));
				}

				if (LeavesFix.LOGS.contains(string)) {
					this.logIds.add(i);
				}
			}

			return this.leaveIds.isEmpty() && this.logIds.isEmpty();
		}

		private Dynamic<?> makeLeafTag(Dynamic<?> dynamic, String string, boolean bl, int i) {
			Dynamic<?> dynamic2 = dynamic.emptyMap();
			dynamic2 = dynamic2.set("persistent", dynamic2.createString(bl ? "true" : "false"));
			dynamic2 = dynamic2.set("distance", dynamic2.createString(Integer.toString(i)));
			Dynamic<?> dynamic3 = dynamic.emptyMap();
			dynamic3 = dynamic3.set("Properties", dynamic2);
			return dynamic3.set("Name", dynamic3.createString(string));
		}

		public boolean isLog(int i) {
			return this.logIds.contains(i);
		}

		public boolean isLeaf(int i) {
			return this.leaveIds.contains(i);
		}

		private int getDistance(int i) {
			return this.isLog(i) ? 0 : Integer.parseInt(((Dynamic)this.palette.get(i)).get("Properties").get("distance").asString(""));
		}

		private void setDistance(int i, int j, int k) {
			Dynamic<?> dynamic = (Dynamic<?>)this.palette.get(j);
			String string = dynamic.get("Name").asString("");
			boolean bl = Objects.equals(dynamic.get("Properties").get("persistent").asString(""), "true");
			int l = this.getStateId(string, bl, k);
			if (!this.stateToIdMap.containsKey(l)) {
				int m = this.palette.size();
				this.leaveIds.add(m);
				this.stateToIdMap.put(l, m);
				this.palette.add(this.makeLeafTag(dynamic, string, bl, k));
			}

			int m = this.stateToIdMap.get(l);
			if (1 << this.storage.getBits() <= m) {
				PackedBitStorage packedBitStorage = new PackedBitStorage(this.storage.getBits() + 1, 4096);

				for (int n = 0; n < 4096; n++) {
					packedBitStorage.set(n, this.storage.get(n));
				}

				this.storage = packedBitStorage;
			}

			this.storage.set(i, m);
		}
	}

	public abstract static class Section {
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
			} else {
				Optional<List<Pair<String, Dynamic<?>>>> optional = typed.getOptional(this.paletteFinder);
				this.palette = (List<Dynamic<?>>)optional.map(list -> (List)list.stream().map(Pair::getSecond).collect(Collectors.toList())).orElse(ImmutableList.of());
				Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
				this.index = dynamic.get("Y").asInt(0);
				this.readStorage(dynamic);
			}
		}

		protected void readStorage(Dynamic<?> dynamic) {
			if (this.skippable()) {
				this.storage = null;
			} else {
				long[] ls = dynamic.get("BlockStates").asLongStream().toArray();
				int i = Math.max(4, DataFixUtils.ceillog2(this.palette.size()));
				this.storage = new PackedBitStorage(i, 4096, ls);
			}
		}

		public Typed<?> write(Typed<?> typed) {
			return this.isSkippable()
				? typed
				: typed.update(DSL.remainderFinder(), dynamic -> dynamic.set("BlockStates", dynamic.createLongList(Arrays.stream(this.storage.getRaw()))))
					.set(
						this.paletteFinder,
						(List<Pair<String, Dynamic<?>>>)this.palette.stream().map(dynamic -> Pair.of(References.BLOCK_STATE.typeName(), dynamic)).collect(Collectors.toList())
					);
		}

		public boolean isSkippable() {
			return this.storage == null;
		}

		public int getBlock(int i) {
			return this.storage.get(i);
		}

		protected int getStateId(String string, boolean bl, int i) {
			return LeavesFix.LEAVES.get(string) << 5 | (bl ? 16 : 0) | i;
		}

		int getIndex() {
			return this.index;
		}

		protected abstract boolean skippable();
	}
}
