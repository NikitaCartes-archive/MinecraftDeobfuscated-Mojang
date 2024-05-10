package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

public interface AllOf {
	static <T, A extends T> MapCodec<A> codec(Codec<T> codec, Function<List<T>, A> function, Function<A, List<T>> function2) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(codec.listOf().fieldOf("effects").forGetter(function2)).apply(instance, function));
	}

	static AllOf.EntityEffects entityEffects(EnchantmentEntityEffect... enchantmentEntityEffects) {
		return new AllOf.EntityEffects(List.of(enchantmentEntityEffects));
	}

	static AllOf.LocationBasedEffects locationBasedEffects(EnchantmentLocationBasedEffect... enchantmentLocationBasedEffects) {
		return new AllOf.LocationBasedEffects(List.of(enchantmentLocationBasedEffects));
	}

	static AllOf.ValueEffects valueEffects(EnchantmentValueEffect... enchantmentValueEffects) {
		return new AllOf.ValueEffects(List.of(enchantmentValueEffects));
	}

	public static record EntityEffects(List<EnchantmentEntityEffect> effects) implements EnchantmentEntityEffect {
		public static final MapCodec<AllOf.EntityEffects> CODEC = AllOf.codec(EnchantmentEntityEffect.CODEC, AllOf.EntityEffects::new, AllOf.EntityEffects::effects);

		@Override
		public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
			for (EnchantmentEntityEffect enchantmentEntityEffect : this.effects) {
				enchantmentEntityEffect.apply(serverLevel, i, enchantedItemInUse, entity, vec3);
			}
		}

		@Override
		public MapCodec<AllOf.EntityEffects> codec() {
			return CODEC;
		}
	}

	public static record LocationBasedEffects(List<EnchantmentLocationBasedEffect> effects) implements EnchantmentLocationBasedEffect {
		public static final MapCodec<AllOf.LocationBasedEffects> CODEC = AllOf.codec(
			EnchantmentLocationBasedEffect.CODEC, AllOf.LocationBasedEffects::new, AllOf.LocationBasedEffects::effects
		);

		@Override
		public void onChangedBlock(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, boolean bl) {
			for (EnchantmentLocationBasedEffect enchantmentLocationBasedEffect : this.effects) {
				enchantmentLocationBasedEffect.onChangedBlock(serverLevel, i, enchantedItemInUse, entity, vec3, bl);
			}
		}

		@Override
		public void onDeactivated(EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, int i) {
			for (EnchantmentLocationBasedEffect enchantmentLocationBasedEffect : this.effects) {
				enchantmentLocationBasedEffect.onDeactivated(enchantedItemInUse, entity, vec3, i);
			}
		}

		@Override
		public MapCodec<AllOf.LocationBasedEffects> codec() {
			return CODEC;
		}
	}

	public static record ValueEffects(List<EnchantmentValueEffect> effects) implements EnchantmentValueEffect {
		public static final MapCodec<AllOf.ValueEffects> CODEC = AllOf.codec(EnchantmentValueEffect.CODEC, AllOf.ValueEffects::new, AllOf.ValueEffects::effects);

		@Override
		public float process(int i, RandomSource randomSource, float f) {
			for (EnchantmentValueEffect enchantmentValueEffect : this.effects) {
				f = enchantmentValueEffect.process(i, randomSource, f);
			}

			return f;
		}

		@Override
		public MapCodec<AllOf.ValueEffects> codec() {
			return CODEC;
		}
	}
}
