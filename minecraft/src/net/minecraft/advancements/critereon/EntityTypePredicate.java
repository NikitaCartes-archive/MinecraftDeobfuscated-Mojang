package net.minecraft.advancements.critereon;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;

public abstract class EntityTypePredicate {
	public static final EntityTypePredicate ANY = new EntityTypePredicate() {
		@Override
		public boolean matches(EntityType<?> entityType) {
			return true;
		}

		@Override
		public JsonElement serializeToJson() {
			return JsonNull.INSTANCE;
		}
	};
	private static final Joiner COMMA_JOINER = Joiner.on(", ");

	public abstract boolean matches(EntityType<?> entityType);

	public abstract JsonElement serializeToJson();

	public static EntityTypePredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			String string = GsonHelper.convertToString(jsonElement, "type");
			if (string.startsWith("#")) {
				ResourceLocation resourceLocation = new ResourceLocation(string.substring(1));
				return new EntityTypePredicate.TagPredicate(TagKey.create(Registries.ENTITY_TYPE, resourceLocation));
			} else {
				ResourceLocation resourceLocation = new ResourceLocation(string);
				EntityType<?> entityType = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE
					.getOptional(resourceLocation)
					.orElseThrow(
						() -> new JsonSyntaxException(
								"Unknown entity type '" + resourceLocation + "', valid types are: " + COMMA_JOINER.join(BuiltInRegistries.ENTITY_TYPE.keySet())
							)
					);
				return new EntityTypePredicate.TypePredicate(entityType);
			}
		} else {
			return ANY;
		}
	}

	public static EntityTypePredicate of(EntityType<?> entityType) {
		return new EntityTypePredicate.TypePredicate(entityType);
	}

	public static EntityTypePredicate of(TagKey<EntityType<?>> tagKey) {
		return new EntityTypePredicate.TagPredicate(tagKey);
	}

	static class TagPredicate extends EntityTypePredicate {
		private final TagKey<EntityType<?>> tag;

		public TagPredicate(TagKey<EntityType<?>> tagKey) {
			this.tag = tagKey;
		}

		@Override
		public boolean matches(EntityType<?> entityType) {
			return entityType.is(this.tag);
		}

		@Override
		public JsonElement serializeToJson() {
			return new JsonPrimitive("#" + this.tag.location());
		}
	}

	static class TypePredicate extends EntityTypePredicate {
		private final EntityType<?> type;

		public TypePredicate(EntityType<?> entityType) {
			this.type = entityType;
		}

		@Override
		public boolean matches(EntityType<?> entityType) {
			return this.type == entityType;
		}

		@Override
		public JsonElement serializeToJson() {
			return new JsonPrimitive(BuiltInRegistries.ENTITY_TYPE.getKey(this.type).toString());
		}
	}
}
