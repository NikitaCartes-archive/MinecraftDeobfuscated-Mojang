package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetFireworksFunction extends LootItemConditionalFunction {
	public static final MapCodec<SetFireworksFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<List<FireworkExplosion>, ListOperation, Optional<Integer>>and(
					instance.group(
						FireworkExplosion.CODEC
							.sizeLimitedListOf(256)
							.optionalFieldOf("explosions", List.of())
							.forGetter(setFireworksFunction -> setFireworksFunction.explosions),
						ListOperation.codec(256).forGetter(setFireworksFunction -> setFireworksFunction.explosionsOperation),
						ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("flight_duration").forGetter(setFireworksFunction -> setFireworksFunction.flightDuration)
					)
				)
				.apply(instance, SetFireworksFunction::new)
	);
	public static final Fireworks DEFAULT_VALUE = new Fireworks(0, List.of());
	private final List<FireworkExplosion> explosions;
	private final ListOperation explosionsOperation;
	private final Optional<Integer> flightDuration;

	protected SetFireworksFunction(List<LootItemCondition> list, List<FireworkExplosion> list2, ListOperation listOperation, Optional<Integer> optional) {
		super(list);
		this.explosions = list2;
		this.explosionsOperation = listOperation;
		this.flightDuration = optional;
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.update(DataComponents.FIREWORKS, DEFAULT_VALUE, this::apply);
		return itemStack;
	}

	private Fireworks apply(Fireworks fireworks) {
		List<FireworkExplosion> list = this.explosionsOperation.apply(fireworks.explosions(), this.explosions, 256);
		return new Fireworks((Integer)this.flightDuration.orElseGet(fireworks::flightDuration), list);
	}

	@Override
	public LootItemFunctionType<SetFireworksFunction> getType() {
		return LootItemFunctions.SET_FIREWORKS;
	}
}
