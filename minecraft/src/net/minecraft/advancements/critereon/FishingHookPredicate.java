package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

public record FishingHookPredicate(Optional<Boolean> inOpenWater) implements EntitySubPredicate {
	public static final FishingHookPredicate ANY = new FishingHookPredicate(Optional.empty());
	public static final MapCodec<FishingHookPredicate> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ExtraCodecs.strictOptionalField(Codec.BOOL, "in_open_water").forGetter(FishingHookPredicate::inOpenWater))
				.apply(instance, FishingHookPredicate::new)
	);

	public static FishingHookPredicate inOpenWater(boolean bl) {
		return new FishingHookPredicate(Optional.of(bl));
	}

	@Override
	public EntitySubPredicate.Type type() {
		return EntitySubPredicate.Types.FISHING_HOOK;
	}

	@Override
	public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
		if (this.inOpenWater.isEmpty()) {
			return true;
		} else {
			return entity instanceof FishingHook fishingHook ? (Boolean)this.inOpenWater.get() == fishingHook.isOpenWaterFishing() : false;
		}
	}
}
