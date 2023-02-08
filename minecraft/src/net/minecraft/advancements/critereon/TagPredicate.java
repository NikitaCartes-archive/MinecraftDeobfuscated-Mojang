package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;

public class TagPredicate<T> {
	private final TagKey<T> tag;
	private final boolean expected;

	public TagPredicate(TagKey<T> tagKey, boolean bl) {
		this.tag = tagKey;
		this.expected = bl;
	}

	public static <T> TagPredicate<T> is(TagKey<T> tagKey) {
		return new TagPredicate<>(tagKey, true);
	}

	public static <T> TagPredicate<T> isNot(TagKey<T> tagKey) {
		return new TagPredicate<>(tagKey, false);
	}

	public boolean matches(Holder<T> holder) {
		return holder.is(this.tag) == this.expected;
	}

	public JsonElement serializeToJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", this.tag.location().toString());
		jsonObject.addProperty("expected", this.expected);
		return jsonObject;
	}

	public static <T> TagPredicate<T> fromJson(@Nullable JsonElement jsonElement, ResourceKey<? extends Registry<T>> resourceKey) {
		if (jsonElement == null) {
			throw new JsonParseException("Expected a tag predicate");
		} else {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "Tag Predicate");
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "id"));
			boolean bl = GsonHelper.getAsBoolean(jsonObject, "expected");
			return new TagPredicate<>(TagKey.create(resourceKey, resourceLocation), bl);
		}
	}
}
