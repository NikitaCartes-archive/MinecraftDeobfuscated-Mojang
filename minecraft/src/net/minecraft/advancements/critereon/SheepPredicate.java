package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

public record SheepPredicate(Optional<Boolean> sheared, Optional<DyeColor> color) implements EntitySubPredicate {
	public static final MapCodec<SheepPredicate> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.BOOL.optionalFieldOf("sheared").forGetter(SheepPredicate::sheared), DyeColor.CODEC.optionalFieldOf("color").forGetter(SheepPredicate::color)
				)
				.apply(instance, SheepPredicate::new)
	);

	@Override
	public MapCodec<SheepPredicate> codec() {
		return EntitySubPredicates.SHEEP;
	}

	@Override
	public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
		if (entity instanceof Sheep sheep) {
			return this.sheared.isPresent() && sheep.isSheared() != this.sheared.get() ? false : !this.color.isPresent() || sheep.getColor() == this.color.get();
		} else {
			return false;
		}
	}

	public static SheepPredicate hasWool(DyeColor dyeColor) {
		return new SheepPredicate(Optional.of(false), Optional.of(dyeColor));
	}
}
