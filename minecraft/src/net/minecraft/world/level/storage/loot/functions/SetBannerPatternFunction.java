package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction extends LootItemConditionalFunction {
	private static final Codec<Pair<Holder<BannerPattern>, DyeColor>> PATTERN_CODEC = Codec.<Holder<BannerPattern>, DyeColor>mapPair(
			BuiltInRegistries.BANNER_PATTERN.holderByNameCodec().fieldOf("pattern"), DyeColor.CODEC.fieldOf("color")
		)
		.codec();
	public static final Codec<SetBannerPatternFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.<List<Pair<Holder<BannerPattern>, DyeColor>>, boolean>and(
					instance.group(
						PATTERN_CODEC.listOf().fieldOf("patterns").forGetter(setBannerPatternFunction -> setBannerPatternFunction.patterns),
						Codec.BOOL.fieldOf("append").forGetter(setBannerPatternFunction -> setBannerPatternFunction.append)
					)
				)
				.apply(instance, SetBannerPatternFunction::new)
	);
	private final List<Pair<Holder<BannerPattern>, DyeColor>> patterns;
	private final boolean append;

	SetBannerPatternFunction(List<LootItemCondition> list, List<Pair<Holder<BannerPattern>, DyeColor>> list2, boolean bl) {
		super(list);
		this.patterns = list2;
		this.append = bl;
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
		if (compoundTag == null) {
			compoundTag = new CompoundTag();
		}

		BannerPattern.Builder builder = new BannerPattern.Builder();
		this.patterns.forEach(builder::addPattern);
		ListTag listTag = builder.toListTag();
		ListTag listTag2;
		if (this.append) {
			listTag2 = compoundTag.getList("Patterns", 10).copy();
			listTag2.addAll(listTag);
		} else {
			listTag2 = listTag;
		}

		compoundTag.put("Patterns", listTag2);
		BlockItem.setBlockEntityData(itemStack, BlockEntityType.BANNER, compoundTag);
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
		private final ImmutableList.Builder<Pair<Holder<BannerPattern>, DyeColor>> patterns = ImmutableList.builder();
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

		public SetBannerPatternFunction.Builder addPattern(ResourceKey<BannerPattern> resourceKey, DyeColor dyeColor) {
			return this.addPattern(BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(resourceKey), dyeColor);
		}

		public SetBannerPatternFunction.Builder addPattern(Holder<BannerPattern> holder, DyeColor dyeColor) {
			this.patterns.add(Pair.of(holder, dyeColor));
			return this;
		}
	}
}
