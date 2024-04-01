package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record PotatoPredicate(boolean isPotato) implements EntitySubPredicate {
	public static final MapCodec<PotatoPredicate> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(Codec.BOOL.fieldOf("is_potato").forGetter(PotatoPredicate::isPotato)).apply(instance, PotatoPredicate::new)
	);

	@Override
	public MapCodec<PotatoPredicate> codec() {
		return CODEC;
	}

	@Override
	public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
		return entity.isPotato() == this.isPotato;
	}
}
