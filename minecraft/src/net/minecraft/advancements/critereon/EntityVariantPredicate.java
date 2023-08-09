package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityVariantPredicate<V> {
	private final Function<Entity, Optional<V>> getter;
	private final EntitySubPredicate.Type type;

	public static <V> EntityVariantPredicate<V> create(Registry<V> registry, Function<Entity, Optional<V>> function) {
		return new EntityVariantPredicate<>(registry.byNameCodec(), function);
	}

	public static <V> EntityVariantPredicate<V> create(Codec<V> codec, Function<Entity, Optional<V>> function) {
		return new EntityVariantPredicate<>(codec, function);
	}

	private EntityVariantPredicate(Codec<V> codec, Function<Entity, Optional<V>> function) {
		this.getter = function;
		MapCodec<EntityVariantPredicate.SubPredicate<V>> mapCodec = RecordCodecBuilder.mapCodec(
			instance -> instance.group(codec.fieldOf("variant").forGetter(EntityVariantPredicate.SubPredicate::variant)).apply(instance, this::createPredicate)
		);
		this.type = new EntitySubPredicate.Type(mapCodec);
	}

	public EntitySubPredicate.Type type() {
		return this.type;
	}

	public EntityVariantPredicate.SubPredicate<V> createPredicate(V object) {
		return new EntityVariantPredicate.SubPredicate<>(this.type, this.getter, object);
	}

	public static record SubPredicate<V>(EntitySubPredicate.Type type, Function<Entity, Optional<V>> getter, V variant) implements EntitySubPredicate {
		@Override
		public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
			return ((Optional)this.getter.apply(entity)).filter(object -> object.equals(this.variant)).isPresent();
		}
	}
}
