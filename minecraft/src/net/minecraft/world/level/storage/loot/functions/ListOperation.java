package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.slf4j.Logger;

public interface ListOperation {
	static MapCodec<ListOperation> codec(int i) {
		return ListOperation.Type.CODEC.<ListOperation>dispatchMap("mode", ListOperation::mode, type -> type.mapCodec).validate(listOperation -> {
			if (listOperation instanceof ListOperation.ReplaceSection replaceSection && replaceSection.size().isPresent()) {
				int j = (Integer)replaceSection.size().get();
				if (j > i) {
					return DataResult.error(() -> "Size value too large: " + j + ", max size is " + i);
				}
			}

			return DataResult.success(listOperation);
		});
	}

	ListOperation.Type mode();

	<T> List<T> apply(List<T> list, List<T> list2, int i);

	public static class Append implements ListOperation {
		private static final Logger LOGGER = LogUtils.getLogger();
		public static final ListOperation.Append INSTANCE = new ListOperation.Append();
		public static final MapCodec<ListOperation.Append> MAP_CODEC = MapCodec.unit((Supplier<ListOperation.Append>)(() -> INSTANCE));

		private Append() {
		}

		@Override
		public ListOperation.Type mode() {
			return ListOperation.Type.APPEND;
		}

		@Override
		public <T> List<T> apply(List<T> list, List<T> list2, int i) {
			if (list.size() + list2.size() > i) {
				LOGGER.error("Contents overflow in section append");
				return list;
			} else {
				return Stream.concat(list.stream(), list2.stream()).toList();
			}
		}
	}

	public static record Insert(int offset) implements ListOperation {
		private static final Logger LOGGER = LogUtils.getLogger();
		public static final MapCodec<ListOperation.Insert> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(ListOperation.Insert::offset))
					.apply(instance, ListOperation.Insert::new)
		);

		@Override
		public ListOperation.Type mode() {
			return ListOperation.Type.INSERT;
		}

		@Override
		public <T> List<T> apply(List<T> list, List<T> list2, int i) {
			int j = list.size();
			if (this.offset > j) {
				LOGGER.error("Cannot insert when offset is out of bounds");
				return list;
			} else if (j + list2.size() > i) {
				LOGGER.error("Contents overflow in section insertion");
				return list;
			} else {
				Builder<T> builder = ImmutableList.builder();
				builder.addAll(list.subList(0, this.offset));
				builder.addAll(list2);
				builder.addAll(list.subList(this.offset, j));
				return builder.build();
			}
		}
	}

	public static class ReplaceAll implements ListOperation {
		public static final ListOperation.ReplaceAll INSTANCE = new ListOperation.ReplaceAll();
		public static final MapCodec<ListOperation.ReplaceAll> MAP_CODEC = MapCodec.unit((Supplier<ListOperation.ReplaceAll>)(() -> INSTANCE));

		private ReplaceAll() {
		}

		@Override
		public ListOperation.Type mode() {
			return ListOperation.Type.REPLACE_ALL;
		}

		@Override
		public <T> List<T> apply(List<T> list, List<T> list2, int i) {
			return list2;
		}
	}

	public static record ReplaceSection(int offset, Optional<Integer> size) implements ListOperation {
		private static final Logger LOGGER = LogUtils.getLogger();
		public static final MapCodec<ListOperation.ReplaceSection> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(ListOperation.ReplaceSection::offset),
						ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("size").forGetter(ListOperation.ReplaceSection::size)
					)
					.apply(instance, ListOperation.ReplaceSection::new)
		);

		public ReplaceSection(int i) {
			this(i, Optional.empty());
		}

		@Override
		public ListOperation.Type mode() {
			return ListOperation.Type.REPLACE_SECTION;
		}

		@Override
		public <T> List<T> apply(List<T> list, List<T> list2, int i) {
			int j = list.size();
			if (this.offset > j) {
				LOGGER.error("Cannot replace when offset is out of bounds");
				return list;
			} else {
				Builder<T> builder = ImmutableList.builder();
				builder.addAll(list.subList(0, this.offset));
				builder.addAll(list2);
				int k = this.offset + (Integer)this.size.orElse(list2.size());
				if (k < j) {
					builder.addAll(list.subList(k, j));
				}

				List<T> list3 = builder.build();
				if (list3.size() > i) {
					LOGGER.error("Contents overflow in section replacement");
					return list;
				} else {
					return list3;
				}
			}
		}
	}

	public static enum Type implements StringRepresentable {
		REPLACE_ALL("replace_all", ListOperation.ReplaceAll.MAP_CODEC),
		REPLACE_SECTION("replace_section", ListOperation.ReplaceSection.MAP_CODEC),
		INSERT("insert", ListOperation.Insert.MAP_CODEC),
		APPEND("append", ListOperation.Append.MAP_CODEC);

		public static final Codec<ListOperation.Type> CODEC = StringRepresentable.fromEnum(ListOperation.Type::values);
		private final String id;
		final MapCodec<? extends ListOperation> mapCodec;

		private Type(String string2, MapCodec<? extends ListOperation> mapCodec) {
			this.id = string2;
			this.mapCodec = mapCodec;
		}

		public MapCodec<? extends ListOperation> mapCodec() {
			return this.mapCodec;
		}

		@Override
		public String getSerializedName() {
			return this.id;
		}
	}
}
