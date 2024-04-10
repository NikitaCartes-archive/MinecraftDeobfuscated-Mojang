package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.slf4j.Logger;

public class SetItemDamageFunction extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<SetItemDamageFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<NumberProvider, boolean>and(
					instance.group(
						NumberProviders.CODEC.fieldOf("damage").forGetter(setItemDamageFunction -> setItemDamageFunction.damage),
						Codec.BOOL.fieldOf("add").orElse(false).forGetter(setItemDamageFunction -> setItemDamageFunction.add)
					)
				)
				.apply(instance, SetItemDamageFunction::new)
	);
	private final NumberProvider damage;
	private final boolean add;

	private SetItemDamageFunction(List<LootItemCondition> list, NumberProvider numberProvider, boolean bl) {
		super(list);
		this.damage = numberProvider;
		this.add = bl;
	}

	@Override
	public LootItemFunctionType<SetItemDamageFunction> getType() {
		return LootItemFunctions.SET_DAMAGE;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.damage.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.isDamageableItem()) {
			int i = itemStack.getMaxDamage();
			float f = this.add ? 1.0F - (float)itemStack.getDamageValue() / (float)i : 0.0F;
			float g = 1.0F - Mth.clamp(this.damage.getFloat(lootContext) + f, 0.0F, 1.0F);
			itemStack.setDamageValue(Mth.floor(g * (float)i));
		} else {
			LOGGER.warn("Couldn't set damage of loot item {}", itemStack);
		}

		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider numberProvider) {
		return simpleBuilder(list -> new SetItemDamageFunction(list, numberProvider, false));
	}

	public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider numberProvider, boolean bl) {
		return simpleBuilder(list -> new SetItemDamageFunction(list, numberProvider, bl));
	}
}
