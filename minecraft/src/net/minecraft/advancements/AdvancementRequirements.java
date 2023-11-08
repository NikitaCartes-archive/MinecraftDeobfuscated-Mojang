package net.minecraft.advancements;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.network.FriendlyByteBuf;

public record AdvancementRequirements(List<List<String>> requirements) {
	public static final Codec<AdvancementRequirements> CODEC = Codec.STRING
		.listOf()
		.listOf()
		.xmap(AdvancementRequirements::new, AdvancementRequirements::requirements);
	public static final AdvancementRequirements EMPTY = new AdvancementRequirements(List.of());

	public AdvancementRequirements(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readList(friendlyByteBufx -> friendlyByteBufx.readList(FriendlyByteBuf::readUtf)));
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.requirements, (friendlyByteBufx, list) -> friendlyByteBufx.writeCollection(list, FriendlyByteBuf::writeUtf));
	}

	public static AdvancementRequirements allOf(Collection<String> collection) {
		return new AdvancementRequirements(collection.stream().map(List::of).toList());
	}

	public static AdvancementRequirements anyOf(Collection<String> collection) {
		return new AdvancementRequirements(List.of(List.copyOf(collection)));
	}

	public int size() {
		return this.requirements.size();
	}

	public boolean test(Predicate<String> predicate) {
		if (this.requirements.isEmpty()) {
			return false;
		} else {
			for (List<String> list : this.requirements) {
				if (!anyMatch(list, predicate)) {
					return false;
				}
			}

			return true;
		}
	}

	public int count(Predicate<String> predicate) {
		int i = 0;

		for (List<String> list : this.requirements) {
			if (anyMatch(list, predicate)) {
				i++;
			}
		}

		return i;
	}

	private static boolean anyMatch(List<String> list, Predicate<String> predicate) {
		for (String string : list) {
			if (predicate.test(string)) {
				return true;
			}
		}

		return false;
	}

	public DataResult<AdvancementRequirements> validate(Set<String> set) {
		Set<String> set2 = new ObjectOpenHashSet<>();

		for (List<String> list : this.requirements) {
			if (list.isEmpty() && set.isEmpty()) {
				return DataResult.error(() -> "Requirement entry cannot be empty");
			}

			set2.addAll(list);
		}

		if (!set.equals(set2)) {
			Set<String> set3 = Sets.<String>difference(set, set2);
			Set<String> set4 = Sets.<String>difference(set2, set);
			return DataResult.error(() -> "Advancement completion requirements did not exactly match specified criteria. Missing: " + set3 + ". Unknown: " + set4);
		} else {
			return DataResult.success(this);
		}
	}

	public boolean isEmpty() {
		return this.requirements.isEmpty();
	}

	public String toString() {
		return this.requirements.toString();
	}

	public Set<String> names() {
		Set<String> set = new ObjectOpenHashSet<>();

		for (List<String> list : this.requirements) {
			set.addAll(list);
		}

		return set;
	}

	public interface Strategy {
		AdvancementRequirements.Strategy AND = AdvancementRequirements::allOf;
		AdvancementRequirements.Strategy OR = AdvancementRequirements::anyOf;

		AdvancementRequirements create(Collection<String> collection);
	}
}
