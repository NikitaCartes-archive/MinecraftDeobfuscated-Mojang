package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction extends LootItemConditionalFunction {
	public static final MapCodec<SetBannerPatternFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<BannerPatternLayers, boolean>and(
					instance.group(
						BannerPatternLayers.CODEC.fieldOf("patterns").forGetter(setBannerPatternFunction -> setBannerPatternFunction.patterns),
						Codec.BOOL.fieldOf("append").forGetter(setBannerPatternFunction -> setBannerPatternFunction.append)
					)
				)
				.apply(instance, SetBannerPatternFunction::new)
	);
	private final BannerPatternLayers patterns;
	private final boolean append;

	SetBannerPatternFunction(List<LootItemCondition> list, BannerPatternLayers bannerPatternLayers, boolean bl) {
		super(list);
		this.patterns = bannerPatternLayers;
		this.append = bl;
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (this.append) {
			itemStack.update(
				DataComponents.BANNER_PATTERNS,
				BannerPatternLayers.EMPTY,
				this.patterns,
				(bannerPatternLayers, bannerPatternLayers2) -> new BannerPatternLayers.Builder().addAll(bannerPatternLayers).addAll(bannerPatternLayers2).build()
			);
		} else {
			itemStack.set(DataComponents.BANNER_PATTERNS, this.patterns);
		}

		return itemStack;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_BANNER_PATTERN;
	}

	public static SetBannerPatternFunction.Builder setBannerPattern(boolean bl) {
		return new SetBannerPatternFunction.Builder(bl);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<SetBannerPatternFunction.Builder> {
		private final BannerPatternLayers.Builder patterns = new BannerPatternLayers.Builder();
		private final boolean append;

		Builder(boolean bl) {
			this.append = bl;
		}

		protected SetBannerPatternFunction.Builder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new SetBannerPatternFunction(this.getConditions(), this.patterns.build(), this.append);
		}

		public SetBannerPatternFunction.Builder addPattern(Holder<BannerPattern> holder, DyeColor dyeColor) {
			this.patterns.add(holder, dyeColor);
			return this;
		}
	}
}
