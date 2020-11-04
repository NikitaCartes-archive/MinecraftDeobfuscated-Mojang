package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
import net.minecraft.core.SerializableUUID;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NbtUtils {
	private static final Comparator<ListTag> YXZ_LISTTAG_INT_COMPARATOR = Comparator.comparingInt(listTag -> listTag.getInt(1))
		.thenComparingInt(listTag -> listTag.getInt(0))
		.thenComparingInt(listTag -> listTag.getInt(2));
	private static final Comparator<ListTag> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.comparingDouble(listTag -> listTag.getDouble(1))
		.thenComparingDouble(listTag -> listTag.getDouble(0))
		.thenComparingDouble(listTag -> listTag.getDouble(2));
	private static final Splitter COMMA_SPLITTER = Splitter.on(",");
	private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
	private static final Logger LOGGER = LogManager.getLogger();

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
		} else if (tag instanceof CompoundTag) {
			CompoundTag compoundTag = (CompoundTag)tag;
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
		return new IntArrayTag(SerializableUUID.uuidToIntArray(uUID));
	}

	public static UUID loadUUID(Tag tag) {
		if (tag.getType() != IntArrayTag.TYPE) {
			throw new IllegalArgumentException("Expected UUID-Tag to be of type " + IntArrayTag.TYPE.getName() + ", but found " + tag.getType().getName() + ".");
		} else {
			int[] is = ((IntArrayTag)tag).getAsIntArray();
			if (is.length != 4) {
				throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + is.length + ".");
			} else {
				return SerializableUUID.uuidFromIntArray(is);
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

	private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
		return property.getName((T)comparable);
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
				.map(stringx -> stringx + ':' + compoundTag2.get(stringx).getAsString())
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
