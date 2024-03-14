package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetFireworkExplosionFunction extends LootItemConditionalFunction {
	public static final Codec<SetFireworkExplosionFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.<Optional<FireworkExplosion.Shape>, Optional<IntList>, Optional<IntList>, Optional<Boolean>, Optional<Boolean>>and(
					instance.group(
						ExtraCodecs.strictOptionalField(FireworkExplosion.Shape.CODEC, "shape").forGetter(setFireworkExplosionFunction -> setFireworkExplosionFunction.shape),
						ExtraCodecs.strictOptionalField(FireworkExplosion.COLOR_LIST_CODEC, "colors")
							.forGetter(setFireworkExplosionFunction -> setFireworkExplosionFunction.colors),
						ExtraCodecs.strictOptionalField(FireworkExplosion.COLOR_LIST_CODEC, "fade_colors")
							.forGetter(setFireworkExplosionFunction -> setFireworkExplosionFunction.fadeColors),
						ExtraCodecs.strictOptionalField(Codec.BOOL, "trail").forGetter(setFireworkExplosionFunction -> setFireworkExplosionFunction.trail),
						ExtraCodecs.strictOptionalField(Codec.BOOL, "twinkle").forGetter(setFireworkExplosionFunction -> setFireworkExplosionFunction.twinkle)
					)
				)
				.apply(instance, SetFireworkExplosionFunction::new)
	);
	public static final FireworkExplosion DEFAULT_VALUE = new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, IntList.of(), IntList.of(), false, false);
	final Optional<FireworkExplosion.Shape> shape;
	final Optional<IntList> colors;
	final Optional<IntList> fadeColors;
	final Optional<Boolean> trail;
	final Optional<Boolean> twinkle;

	public SetFireworkExplosionFunction(
		List<LootItemCondition> list,
		Optional<FireworkExplosion.Shape> optional,
		Optional<IntList> optional2,
		Optional<IntList> optional3,
		Optional<Boolean> optional4,
		Optional<Boolean> optional5
	) {
		super(list);
		this.shape = optional;
		this.colors = optional2;
		this.fadeColors = optional3;
		this.trail = optional4;
		this.twinkle = optional5;
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.update(DataComponents.FIREWORK_EXPLOSION, DEFAULT_VALUE, this::apply);
		return itemStack;
	}

	private FireworkExplosion apply(FireworkExplosion fireworkExplosion) {
		return new FireworkExplosion(
			(FireworkExplosion.Shape)this.shape.orElseGet(fireworkExplosion::shape),
			(IntList)this.colors.orElseGet(fireworkExplosion::colors),
			(IntList)this.fadeColors.orElseGet(fireworkExplosion::fadeColors),
			(Boolean)this.trail.orElseGet(fireworkExplosion::hasTrail),
			(Boolean)this.twinkle.orElseGet(fireworkExplosion::hasTwinkle)
		);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_FIREWORK_EXPLOSION;
	}
}
