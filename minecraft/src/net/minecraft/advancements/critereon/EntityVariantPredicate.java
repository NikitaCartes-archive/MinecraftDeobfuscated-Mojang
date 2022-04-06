package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityVariantPredicate<V> {
	private static final String VARIANT_KEY = "variant";
	final Registry<V> registry;
	final Function<Entity, Optional<V>> getter;
	final EntitySubPredicate.Type type;

	public static <V> EntityVariantPredicate<V> create(Registry<V> registry, Function<Entity, Optional<V>> function) {
		return new EntityVariantPredicate<>(registry, function);
	}

	private EntityVariantPredicate(Registry<V> registry, Function<Entity, Optional<V>> function) {
		this.registry = registry;
		this.getter = function;
		this.type = jsonObject -> {
			String string = GsonHelper.getAsString(jsonObject, "variant");
			V object = registry.get(ResourceLocation.tryParse(string));
			if (object == null) {
				throw new JsonSyntaxException("Unknown variant: " + string);
			} else {
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
				jsonObject.addProperty("variant", EntityVariantPredicate.this.registry.getKey(object).toString());
				return jsonObject;
			}

			@Override
			public EntitySubPredicate.Type type() {
				return EntityVariantPredicate.this.type;
			}
		};
	}
}
