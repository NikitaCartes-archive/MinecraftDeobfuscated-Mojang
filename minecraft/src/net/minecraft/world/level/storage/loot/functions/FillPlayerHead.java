package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FillPlayerHead extends LootItemConditionalFunction {
	public static final MapCodec<FillPlayerHead> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.and(LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(fillPlayerHead -> fillPlayerHead.entityTarget))
				.apply(instance, FillPlayerHead::new)
	);
	private final LootContext.EntityTarget entityTarget;

	public FillPlayerHead(List<LootItemCondition> list, LootContext.EntityTarget entityTarget) {
		super(list);
		this.entityTarget = entityTarget;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.FILL_PLAYER_HEAD;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(this.entityTarget.getParam());
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.is(Items.PLAYER_HEAD)) {
			Object var4 = lootContext.getParamOrNull(this.entityTarget.getParam());
			if (var4 instanceof Player player) {
				itemStack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
			}
		}

		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> fillPlayerHead(LootContext.EntityTarget entityTarget) {
		return simpleBuilder(list -> new FillPlayerHead(list, entityTarget));
	}
}
