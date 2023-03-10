/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public class AdvancementProgress
implements Comparable<AdvancementProgress> {
    final Map<String, CriterionProgress> criteria;
    private String[][] requirements = new String[0][];

    private AdvancementProgress(Map<String, CriterionProgress> map) {
        this.criteria = map;
    }

    public AdvancementProgress() {
        this.criteria = Maps.newHashMap();
    }

    public void update(Map<String, Criterion> map, String[][] strings) {
        Set<String> set = map.keySet();
        this.criteria.entrySet().removeIf(entry -> !set.contains(entry.getKey()));
        for (String string : set) {
            if (this.criteria.containsKey(string)) continue;
            this.criteria.put(string, new CriterionProgress());
        }
        this.requirements = strings;
    }

    public boolean isDone() {
        if (this.requirements.length == 0) {
            return false;
        }
        for (String[] strings : this.requirements) {
            boolean bl = false;
            for (String string : strings) {
                CriterionProgress criterionProgress = this.getCriterion(string);
                if (criterionProgress == null || !criterionProgress.isDone()) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            return false;
        }
        return true;
    }

    public boolean hasProgress() {
        for (CriterionProgress criterionProgress : this.criteria.values()) {
            if (!criterionProgress.isDone()) continue;
            return true;
        }
        return false;
    }

    public boolean grantProgress(String string) {
        CriterionProgress criterionProgress = this.criteria.get(string);
        if (criterionProgress != null && !criterionProgress.isDone()) {
            criterionProgress.grant();
            return true;
        }
        return false;
    }

    public boolean revokeProgress(String string) {
        CriterionProgress criterionProgress = this.criteria.get(string);
        if (criterionProgress != null && criterionProgress.isDone()) {
            criterionProgress.revoke();
            return true;
        }
        return false;
    }

    public String toString() {
        return "AdvancementProgress{criteria=" + this.criteria + ", requirements=" + Arrays.deepToString((Object[])this.requirements) + "}";
    }

    public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeMap(this.criteria, FriendlyByteBuf::writeUtf, (friendlyByteBuf, criterionProgress) -> criterionProgress.serializeToNetwork((FriendlyByteBuf)friendlyByteBuf));
    }

    public static AdvancementProgress fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        Map<String, CriterionProgress> map = friendlyByteBuf.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
        return new AdvancementProgress(map);
    }

    @Nullable
    public CriterionProgress getCriterion(String string) {
        return this.criteria.get(string);
    }

    public float getPercent() {
        if (this.criteria.isEmpty()) {
            return 0.0f;
        }
        float f = this.requirements.length;
        float g = this.countCompletedRequirements();
        return g / f;
    }

    @Nullable
    public String getProgressText() {
        if (this.criteria.isEmpty()) {
            return null;
        }
        int i = this.requirements.length;
        if (i <= 1) {
            return null;
        }
        int j = this.countCompletedRequirements();
        return j + "/" + i;
    }

    private int countCompletedRequirements() {
        int i = 0;
        for (String[] strings : this.requirements) {
            boolean bl = false;
            for (String string : strings) {
                CriterionProgress criterionProgress = this.getCriterion(string);
                if (criterionProgress == null || !criterionProgress.isDone()) continue;
                bl = true;
                break;
            }
            if (!bl) continue;
            ++i;
        }
        return i;
    }

    public Iterable<String> getRemainingCriteria() {
        ArrayList<String> list = Lists.newArrayList();
        for (Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
            if (entry.getValue().isDone()) continue;
            list.add(entry.getKey());
        }
        return list;
    }

    public Iterable<String> getCompletedCriteria() {
        ArrayList<String> list = Lists.newArrayList();
        for (Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
            if (!entry.getValue().isDone()) continue;
            list.add(entry.getKey());
        }
        return list;
    }

    @Nullable
    public Date getFirstProgressDate() {
        Date date = null;
        for (CriterionProgress criterionProgress : this.criteria.values()) {
            if (!criterionProgress.isDone() || date != null && !criterionProgress.getObtained().before(date)) continue;
            date = criterionProgress.getObtained();
        }
        return date;
    }

    @Override
    public int compareTo(AdvancementProgress advancementProgress) {
        Date date = this.getFirstProgressDate();
        Date date2 = advancementProgress.getFirstProgressDate();
        if (date == null && date2 != null) {
            return 1;
        }
        if (date != null && date2 == null) {
            return -1;
        }
        if (date == null && date2 == null) {
            return 0;
        }
        return date.compareTo(date2);
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((AdvancementProgress)object);
    }

    public static class Serializer
    implements JsonDeserializer<AdvancementProgress>,
    JsonSerializer<AdvancementProgress> {
        @Override
        public JsonElement serialize(AdvancementProgress advancementProgress, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            JsonObject jsonObject2 = new JsonObject();
            for (Map.Entry<String, CriterionProgress> entry : advancementProgress.criteria.entrySet()) {
                CriterionProgress criterionProgress = entry.getValue();
                if (!criterionProgress.isDone()) continue;
                jsonObject2.add(entry.getKey(), criterionProgress.serializeToJson());
            }
            if (!jsonObject2.entrySet().isEmpty()) {
                jsonObject.add("criteria", jsonObject2);
            }
            jsonObject.addProperty("done", advancementProgress.isDone());
            return jsonObject;
        }

        @Override
        public AdvancementProgress deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "advancement");
            JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "criteria", new JsonObject());
            AdvancementProgress advancementProgress = new AdvancementProgress();
            for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
                String string = entry.getKey();
                advancementProgress.criteria.put(string, CriterionProgress.fromJson(GsonHelper.convertToString(entry.getValue(), string)));
            }
            return advancementProgress;
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((AdvancementProgress)object, type, jsonSerializationContext);
        }
    }
}

