package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;

public record UniformGenerator(NumberProvider min, NumberProvider max) implements NumberProvider {
	public static final MapCodec<UniformGenerator> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					NumberProviders.CODEC.fieldOf("min").forGetter(UniformGenerator::min), NumberProviders.CODEC.fieldOf("max").forGetter(UniformGenerator::max)
				)
				.apply(instance, UniformGenerator::new)
	);

	@Override
	public LootNumberProviderType getType() {
		return NumberProviders.UNIFORM;
	}

	public static UniformGenerator between(float f, float g) {
		return new UniformGenerator(ConstantValue.exactly(f), ConstantValue.exactly(g));
	}

	@Override
	public int getInt(LootContext lootContext) {
		return Mth.nextInt(lootContext.getRandom(), this.min.getInt(lootContext), this.max.getInt(lootContext));
	}

	@Override
	public float getFloat(LootContext lootContext) {
		return Mth.nextFloat(lootContext.getRandom(), this.min.getFloat(lootContext), this.max.getFloat(lootContext));
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return Sets.<ContextKey<?>>union(this.min.getReferencedContextParams(), this.max.getReferencedContextParams());
	}
}
