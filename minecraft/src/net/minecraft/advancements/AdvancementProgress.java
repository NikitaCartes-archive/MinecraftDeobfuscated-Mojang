package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public class AdvancementProgress implements Comparable<AdvancementProgress> {
	private static final DateTimeFormatter OBTAINED_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
	private static final Codec<Instant> OBTAINED_TIME_CODEC = ExtraCodecs.temporalCodec(OBTAINED_TIME_FORMAT)
		.xmap(Instant::from, instant -> instant.atZone(ZoneId.systemDefault()));
	private static final Codec<Map<String, CriterionProgress>> CRITERIA_CODEC = Codec.unboundedMap(Codec.STRING, OBTAINED_TIME_CODEC)
		.xmap(
			map -> (Map)map.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> new CriterionProgress((Instant)entry.getValue()))),
			map -> (Map)map.entrySet()
					.stream()
					.filter(entry -> ((CriterionProgress)entry.getValue()).isDone())
					.collect(Collectors.toMap(Entry::getKey, entry -> (Instant)Objects.requireNonNull(((CriterionProgress)entry.getValue()).getObtained())))
		);
	public static final Codec<AdvancementProgress> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					CRITERIA_CODEC.optionalFieldOf("criteria", Map.of()).forGetter(advancementProgress -> advancementProgress.criteria),
					Codec.BOOL.fieldOf("done").orElse(true).forGetter(AdvancementProgress::isDone)
				)
				.apply(instance, (map, boolean_) -> new AdvancementProgress(new HashMap(map)))
	);
	private final Map<String, CriterionProgress> criteria;
	private AdvancementRequirements requirements = AdvancementRequirements.EMPTY;

	private AdvancementProgress(Map<String, CriterionProgress> map) {
		this.criteria = map;
	}

	public AdvancementProgress() {
		this.criteria = Maps.<String, CriterionProgress>newHashMap();
	}

	public void update(AdvancementRequirements advancementRequirements) {
		Set<String> set = advancementRequirements.names();
		this.criteria.entrySet().removeIf(entry -> !set.contains(entry.getKey()));

		for (String string : set) {
			this.criteria.putIfAbsent(string, new CriterionProgress());
		}

		this.requirements = advancementRequirements;
	}

	public boolean isDone() {
		return this.requirements.test(this::isCriterionDone);
	}

	public boolean hasProgress() {
		for (CriterionProgress criterionProgress : this.criteria.values()) {
			if (criterionProgress.isDone()) {
				return true;
			}
		}

		return false;
	}

	public boolean grantProgress(String string) {
		CriterionProgress criterionProgress = (CriterionProgress)this.criteria.get(string);
		if (criterionProgress != null && !criterionProgress.isDone()) {
			criterionProgress.grant();
			return true;
		} else {
			return false;
		}
	}

	public boolean revokeProgress(String string) {
		CriterionProgress criterionProgress = (CriterionProgress)this.criteria.get(string);
		if (criterionProgress != null && criterionProgress.isDone()) {
			criterionProgress.revoke();
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		return "AdvancementProgress{criteria=" + this.criteria + ", requirements=" + this.requirements + "}";
	}

	public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeMap(
			this.criteria, FriendlyByteBuf::writeUtf, (friendlyByteBufx, criterionProgress) -> criterionProgress.serializeToNetwork(friendlyByteBufx)
		);
	}

	public static AdvancementProgress fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		Map<String, CriterionProgress> map = friendlyByteBuf.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
		return new AdvancementProgress(map);
	}

	@Nullable
	public CriterionProgress getCriterion(String string) {
		return (CriterionProgress)this.criteria.get(string);
	}

	private boolean isCriterionDone(String string) {
		CriterionProgress criterionProgress = this.getCriterion(string);
		return criterionProgress != null && criterionProgress.isDone();
	}

	public float getPercent() {
		if (this.criteria.isEmpty()) {
			return 0.0F;
		} else {
			float f = (float)this.requirements.size();
			float g = (float)this.countCompletedRequirements();
			return g / f;
		}
	}

	@Nullable
	public Component getProgressText() {
		if (this.criteria.isEmpty()) {
			return null;
		} else {
			int i = this.requirements.size();
			if (i <= 1) {
				return null;
			} else {
				int j = this.countCompletedRequirements();
				return Component.translatable("advancements.progress", j, i);
			}
		}
	}

	private int countCompletedRequirements() {
		return this.requirements.count(this::isCriterionDone);
	}

	public Iterable<String> getRemainingCriteria() {
		List<String> list = Lists.<String>newArrayList();

		for (Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
			if (!((CriterionProgress)entry.getValue()).isDone()) {
				list.add((String)entry.getKey());
			}
		}

		return list;
	}

	public Iterable<String> getCompletedCriteria() {
		List<String> list = Lists.<String>newArrayList();

		for (Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
			if (((CriterionProgress)entry.getValue()).isDone()) {
				list.add((String)entry.getKey());
			}
		}

		return list;
	}

	@Nullable
	public Instant getFirstProgressDate() {
		return (Instant)this.criteria.values().stream().map(CriterionProgress::getObtained).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
	}

	public int compareTo(AdvancementProgress advancementProgress) {
		Instant instant = this.getFirstProgressDate();
		Instant instant2 = advancementProgress.getFirstProgressDate();
		if (instant == null && instant2 != null) {
			return 1;
		} else if (instant != null && instant2 == null) {
			return -1;
		} else {
			return instant == null && instant2 == null ? 0 : instant.compareTo(instant2);
		}
	}
}
