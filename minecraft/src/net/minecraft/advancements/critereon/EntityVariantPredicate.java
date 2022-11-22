package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityVariantPredicate<V> {
	private static final String VARIANT_KEY = "variant";
	final Codec<V> variantCodec;
	final Function<Entity, Optional<V>> getter;
	final EntitySubPredicate.Type type;

	public static <V> EntityVariantPredicate<V> create(Registry<V> registry, Function<Entity, Optional<V>> function) {
		return new EntityVariantPredicate<>(registry.byNameCodec(), function);
	}

	public static <V> EntityVariantPredicate<V> create(Codec<V> codec, Function<Entity, Optional<V>> function) {
		return new EntityVariantPredicate<>(codec, function);
	}

	private EntityVariantPredicate(Codec<V> codec, Function<Entity, Optional<V>> function) {
		this.variantCodec = codec;
		this.getter = function;
		this.type = jsonObject -> {
			JsonElement jsonElement = jsonObject.get("variant");
			if (jsonElement == null) {
				throw new JsonParseException("Missing variant field");
			} else {
				V object = Util.getOrThrow(codec.decode(new Dynamic<>(JsonOps.INSTANCE, jsonElement)), JsonParseException::new).getFirst();
				return this.createPredicate(object);
			}
		};
	}

	public EntitySubPredicate.Type type() {
		return this.type;
	}

	public EntitySubPredicate createPredicate(V object) {
		return new EntitySubPredicate() {
			@Override
			public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
				return ((Optional)EntityVariantPredicate.this.getter.apply(entity)).filter(object2 -> object2.equals(object)).isPresent();
			}

			@Override
			public JsonObject serializeCustomData() {
				JsonObject jsonObject = new JsonObject();
				jsonObject.add(
					"variant",
					Util.getOrThrow(
						EntityVariantPredicate.this.variantCodec.encodeStart(JsonOps.INSTANCE, object),
						string -> new JsonParseException("Can't serialize variant " + object + ", message " + string)
					)
				);
				return jsonObject;
			}

			@Override
			public EntitySubPredicate.Type type() {
				return EntityVariantPredicate.this.type;
			}
		};
	}
}
