package net.minecraft.advancements.critereon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;

public record SlimePredicate(MinMaxBounds.Ints size) implements EntitySubPredicate {
	public static final MapCodec<SlimePredicate> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("size", MinMaxBounds.Ints.ANY).forGetter(SlimePredicate::size))
				.apply(instance, SlimePredicate::new)
	);

	public static SlimePredicate sized(MinMaxBounds.Ints ints) {
		return new SlimePredicate(ints);
	}

	@Override
	public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
		return entity instanceof Slime slime ? this.size.matches(slime.getSize()) : false;
	}

	@Override
	public MapCodec<SlimePredicate> codec() {
		return EntitySubPredicates.SLIME;
	}
}
