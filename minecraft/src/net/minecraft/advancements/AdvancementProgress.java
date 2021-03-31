package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

public class AdvancementProgress implements Comparable<AdvancementProgress> {
	private final Map<String, CriterionProgress> criteria;
	private String[][] requirements = new String[0][];

	private AdvancementProgress(Map<String, CriterionProgress> map) {
		this.criteria = map;
	}

	public AdvancementProgress() {
		this.criteria = Maps.<String, CriterionProgress>newHashMap();
	}

	public void update(Map<String, Criterion> map, String[][] strings) {
		Set<String> set = map.keySet();
		this.criteria.entrySet().removeIf(entry -> !set.contains(entry.getKey()));

		for (String string : set) {
			if (!this.criteria.containsKey(string)) {
				this.criteria.put(string, new CriterionProgress());
			}
		}

		this.requirements = strings;
	}

	public boolean isDone() {
		if (this.requirements.length == 0) {
			return false;
		} else {
			for (String[] strings : this.requirements) {
				boolean bl = false;

				for (String string : strings) {
					CriterionProgress criterionProgress = this.getCriterion(string);
					if (criterionProgress != null && criterionProgress.isDone()) {
						bl = true;
						break;
					}
				}

				if (!bl) {
					return false;
				}
			}

			return true;
		}
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
		return "AdvancementProgress{criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
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

	public float getPercent() {
		if (this.criteria.isEmpty()) {
			return 0.0F;
		} else {
			float f = (float)this.requirements.length;
			float g = (float)this.countCompletedRequirements();
			return g / f;
		}
	}

	@Nullable
	public String getProgressText() {
		if (this.criteria.isEmpty()) {
			return null;
		} else {
			int i = this.requirements.length;
			if (i <= 1) {
				return null;
			} else {
				int j = this.countCompletedRequirements();
				return j + "/" + i;
			}
		}
	}

	private int countCompletedRequirements() {
		int i = 0;

		for (String[] strings : this.requirements) {
			boolean bl = false;

			for (String string : strings) {
				CriterionProgress criterionProgress = this.getCriterion(string);
				if (criterionProgress != null && criterionProgress.isDone()) {
					bl = true;
					break;
				}
			}

			if (bl) {
				i++;
			}
		}

		return i;
	}

	public Iterable<String> getRemainingCriteria() {
		List<String> list = Lists.<String>newArrayList();

		for (Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
			if (!((CriterionProgress)entry.getValue()).isDone()) {
				list.add(entry.getKey());
			}
		}

		return list;
	}

	public Iterable<String> getCompletedCriteria() {
		List<String> list = Lists.<String>newArrayList();

		for (Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
			if (((CriterionProgress)entry.getValue()).isDone()) {
				list.add(entry.getKey());
			}
		}

		return list;
	}

	@Nullable
	public Date getFirstProgressDate() {
		Date date = null;

		for (CriterionProgress criterionProgress : this.criteria.values()) {
			if (criterionProgress.isDone() && (date == null || criterionProgress.getObtained().before(date))) {
				date = criterionProgress.getObtained();
			}
		}

		return date;
	}

	public int compareTo(AdvancementProgress advancementProgress) {
		Date date = this.getFirstProgressDate();
		Date date2 = advancementProgress.getFirstProgressDate();
		if (date == null && date2 != null) {
			return 1;
		} else if (date != null && date2 == null) {
			return -1;
		} else {
			return date == null && date2 == null ? 0 : date.compareTo(date2);
		}
	}

	public static class Serializer implements JsonDeserializer<AdvancementProgress>, JsonSerializer<AdvancementProgress> {
		public JsonElement serialize(AdvancementProgress advancementProgress, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			JsonObject jsonObject2 = new JsonObject();

			for (Entry<String, CriterionProgress> entry : advancementProgress.criteria.entrySet()) {
				CriterionProgress criterionProgress = (CriterionProgress)entry.getValue();
				if (criterionProgress.isDone()) {
					jsonObject2.add((String)entry.getKey(), criterionProgress.serializeToJson());
				}
			}

			if (!jsonObject2.entrySet().isEmpty()) {
				jsonObject.add("criteria", jsonObject2);
			}

			jsonObject.addProperty("done", advancementProgress.isDone());
			return jsonObject;
		}

		public AdvancementProgress deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "advancement");
			JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "criteria", new JsonObject());
			AdvancementProgress advancementProgress = new AdvancementProgress();

			for (Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
				String string = (String)entry.getKey();
				advancementProgress.criteria.put(string, CriterionProgress.fromJson(GsonHelper.convertToString((JsonElement)entry.getValue(), string)));
			}

			return advancementProgress;
		}
	}
}
