package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NbtPathArgument implements ArgumentType<NbtPathArgument.NbtPath> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar", "foo[0]", "[0]", "[]", "{foo=bar}");
	public static final SimpleCommandExceptionType ERROR_INVALID_NODE = new SimpleCommandExceptionType(new TranslatableComponent("arguments.nbtpath.node.invalid"));
	public static final DynamicCommandExceptionType ERROR_NOTHING_FOUND = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("arguments.nbtpath.nothing_found", object)
	);
	private static final char INDEX_MATCH_START = '[';
	private static final char INDEX_MATCH_END = ']';
	private static final char KEY_MATCH_START = '{';
	private static final char KEY_MATCH_END = '}';
	private static final char QUOTED_KEY_START = '"';

	public static NbtPathArgument nbtPath() {
		return new NbtPathArgument();
	}

	public static NbtPathArgument.NbtPath getPath(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, NbtPathArgument.NbtPath.class);
	}

	public NbtPathArgument.NbtPath parse(StringReader stringReader) throws CommandSyntaxException {
		List<NbtPathArgument.Node> list = Lists.<NbtPathArgument.Node>newArrayList();
		int i = stringReader.getCursor();
		Object2IntMap<NbtPathArgument.Node> object2IntMap = new Object2IntOpenHashMap<>();
		boolean bl = true;

		while (stringReader.canRead() && stringReader.peek() != ' ') {
			NbtPathArgument.Node node = parseNode(stringReader, bl);
			list.add(node);
			object2IntMap.put(node, stringReader.getCursor() - i);
			bl = false;
			if (stringReader.canRead()) {
				char c = stringReader.peek();
				if (c != ' ' && c != '[' && c != '{') {
					stringReader.expect('.');
				}
			}
		}

		return new NbtPathArgument.NbtPath(
			stringReader.getString().substring(i, stringReader.getCursor()), (NbtPathArgument.Node[])list.toArray(new NbtPathArgument.Node[0]), object2IntMap
		);
	}

	private static NbtPathArgument.Node parseNode(StringReader stringReader, boolean bl) throws CommandSyntaxException {
		switch (stringReader.peek()) {
			case '"': {
				String string = stringReader.readString();
				return readObjectNode(stringReader, string);
			}
			case '[':
				stringReader.skip();
				int i = stringReader.peek();
				if (i == 123) {
					CompoundTag compoundTag2 = new TagParser(stringReader).readStruct();
					stringReader.expect(']');
					return new NbtPathArgument.MatchElementNode(compoundTag2);
				} else {
					if (i == 93) {
						stringReader.skip();
						return NbtPathArgument.AllElementsNode.INSTANCE;
					}

					int j = stringReader.readInt();
					stringReader.expect(']');
					return new NbtPathArgument.IndexedElementNode(j);
				}
			case '{':
				if (!bl) {
					throw ERROR_INVALID_NODE.createWithContext(stringReader);
				}

				CompoundTag compoundTag = new TagParser(stringReader).readStruct();
				return new NbtPathArgument.MatchRootObjectNode(compoundTag);
			default: {
				String string = readUnquotedName(stringReader);
				return readObjectNode(stringReader, string);
			}
		}
	}

	private static NbtPathArgument.Node readObjectNode(StringReader stringReader, String string) throws CommandSyntaxException {
		if (stringReader.canRead() && stringReader.peek() == '{') {
			CompoundTag compoundTag = new TagParser(stringReader).readStruct();
			return new NbtPathArgument.MatchObjectNode(string, compoundTag);
		} else {
			return new NbtPathArgument.CompoundChildNode(string);
		}
	}

	private static String readUnquotedName(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		while (stringReader.canRead() && isAllowedInUnquotedName(stringReader.peek())) {
			stringReader.skip();
		}

		if (stringReader.getCursor() == i) {
			throw ERROR_INVALID_NODE.createWithContext(stringReader);
		} else {
			return stringReader.getString().substring(i, stringReader.getCursor());
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	private static boolean isAllowedInUnquotedName(char c) {
		return c != ' ' && c != '"' && c != '[' && c != ']' && c != '.' && c != '{' && c != '}';
	}

	static Predicate<Tag> createTagPredicate(CompoundTag compoundTag) {
		return tag -> NbtUtils.compareNbt(compoundTag, tag, true);
	}

	static class AllElementsNode implements NbtPathArgument.Node {
		public static final NbtPathArgument.AllElementsNode INSTANCE = new NbtPathArgument.AllElementsNode();

		private AllElementsNode() {
		}

		@Override
		public void getTag(Tag tag, List<Tag> list) {
			if (tag instanceof CollectionTag) {
				list.addAll((CollectionTag)tag);
			}
		}

		@Override
		public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
			if (tag instanceof CollectionTag<?> collectionTag) {
				if (collectionTag.isEmpty()) {
					Tag tag2 = (Tag)supplier.get();
					if (collectionTag.addTag(0, tag2)) {
						list.add(tag2);
					}
				} else {
					list.addAll(collectionTag);
				}
			}
		}

		@Override
		public Tag createPreferredParentTag() {
			return new ListTag();
		}

		@Override
		public int setTag(Tag tag, Supplier<Tag> supplier) {
			if (!(tag instanceof CollectionTag<?> collectionTag)) {
				return 0;
			} else {
				int i = collectionTag.size();
				if (i == 0) {
					collectionTag.addTag(0, (Tag)supplier.get());
					return 1;
				} else {
					Tag tag2 = (Tag)supplier.get();
					int j = i - (int)collectionTag.stream().filter(tag2::equals).count();
					if (j == 0) {
						return 0;
					} else {
						collectionTag.clear();
						if (!collectionTag.addTag(0, tag2)) {
							return 0;
						} else {
							for (int k = 1; k < i; k++) {
								collectionTag.addTag(k, (Tag)supplier.get());
							}

							return j;
						}
					}
				}
			}
		}

		@Override
		public int removeTag(Tag tag) {
			if (tag instanceof CollectionTag<?> collectionTag) {
				int i = collectionTag.size();
				if (i > 0) {
					collectionTag.clear();
					return i;
				}
			}

			return 0;
		}
	}

	static class CompoundChildNode implements NbtPathArgument.Node {
		private final String name;

		public CompoundChildNode(String string) {
			this.name = string;
		}

		@Override
		public void getTag(Tag tag, List<Tag> list) {
			if (tag instanceof CompoundTag) {
				Tag tag2 = ((CompoundTag)tag).get(this.name);
				if (tag2 != null) {
					list.add(tag2);
				}
			}
		}

		@Override
		public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
			if (tag instanceof CompoundTag compoundTag) {
				Tag tag2;
				if (compoundTag.contains(this.name)) {
					tag2 = compoundTag.get(this.name);
				} else {
					tag2 = (Tag)supplier.get();
					compoundTag.put(this.name, tag2);
				}

				list.add(tag2);
			}
		}

		@Override
		public Tag createPreferredParentTag() {
			return new CompoundTag();
		}

		@Override
		public int setTag(Tag tag, Supplier<Tag> supplier) {
			if (tag instanceof CompoundTag compoundTag) {
				Tag tag2 = (Tag)supplier.get();
				Tag tag3 = compoundTag.put(this.name, tag2);
				if (!tag2.equals(tag3)) {
					return 1;
				}
			}

			return 0;
		}

		@Override
		public int removeTag(Tag tag) {
			if (tag instanceof CompoundTag compoundTag && compoundTag.contains(this.name)) {
				compoundTag.remove(this.name);
				return 1;
			}

			return 0;
		}
	}

	static class IndexedElementNode implements NbtPathArgument.Node {
		private final int index;

		public IndexedElementNode(int i) {
			this.index = i;
		}

		@Override
		public void getTag(Tag tag, List<Tag> list) {
			if (tag instanceof CollectionTag<?> collectionTag) {
				int i = collectionTag.size();
				int j = this.index < 0 ? i + this.index : this.index;
				if (0 <= j && j < i) {
					list.add((Tag)collectionTag.get(j));
				}
			}
		}

		@Override
		public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
			this.getTag(tag, list);
		}

		@Override
		public Tag createPreferredParentTag() {
			return new ListTag();
		}

		@Override
		public int setTag(Tag tag, Supplier<Tag> supplier) {
			if (tag instanceof CollectionTag<?> collectionTag) {
				int i = collectionTag.size();
				int j = this.index < 0 ? i + this.index : this.index;
				if (0 <= j && j < i) {
					Tag tag2 = (Tag)collectionTag.get(j);
					Tag tag3 = (Tag)supplier.get();
					if (!tag3.equals(tag2) && collectionTag.setTag(j, tag3)) {
						return 1;
					}
				}
			}

			return 0;
		}

		@Override
		public int removeTag(Tag tag) {
			if (tag instanceof CollectionTag<?> collectionTag) {
				int i = collectionTag.size();
				int j = this.index < 0 ? i + this.index : this.index;
				if (0 <= j && j < i) {
					collectionTag.remove(j);
					return 1;
				}
			}

			return 0;
		}
	}

	static class MatchElementNode implements NbtPathArgument.Node {
		private final CompoundTag pattern;
		private final Predicate<Tag> predicate;

		public MatchElementNode(CompoundTag compoundTag) {
			this.pattern = compoundTag;
			this.predicate = NbtPathArgument.createTagPredicate(compoundTag);
		}

		@Override
		public void getTag(Tag tag, List<Tag> list) {
			if (tag instanceof ListTag listTag) {
				listTag.stream().filter(this.predicate).forEach(list::add);
			}
		}

		@Override
		public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
			MutableBoolean mutableBoolean = new MutableBoolean();
			if (tag instanceof ListTag listTag) {
				listTag.stream().filter(this.predicate).forEach(tagx -> {
					list.add(tagx);
					mutableBoolean.setTrue();
				});
				if (mutableBoolean.isFalse()) {
					CompoundTag compoundTag = this.pattern.copy();
					listTag.add(compoundTag);
					list.add(compoundTag);
				}
			}
		}

		@Override
		public Tag createPreferredParentTag() {
			return new ListTag();
		}

		@Override
		public int setTag(Tag tag, Supplier<Tag> supplier) {
			int i = 0;
			if (tag instanceof ListTag listTag) {
				int j = listTag.size();
				if (j == 0) {
					listTag.add((Tag)supplier.get());
					i++;
				} else {
					for (int k = 0; k < j; k++) {
						Tag tag2 = listTag.get(k);
						if (this.predicate.test(tag2)) {
							Tag tag3 = (Tag)supplier.get();
							if (!tag3.equals(tag2) && listTag.setTag(k, tag3)) {
								i++;
							}
						}
					}
				}
			}

			return i;
		}

		@Override
		public int removeTag(Tag tag) {
			int i = 0;
			if (tag instanceof ListTag listTag) {
				for (int j = listTag.size() - 1; j >= 0; j--) {
					if (this.predicate.test(listTag.get(j))) {
						listTag.remove(j);
						i++;
					}
				}
			}

			return i;
		}
	}

	static class MatchObjectNode implements NbtPathArgument.Node {
		private final String name;
		private final CompoundTag pattern;
		private final Predicate<Tag> predicate;

		public MatchObjectNode(String string, CompoundTag compoundTag) {
			this.name = string;
			this.pattern = compoundTag;
			this.predicate = NbtPathArgument.createTagPredicate(compoundTag);
		}

		@Override
		public void getTag(Tag tag, List<Tag> list) {
			if (tag instanceof CompoundTag) {
				Tag tag2 = ((CompoundTag)tag).get(this.name);
				if (this.predicate.test(tag2)) {
					list.add(tag2);
				}
			}
		}

		@Override
		public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
			if (tag instanceof CompoundTag compoundTag) {
				Tag tag2 = compoundTag.get(this.name);
				if (tag2 == null) {
					Tag var6 = this.pattern.copy();
					compoundTag.put(this.name, var6);
					list.add(var6);
				} else if (this.predicate.test(tag2)) {
					list.add(tag2);
				}
			}
		}

		@Override
		public Tag createPreferredParentTag() {
			return new CompoundTag();
		}

		@Override
		public int setTag(Tag tag, Supplier<Tag> supplier) {
			if (tag instanceof CompoundTag compoundTag) {
				Tag tag2 = compoundTag.get(this.name);
				if (this.predicate.test(tag2)) {
					Tag tag3 = (Tag)supplier.get();
					if (!tag3.equals(tag2)) {
						compoundTag.put(this.name, tag3);
						return 1;
					}
				}
			}

			return 0;
		}

		@Override
		public int removeTag(Tag tag) {
			if (tag instanceof CompoundTag compoundTag) {
				Tag tag2 = compoundTag.get(this.name);
				if (this.predicate.test(tag2)) {
					compoundTag.remove(this.name);
					return 1;
				}
			}

			return 0;
		}
	}

	static class MatchRootObjectNode implements NbtPathArgument.Node {
		private final Predicate<Tag> predicate;

		public MatchRootObjectNode(CompoundTag compoundTag) {
			this.predicate = NbtPathArgument.createTagPredicate(compoundTag);
		}

		@Override
		public void getTag(Tag tag, List<Tag> list) {
			if (tag instanceof CompoundTag && this.predicate.test(tag)) {
				list.add(tag);
			}
		}

		@Override
		public void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list) {
			this.getTag(tag, list);
		}

		@Override
		public Tag createPreferredParentTag() {
			return new CompoundTag();
		}

		@Override
		public int setTag(Tag tag, Supplier<Tag> supplier) {
			return 0;
		}

		@Override
		public int removeTag(Tag tag) {
			return 0;
		}
	}

	public static class NbtPath {
		private final String original;
		private final Object2IntMap<NbtPathArgument.Node> nodeToOriginalPosition;
		private final NbtPathArgument.Node[] nodes;

		public NbtPath(String string, NbtPathArgument.Node[] nodes, Object2IntMap<NbtPathArgument.Node> object2IntMap) {
			this.original = string;
			this.nodes = nodes;
			this.nodeToOriginalPosition = object2IntMap;
		}

		public List<Tag> get(Tag tag) throws CommandSyntaxException {
			List<Tag> list = Collections.singletonList(tag);

			for (NbtPathArgument.Node node : this.nodes) {
				list = node.get(list);
				if (list.isEmpty()) {
					throw this.createNotFoundException(node);
				}
			}

			return list;
		}

		public int countMatching(Tag tag) {
			List<Tag> list = Collections.singletonList(tag);

			for (NbtPathArgument.Node node : this.nodes) {
				list = node.get(list);
				if (list.isEmpty()) {
					return 0;
				}
			}

			return list.size();
		}

		private List<Tag> getOrCreateParents(Tag tag) throws CommandSyntaxException {
			List<Tag> list = Collections.singletonList(tag);

			for (int i = 0; i < this.nodes.length - 1; i++) {
				NbtPathArgument.Node node = this.nodes[i];
				int j = i + 1;
				list = node.getOrCreate(list, this.nodes[j]::createPreferredParentTag);
				if (list.isEmpty()) {
					throw this.createNotFoundException(node);
				}
			}

			return list;
		}

		public List<Tag> getOrCreate(Tag tag, Supplier<Tag> supplier) throws CommandSyntaxException {
			List<Tag> list = this.getOrCreateParents(tag);
			NbtPathArgument.Node node = this.nodes[this.nodes.length - 1];
			return node.getOrCreate(list, supplier);
		}

		private static int apply(List<Tag> list, Function<Tag, Integer> function) {
			return (Integer)list.stream().map(function).reduce(0, (integer, integer2) -> integer + integer2);
		}

		public int set(Tag tag, Tag tag2) throws CommandSyntaxException {
			return this.set(tag, tag2::copy);
		}

		public int set(Tag tag, Supplier<Tag> supplier) throws CommandSyntaxException {
			List<Tag> list = this.getOrCreateParents(tag);
			NbtPathArgument.Node node = this.nodes[this.nodes.length - 1];
			return apply(list, tagx -> node.setTag(tagx, supplier));
		}

		public int remove(Tag tag) {
			List<Tag> list = Collections.singletonList(tag);

			for (int i = 0; i < this.nodes.length - 1; i++) {
				list = this.nodes[i].get(list);
			}

			NbtPathArgument.Node node = this.nodes[this.nodes.length - 1];
			return apply(list, node::removeTag);
		}

		private CommandSyntaxException createNotFoundException(NbtPathArgument.Node node) {
			int i = this.nodeToOriginalPosition.getInt(node);
			return NbtPathArgument.ERROR_NOTHING_FOUND.create(this.original.substring(0, i));
		}

		public String toString() {
			return this.original;
		}
	}

	interface Node {
		void getTag(Tag tag, List<Tag> list);

		void getOrCreateTag(Tag tag, Supplier<Tag> supplier, List<Tag> list);

		Tag createPreferredParentTag();

		int setTag(Tag tag, Supplier<Tag> supplier);

		int removeTag(Tag tag);

		default List<Tag> get(List<Tag> list) {
			return this.collect(list, this::getTag);
		}

		default List<Tag> getOrCreate(List<Tag> list, Supplier<Tag> supplier) {
			return this.collect(list, (tag, listx) -> this.getOrCreateTag(tag, supplier, listx));
		}

		default List<Tag> collect(List<Tag> list, BiConsumer<Tag, List<Tag>> biConsumer) {
			List<Tag> list2 = Lists.<Tag>newArrayList();

			for (Tag tag : list) {
				biConsumer.accept(tag, list2);
			}

			return list2;
		}
	}
}
