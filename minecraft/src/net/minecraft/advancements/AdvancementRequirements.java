package net.minecraft.advancements;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

public record AdvancementRequirements(String[][] requirements) {
	public static final AdvancementRequirements EMPTY = new AdvancementRequirements(new String[0][]);

	public AdvancementRequirements(FriendlyByteBuf friendlyByteBuf) {
		this(read(friendlyByteBuf));
	}

	private static String[][] read(FriendlyByteBuf friendlyByteBuf) {
		String[][] strings = new String[friendlyByteBuf.readVarInt()][];

		for (int i = 0; i < strings.length; i++) {
			strings[i] = new String[friendlyByteBuf.readVarInt()];

			for (int j = 0; j < strings[i].length; j++) {
				strings[i][j] = friendlyByteBuf.readUtf();
			}
		}

		return strings;
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.requirements.length);

		for (String[] strings : this.requirements) {
			friendlyByteBuf.writeVarInt(strings.length);

			for (String string : strings) {
				friendlyByteBuf.writeUtf(string);
			}
		}
	}

	public static AdvancementRequirements allOf(Collection<String> collection) {
		return new AdvancementRequirements((String[][])collection.stream().map(string -> new String[]{string}).toArray(String[][]::new));
	}

	public static AdvancementRequirements anyOf(Collection<String> collection) {
		return new AdvancementRequirements(new String[][]{(String[])collection.toArray(String[]::new)});
	}

	public int size() {
		return this.requirements.length;
	}

	public boolean test(Predicate<String> predicate) {
		if (this.requirements.length == 0) {
			return false;
		} else {
			for (String[] strings : this.requirements) {
				if (!anyMatch(strings, predicate)) {
					return false;
				}
			}

			return true;
		}
	}

	public int count(Predicate<String> predicate) {
		int i = 0;

		for (String[] strings : this.requirements) {
			if (anyMatch(strings, predicate)) {
				i++;
			}
		}

		return i;
	}

	private static boolean anyMatch(String[] strings, Predicate<String> predicate) {
		for (String string : strings) {
			if (predicate.test(string)) {
				return true;
			}
		}

		return false;
	}

	public static AdvancementRequirements fromJson(JsonArray jsonArray, Set<String> set) {
		String[][] strings = new String[jsonArray.size()][];
		Set<String> set2 = new ObjectOpenHashSet<>();

		for (int i = 0; i < jsonArray.size(); i++) {
			JsonArray jsonArray2 = GsonHelper.convertToJsonArray(jsonArray.get(i), "requirements[" + i + "]");
			if (jsonArray2.isEmpty() && set.isEmpty()) {
				throw new JsonSyntaxException("Requirement entry cannot be empty");
			}

			strings[i] = new String[jsonArray2.size()];

			for (int j = 0; j < jsonArray2.size(); j++) {
				String string = GsonHelper.convertToString(jsonArray2.get(j), "requirements[" + i + "][" + j + "]");
				strings[i][j] = string;
				set2.add(string);
			}
		}

		if (!set.equals(set2)) {
			Set<String> set3 = Sets.<String>difference(set, set2);
			Set<String> set4 = Sets.<String>difference(set2, set);
			throw new JsonSyntaxException("Advancement completion requirements did not exactly match specified criteria. Missing: " + set3 + ". Unknown: " + set4);
		} else {
			return new AdvancementRequirements(strings);
		}
	}

	public JsonArray toJson() {
		JsonArray jsonArray = new JsonArray();

		for (String[] strings : this.requirements) {
			JsonArray jsonArray2 = new JsonArray();
			Arrays.stream(strings).forEach(jsonArray2::add);
			jsonArray.add(jsonArray2);
		}

		return jsonArray;
	}

	public boolean isEmpty() {
		return this.requirements.length == 0;
	}

	public String toString() {
		return Arrays.deepToString(this.requirements);
	}

	public Set<String> names() {
		Set<String> set = new ObjectOpenHashSet<>();

		for (String[] strings : this.requirements) {
			Collections.addAll(set, strings);
		}

		return set;
	}

	public interface Strategy {
		AdvancementRequirements.Strategy AND = AdvancementRequirements::allOf;
		AdvancementRequirements.Strategy OR = AdvancementRequirements::anyOf;

		AdvancementRequirements create(Collection<String> collection);
	}
}
