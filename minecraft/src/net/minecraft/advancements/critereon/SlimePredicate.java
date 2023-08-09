package net.minecraft.advancements.critereon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;

public record SlimePredicate(MinMaxBounds.Ints size) implements EntitySubPredicate {
	public static final MapCodec<SlimePredicate> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "size", MinMaxBounds.Ints.ANY).forGetter(SlimePredicate::size))
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
	public EntitySubPredicate.Type type() {
		return EntitySubPredicate.Types.SLIME;
	}
}
