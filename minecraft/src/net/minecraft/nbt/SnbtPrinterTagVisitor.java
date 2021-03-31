package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.Util;

public class SnbtPrinterTagVisitor implements TagVisitor {
	private static final Map<String, List<String>> KEY_ORDER = Util.make(Maps.<String, List<String>>newHashMap(), hashMap -> {
		hashMap.put("{}", Lists.newArrayList("DataVersion", "author", "size", "data", "entities", "palette", "palettes"));
		hashMap.put("{}.data.[].{}", Lists.newArrayList("pos", "state", "nbt"));
		hashMap.put("{}.entities.[].{}", Lists.newArrayList("blockPos", "pos"));
	});
	private static final Set<String> NO_INDENTATION = Sets.<String>newHashSet("{}.size.[]", "{}.data.[].{}", "{}.palette.[].{}", "{}.entities.[].{}");
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
	private final List<String> path;
	private String result;

	public SnbtPrinterTagVisitor() {
		this("    ", 0, Lists.<String>newArrayList());
	}

	public SnbtPrinterTagVisitor(String string, int i, List<String> list) {
		this.indentation = string;
		this.depth = i;
		this.path = list;
	}

	public String visit(Tag tag) {
		tag.accept(this);
		return this.result;
	}

	@Override
	public void visitString(StringTag stringTag) {
		this.result = StringTag.quoteAndEscape(stringTag.getAsString());
	}

	@Override
	public void visitByte(ByteTag byteTag) {
		this.result = byteTag.getAsNumber() + "b";
	}

	@Override
	public void visitShort(ShortTag shortTag) {
		this.result = shortTag.getAsNumber() + "s";
	}

	@Override
	public void visitInt(IntTag intTag) {
		this.result = String.valueOf(intTag.getAsNumber());
	}

	@Override
	public void visitLong(LongTag longTag) {
		this.result = longTag.getAsNumber() + "L";
	}

	@Override
	public void visitFloat(FloatTag floatTag) {
		this.result = floatTag.getAsFloat() + "f";
	}

	@Override
	public void visitDouble(DoubleTag doubleTag) {
		this.result = doubleTag.getAsDouble() + "d";
	}

	@Override
	public void visitByteArray(ByteArrayTag byteArrayTag) {
		StringBuilder stringBuilder = new StringBuilder("[").append("B").append(";");
		byte[] bs = byteArrayTag.getAsByteArray();

		for (int i = 0; i < bs.length; i++) {
			stringBuilder.append(" ").append(bs[i]).append("B");
			if (i != bs.length - 1) {
				stringBuilder.append(ELEMENT_SEPARATOR);
			}
		}

		stringBuilder.append("]");
		this.result = stringBuilder.toString();
	}

	@Override
	public void visitIntArray(IntArrayTag intArrayTag) {
		StringBuilder stringBuilder = new StringBuilder("[").append("I").append(";");
		int[] is = intArrayTag.getAsIntArray();

		for (int i = 0; i < is.length; i++) {
			stringBuilder.append(" ").append(is[i]);
			if (i != is.length - 1) {
				stringBuilder.append(ELEMENT_SEPARATOR);
			}
		}

		stringBuilder.append("]");
		this.result = stringBuilder.toString();
	}

	@Override
	public void visitLongArray(LongArrayTag longArrayTag) {
		String string = "L";
		StringBuilder stringBuilder = new StringBuilder("[").append("L").append(";");
		long[] ls = longArrayTag.getAsLongArray();

		for (int i = 0; i < ls.length; i++) {
			stringBuilder.append(" ").append(ls[i]).append("L");
			if (i != ls.length - 1) {
				stringBuilder.append(ELEMENT_SEPARATOR);
			}
		}

		stringBuilder.append("]");
		this.result = stringBuilder.toString();
	}

	@Override
	public void visitList(ListTag listTag) {
		if (listTag.isEmpty()) {
			this.result = "[]";
		} else {
			StringBuilder stringBuilder = new StringBuilder("[");
			this.pushPath("[]");
			String string = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
			if (!string.isEmpty()) {
				stringBuilder.append("\n");
			}

			for (int i = 0; i < listTag.size(); i++) {
				stringBuilder.append(Strings.repeat(string, this.depth + 1));
				stringBuilder.append(new SnbtPrinterTagVisitor(string, this.depth + 1, this.path).visit(listTag.get(i)));
				if (i != listTag.size() - 1) {
					stringBuilder.append(ELEMENT_SEPARATOR).append(string.isEmpty() ? " " : "\n");
				}
			}

			if (!string.isEmpty()) {
				stringBuilder.append("\n").append(Strings.repeat(string, this.depth));
			}

			stringBuilder.append("]");
			this.result = stringBuilder.toString();
			this.popPath();
		}
	}

	@Override
	public void visitCompound(CompoundTag compoundTag) {
		if (compoundTag.isEmpty()) {
			this.result = "{}";
		} else {
			StringBuilder stringBuilder = new StringBuilder("{");
			this.pushPath("{}");
			String string = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
			if (!string.isEmpty()) {
				stringBuilder.append("\n");
			}

			Collection<String> collection = this.getKeys(compoundTag);
			Iterator<String> iterator = collection.iterator();

			while (iterator.hasNext()) {
				String string2 = (String)iterator.next();
				Tag tag = compoundTag.get(string2);
				this.pushPath(string2);
				stringBuilder.append(Strings.repeat(string, this.depth + 1))
					.append(handleEscapePretty(string2))
					.append(NAME_VALUE_SEPARATOR)
					.append(" ")
					.append(new SnbtPrinterTagVisitor(string, this.depth + 1, this.path).visit(tag));
				this.popPath();
				if (iterator.hasNext()) {
					stringBuilder.append(ELEMENT_SEPARATOR).append(string.isEmpty() ? " " : "\n");
				}
			}

			if (!string.isEmpty()) {
				stringBuilder.append("\n").append(Strings.repeat(string, this.depth));
			}

			stringBuilder.append("}");
			this.result = stringBuilder.toString();
			this.popPath();
		}
	}

	private void popPath() {
		this.path.remove(this.path.size() - 1);
	}

	private void pushPath(String string) {
		this.path.add(string);
	}

	protected List<String> getKeys(CompoundTag compoundTag) {
		Set<String> set = Sets.<String>newHashSet(compoundTag.getAllKeys());
		List<String> list = Lists.<String>newArrayList();
		List<String> list2 = (List<String>)KEY_ORDER.get(this.pathString());
		if (list2 != null) {
			for (String string : list2) {
				if (set.remove(string)) {
					list.add(string);
				}
			}

			if (!set.isEmpty()) {
				set.stream().sorted().forEach(list::add);
			}
		} else {
			list.addAll(set);
			Collections.sort(list);
		}

		return list;
	}

	public String pathString() {
		return String.join(".", this.path);
	}

	protected static String handleEscapePretty(String string) {
		return SIMPLE_VALUE.matcher(string).matches() ? string : StringTag.quoteAndEscape(string);
	}

	@Override
	public void visitEnd(EndTag endTag) {
	}
}
