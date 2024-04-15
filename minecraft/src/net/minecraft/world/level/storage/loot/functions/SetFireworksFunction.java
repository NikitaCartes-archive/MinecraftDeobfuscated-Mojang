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
				.<Optional<ListOperation.StandAlone<FireworkExplosion>>, Optional<Integer>>and(
					instance.group(
						ListOperation.StandAlone.codec(FireworkExplosion.CODEC, 256)
							.optionalFieldOf("explosions")
							.forGetter(setFireworksFunction -> setFireworksFunction.explosions),
						ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("flight_duration").forGetter(setFireworksFunction -> setFireworksFunction.flightDuration)
					)
				)
				.apply(instance, SetFireworksFunction::new)
	);
	public static final Fireworks DEFAULT_VALUE = new Fireworks(0, List.of());
	private final Optional<ListOperation.StandAlone<FireworkExplosion>> explosions;
	private final Optional<Integer> flightDuration;

	protected SetFireworksFunction(List<LootItemCondition> list, Optional<ListOperation.StandAlone<FireworkExplosion>> optional, Optional<Integer> optional2) {
		super(list);
		this.explosions = optional;
		this.flightDuration = optional2;
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.update(DataComponents.FIREWORKS, DEFAULT_VALUE, this::apply);
		return itemStack;
	}

	private Fireworks apply(Fireworks fireworks) {
		return new Fireworks(
			(Integer)this.flightDuration.orElseGet(fireworks::flightDuration),
			(List<FireworkExplosion>)this.explosions.map(standAlone -> standAlone.apply(fireworks.explosions())).orElse(fireworks.explosions())
		);
	}

	@Override
	public LootItemFunctionType<SetFireworksFunction> getType() {
		return LootItemFunctions.SET_FIREWORKS;
	}
}
