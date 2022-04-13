package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

public final class NbtUtils {
	private static final Comparator<ListTag> YXZ_LISTTAG_INT_COMPARATOR = Comparator.comparingInt(listTag -> listTag.getInt(1))
		.thenComparingInt(listTag -> listTag.getInt(0))
		.thenComparingInt(listTag -> listTag.getInt(2));
	private static final Comparator<ListTag> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.comparingDouble(listTag -> listTag.getDouble(1))
		.thenComparingDouble(listTag -> listTag.getDouble(0))
		.thenComparingDouble(listTag -> listTag.getDouble(2));
	public static final String SNBT_DATA_TAG = "data";
	private static final char PROPERTIES_START = '{';
	private static final char PROPERTIES_END = '}';
	private static final String ELEMENT_SEPARATOR = ",";
	private static final char KEY_VALUE_SEPARATOR = ':';
	private static final Splitter COMMA_SPLITTER = Splitter.on(",");
	private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
	private static final Logger LOGGER = LogUtils.getLogger();
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

					for (int i = 0; i < listTag.size(); i++) {
						CompoundTag compoundTag3 = listTag.getCompound(i);
						String string3 = compoundTag3.getString("Value");
						if (compoundTag3.contains("Signature", 8)) {
							gameProfile.getProperties().put(string2, new com.mojang.authlib.properties.Property(string2, string3, compoundTag3.getString("Signature")));
						} else {
							gameProfile.getProperties().put(string2, new com.mojang.authlib.properties.Property(string2, string3));
						}
					}
				}
			}

			return gameProfile;
		} catch (Throwable var11) {
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

				for (com.mojang.authlib.properties.Property property : gameProfile.getProperties().get(string)) {
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
		} else if (tag == null) {
			return true;
		} else if (tag2 == null) {
			return false;
		} else if (!tag.getClass().equals(tag2.getClass())) {
			return false;
		} else if (tag instanceof CompoundTag compoundTag) {
			CompoundTag compoundTag2 = (CompoundTag)tag2;

			for (String string : compoundTag.getAllKeys()) {
				Tag tag3 = compoundTag.get(string);
				if (!compareNbt(tag3, compoundTag2.get(string), bl)) {
					return false;
				}
			}

			return true;
		} else if (tag instanceof ListTag && bl) {
			ListTag listTag = (ListTag)tag;
			ListTag listTag2 = (ListTag)tag2;
			if (listTag.isEmpty()) {
				return listTag2.isEmpty();
			} else {
				for (int i = 0; i < listTag.size(); i++) {
					Tag tag4 = listTag.get(i);
					boolean bl2 = false;

					for (int j = 0; j < listTag2.size(); j++) {
						if (compareNbt(tag4, listTag2.get(j), bl)) {
							bl2 = true;
							break;
						}
					}

					if (!bl2) {
						return false;
					}
				}

				return true;
			}
		} else {
			return tag.equals(tag2);
		}
	}

	public static IntArrayTag createUUID(UUID uUID) {
		return new IntArrayTag(UUIDUtil.uuidToIntArray(uUID));
	}

	public static UUID loadUUID(Tag tag) {
		if (tag.getType() != IntArrayTag.TYPE) {
			throw new IllegalArgumentException("Expected UUID-Tag to be of type " + IntArrayTag.TYPE.getName() + ", but found " + tag.getType().getName() + ".");
		} else {
			int[] is = ((IntArrayTag)tag).getAsIntArray();
			if (is.length != 4) {
				throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + is.length + ".");
			} else {
				return UUIDUtil.uuidFromIntArray(is);
			}
		}
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
		} else {
			Block block = Registry.BLOCK.get(new ResourceLocation(compoundTag.getString("Name")));
			BlockState blockState = block.defaultBlockState();
			if (compoundTag.contains("Properties", 10)) {
				CompoundTag compoundTag2 = compoundTag.getCompound("Properties");
				StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();

				for (String string : compoundTag2.getAllKeys()) {
					Property<?> property = stateDefinition.getProperty(string);
					if (property != null) {
						blockState = setValueHelper(blockState, property, string, compoundTag2, compoundTag);
					}
				}
			}

			return blockState;
		}
	}

	private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(
		S stateHolder, Property<T> property, String string, CompoundTag compoundTag, CompoundTag compoundTag2
	) {
		Optional<T> optional = property.getValue(compoundTag.getString(string));
		if (optional.isPresent()) {
			return stateHolder.setValue(property, (Comparable)optional.get());
		} else {
			LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", string, compoundTag.getString(string), compoundTag2.toString());
			return stateHolder;
		}
	}

	public static CompoundTag writeBlockState(BlockState blockState) {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("Name", Registry.BLOCK.getKey(blockState.getBlock()).toString());
		ImmutableMap<Property<?>, Comparable<?>> immutableMap = blockState.getValues();
		if (!immutableMap.isEmpty()) {
			CompoundTag compoundTag2 = new CompoundTag();

			for (Entry<Property<?>, Comparable<?>> entry : immutableMap.entrySet()) {
				Property<?> property = (Property<?>)entry.getKey();
				compoundTag2.putString(property.getName(), getName(property, (Comparable<?>)entry.getValue()));
			}

			compoundTag.put("Properties", compoundTag2);
		}

		return compoundTag;
	}

	public static CompoundTag writeFluidState(FluidState fluidState) {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("Name", Registry.FLUID.getKey(fluidState.getType()).toString());
		ImmutableMap<Property<?>, Comparable<?>> immutableMap = fluidState.getValues();
		if (!immutableMap.isEmpty()) {
			CompoundTag compoundTag2 = new CompoundTag();

			for (Entry<Property<?>, Comparable<?>> entry : immutableMap.entrySet()) {
				Property<?> property = (Property<?>)entry.getKey();
				compoundTag2.putString(property.getName(), getName(property, (Comparable<?>)entry.getValue()));
			}

			compoundTag.put("Properties", compoundTag2);
		}

		return compoundTag;
	}

	private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
		return property.getName((T)comparable);
	}

	public static String prettyPrint(Tag tag) {
		return prettyPrint(tag, false);
	}

	public static String prettyPrint(Tag tag, boolean bl) {
		return prettyPrint(new StringBuilder(), tag, 0, bl).toString();
	}

	public static StringBuilder prettyPrint(StringBuilder stringBuilder, Tag tag, int i, boolean bl) {
		switch (tag.getId()) {
			case 0:
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 8:
				stringBuilder.append(tag);
				break;
			case 7:
				ByteArrayTag byteArrayTag = (ByteArrayTag)tag;
				byte[] bs = byteArrayTag.getAsByteArray();
				int jx = bs.length;
				indent(i, stringBuilder).append("byte[").append(jx).append("] {\n");
				if (bl) {
					indent(i + 1, stringBuilder);

					for (int k = 0; k < bs.length; k++) {
						if (k != 0) {
							stringBuilder.append(',');
						}

						if (k % 16 == 0 && k / 16 > 0) {
							stringBuilder.append('\n');
							if (k < bs.length) {
								indent(i + 1, stringBuilder);
							}
						} else if (k != 0) {
							stringBuilder.append(' ');
						}

						stringBuilder.append(String.format("0x%02X", bs[k] & 255));
					}
				} else {
					indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
				}

				stringBuilder.append('\n');
				indent(i, stringBuilder).append('}');
				break;
			case 9:
				ListTag listTag = (ListTag)tag;
				int l = listTag.size();
				int j = listTag.getElementType();
				String string = j == 0 ? "undefined" : TagTypes.getType(j).getPrettyName();
				indent(i, stringBuilder).append("list<").append(string).append(">[").append(l).append("] [");
				if (l != 0) {
					stringBuilder.append('\n');
				}

				for (int m = 0; m < l; m++) {
					if (m != 0) {
						stringBuilder.append(",\n");
					}

					indent(i + 1, stringBuilder);
					prettyPrint(stringBuilder, listTag.get(m), i + 1, bl);
				}

				if (l != 0) {
					stringBuilder.append('\n');
				}

				indent(i, stringBuilder).append(']');
				break;
			case 10:
				CompoundTag compoundTag = (CompoundTag)tag;
				List<String> list = Lists.<String>newArrayList(compoundTag.getAllKeys());
				Collections.sort(list);
				indent(i, stringBuilder).append('{');
				if (stringBuilder.length() - stringBuilder.lastIndexOf("\n") > 2 * (i + 1)) {
					stringBuilder.append('\n');
					indent(i + 1, stringBuilder);
				}

				int jx = list.stream().mapToInt(String::length).max().orElse(0);
				String stringx = Strings.repeat(" ", jx);

				for (int m = 0; m < list.size(); m++) {
					if (m != 0) {
						stringBuilder.append(",\n");
					}

					String string2 = (String)list.get(m);
					indent(i + 1, stringBuilder).append('"').append(string2).append('"').append(stringx, 0, stringx.length() - string2.length()).append(": ");
					prettyPrint(stringBuilder, compoundTag.get(string2), i + 1, bl);
				}

				if (!list.isEmpty()) {
					stringBuilder.append('\n');
				}

				indent(i, stringBuilder).append('}');
				break;
			case 11:
				IntArrayTag intArrayTag = (IntArrayTag)tag;
				int[] is = intArrayTag.getAsIntArray();
				int jx = 0;

				for (int n : is) {
					jx = Math.max(jx, String.format("%X", n).length());
				}

				int k = is.length;
				indent(i, stringBuilder).append("int[").append(k).append("] {\n");
				if (bl) {
					indent(i + 1, stringBuilder);

					for (int m = 0; m < is.length; m++) {
						if (m != 0) {
							stringBuilder.append(',');
						}

						if (m % 16 == 0 && m / 16 > 0) {
							stringBuilder.append('\n');
							if (m < is.length) {
								indent(i + 1, stringBuilder);
							}
						} else if (m != 0) {
							stringBuilder.append(' ');
						}

						stringBuilder.append(String.format("0x%0" + jx + "X", is[m]));
					}
				} else {
					indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
				}

				stringBuilder.append('\n');
				indent(i, stringBuilder).append('}');
				break;
			case 12:
				LongArrayTag longArrayTag = (LongArrayTag)tag;
				long[] ls = longArrayTag.getAsLongArray();
				long o = 0L;

				for (long p : ls) {
					o = Math.max(o, (long)String.format("%X", p).length());
				}

				long q = (long)ls.length;
				indent(i, stringBuilder).append("long[").append(q).append("] {\n");
				if (bl) {
					indent(i + 1, stringBuilder);

					for (int n = 0; n < ls.length; n++) {
						if (n != 0) {
							stringBuilder.append(',');
						}

						if (n % 16 == 0 && n / 16 > 0) {
							stringBuilder.append('\n');
							if (n < ls.length) {
								indent(i + 1, stringBuilder);
							}
						} else if (n != 0) {
							stringBuilder.append(' ');
						}

						stringBuilder.append(String.format("0x%0" + o + "X", ls[n]));
					}
				} else {
					indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
				}

				stringBuilder.append('\n');
				indent(i, stringBuilder).append('}');
				break;
			default:
				stringBuilder.append("<UNKNOWN :(>");
		}

		return stringBuilder;
	}

	private static StringBuilder indent(int i, StringBuilder stringBuilder) {
		int j = stringBuilder.lastIndexOf("\n") + 1;
		int k = stringBuilder.length() - j;

		for (int l = 0; l < 2 * i - k; l++) {
			stringBuilder.append(' ');
		}

		return stringBuilder;
	}

	public static CompoundTag update(DataFixer dataFixer, DataFixTypes dataFixTypes, CompoundTag compoundTag, int i) {
		return update(dataFixer, dataFixTypes, compoundTag, i, SharedConstants.getCurrentVersion().getWorldVersion());
	}

	public static CompoundTag update(DataFixer dataFixer, DataFixTypes dataFixTypes, CompoundTag compoundTag, int i, int j) {
		return (CompoundTag)dataFixer.update(dataFixTypes.getType(), new Dynamic<>(NbtOps.INSTANCE, compoundTag), i, j).getValue();
	}

	public static Component toPrettyComponent(Tag tag) {
		return new TextComponentTagVisitor("", 0).visit(tag);
	}

	public static String structureToSnbt(CompoundTag compoundTag) {
		return new SnbtPrinterTagVisitor().visit(packStructureTemplate(compoundTag));
	}

	public static CompoundTag snbtToStructure(String string) throws CommandSyntaxException {
		return unpackStructureTemplate(TagParser.parseTag(string));
	}

	@VisibleForTesting
	static CompoundTag packStructureTemplate(CompoundTag compoundTag) {
		boolean bl = compoundTag.contains("palettes", 9);
		ListTag listTag;
		if (bl) {
			listTag = compoundTag.getList("palettes", 9).getList(0);
		} else {
			listTag = compoundTag.getList("palette", 10);
		}

		ListTag listTag2 = (ListTag)listTag.stream()
			.map(CompoundTag.class::cast)
			.map(NbtUtils::packBlockState)
			.map(StringTag::valueOf)
			.collect(Collectors.toCollection(ListTag::new));
		compoundTag.put("palette", listTag2);
		if (bl) {
			ListTag listTag3 = new ListTag();
			ListTag listTag4 = compoundTag.getList("palettes", 9);
			listTag4.stream().map(ListTag.class::cast).forEach(listTag3x -> {
				CompoundTag compoundTagx = new CompoundTag();

				for (int i = 0; i < listTag3x.size(); i++) {
					compoundTagx.putString(listTag2.getString(i), packBlockState(listTag3x.getCompound(i)));
				}

				listTag3.add(compoundTagx);
			});
			compoundTag.put("palettes", listTag3);
		}

		if (compoundTag.contains("entities", 10)) {
			ListTag listTag3 = compoundTag.getList("entities", 10);
			ListTag listTag4 = (ListTag)listTag3.stream()
				.map(CompoundTag.class::cast)
				.sorted(Comparator.comparing(compoundTagx -> compoundTagx.getList("pos", 6), YXZ_LISTTAG_DOUBLE_COMPARATOR))
				.collect(Collectors.toCollection(ListTag::new));
			compoundTag.put("entities", listTag4);
		}

		ListTag listTag3 = (ListTag)compoundTag.getList("blocks", 10)
			.stream()
			.map(CompoundTag.class::cast)
			.sorted(Comparator.comparing(compoundTagx -> compoundTagx.getList("pos", 3), YXZ_LISTTAG_INT_COMPARATOR))
			.peek(compoundTagx -> compoundTagx.putString("state", listTag2.getString(compoundTagx.getInt("state"))))
			.collect(Collectors.toCollection(ListTag::new));
		compoundTag.put("data", listTag3);
		compoundTag.remove("blocks");
		return compoundTag;
	}

	@VisibleForTesting
	static CompoundTag unpackStructureTemplate(CompoundTag compoundTag) {
		ListTag listTag = compoundTag.getList("palette", 8);
		Map<String, Tag> map = (Map<String, Tag>)listTag.stream()
			.map(StringTag.class::cast)
			.map(StringTag::getAsString)
			.collect(ImmutableMap.toImmutableMap(Function.identity(), NbtUtils::unpackBlockState));
		if (compoundTag.contains("palettes", 9)) {
			compoundTag.put(
				"palettes",
				(Tag)compoundTag.getList("palettes", 10)
					.stream()
					.map(CompoundTag.class::cast)
					.map(
						compoundTagx -> (ListTag)map.keySet()
								.stream()
								.map(compoundTagx::getString)
								.map(NbtUtils::unpackBlockState)
								.collect(Collectors.toCollection(ListTag::new))
					)
					.collect(Collectors.toCollection(ListTag::new))
			);
			compoundTag.remove("palette");
		} else {
			compoundTag.put("palette", (Tag)map.values().stream().collect(Collectors.toCollection(ListTag::new)));
		}

		if (compoundTag.contains("data", 9)) {
			Object2IntMap<String> object2IntMap = new Object2IntOpenHashMap<>();
			object2IntMap.defaultReturnValue(-1);

			for (int i = 0; i < listTag.size(); i++) {
				object2IntMap.put(listTag.getString(i), i);
			}

			ListTag listTag2 = compoundTag.getList("data", 10);

			for (int j = 0; j < listTag2.size(); j++) {
				CompoundTag compoundTag2 = listTag2.getCompound(j);
				String string = compoundTag2.getString("state");
				int k = object2IntMap.getInt(string);
				if (k == -1) {
					throw new IllegalStateException("Entry " + string + " missing from palette");
				}

				compoundTag2.putInt("state", k);
			}

			compoundTag.put("blocks", listTag2);
			compoundTag.remove("data");
		}

		return compoundTag;
	}

	@VisibleForTesting
	static String packBlockState(CompoundTag compoundTag) {
		StringBuilder stringBuilder = new StringBuilder(compoundTag.getString("Name"));
		if (compoundTag.contains("Properties", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("Properties");
			String string = (String)compoundTag2.getAllKeys()
				.stream()
				.sorted()
				.map(stringx -> stringx + ":" + compoundTag2.get(stringx).getAsString())
				.collect(Collectors.joining(","));
			stringBuilder.append('{').append(string).append('}');
		}

		return stringBuilder.toString();
	}

	@VisibleForTesting
	static CompoundTag unpackBlockState(String string) {
		CompoundTag compoundTag = new CompoundTag();
		int i = string.indexOf(123);
		String string2;
		if (i >= 0) {
			string2 = string.substring(0, i);
			CompoundTag compoundTag2 = new CompoundTag();
			if (i + 2 <= string.length()) {
				String string3 = string.substring(i + 1, string.indexOf(125, i));
				COMMA_SPLITTER.split(string3).forEach(string2x -> {
					List<String> list = COLON_SPLITTER.splitToList(string2x);
					if (list.size() == 2) {
						compoundTag2.putString((String)list.get(0), (String)list.get(1));
					} else {
						LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", string);
					}
				});
				compoundTag.put("Properties", compoundTag2);
			}
		} else {
			string2 = string;
		}

		compoundTag.putString("Name", string2);
		return compoundTag;
	}
}
