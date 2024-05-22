package net.minecraft.world.item.armortrim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public class ArmorTrim implements TooltipProvider {
	public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material),
					TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern),
					Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(armorTrim -> armorTrim.showInTooltip)
				)
				.apply(instance, ArmorTrim::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ArmorTrim> STREAM_CODEC = StreamCodec.composite(
		TrimMaterial.STREAM_CODEC,
		ArmorTrim::material,
		TrimPattern.STREAM_CODEC,
		ArmorTrim::pattern,
		ByteBufCodecs.BOOL,
		armorTrim -> armorTrim.showInTooltip,
		ArmorTrim::new
	);
	private static final Component UPGRADE_TITLE = Component.translatable(
			Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.upgrade"))
		)
		.withStyle(ChatFormatting.GRAY);
	private final Holder<TrimMaterial> material;
	private final Holder<TrimPattern> pattern;
	private final boolean showInTooltip;
	private final Function<Holder<ArmorMaterial>, ResourceLocation> innerTexture;
	private final Function<Holder<ArmorMaterial>, ResourceLocation> outerTexture;

	private ArmorTrim(
		Holder<TrimMaterial> holder,
		Holder<TrimPattern> holder2,
		boolean bl,
		Function<Holder<ArmorMaterial>, ResourceLocation> function,
		Function<Holder<ArmorMaterial>, ResourceLocation> function2
	) {
		this.material = holder;
		this.pattern = holder2;
		this.showInTooltip = bl;
		this.innerTexture = function;
		this.outerTexture = function2;
	}

	public ArmorTrim(Holder<TrimMaterial> holder, Holder<TrimPattern> holder2, boolean bl) {
		this.material = holder;
		this.pattern = holder2;
		this.innerTexture = Util.memoize((Function<Holder<ArmorMaterial>, ResourceLocation>)(holder3 -> {
			ResourceLocation resourceLocation = holder2.value().assetId();
			String string = getColorPaletteSuffix(holder, holder3);
			return resourceLocation.withPath((UnaryOperator<String>)(string2 -> "trims/models/armor/" + string2 + "_leggings_" + string));
		}));
		this.outerTexture = Util.memoize((Function<Holder<ArmorMaterial>, ResourceLocation>)(holder3 -> {
			ResourceLocation resourceLocation = holder2.value().assetId();
			String string = getColorPaletteSuffix(holder, holder3);
			return resourceLocation.withPath((UnaryOperator<String>)(string2 -> "trims/models/armor/" + string2 + "_" + string));
		}));
		this.showInTooltip = bl;
	}

	public ArmorTrim(Holder<TrimMaterial> holder, Holder<TrimPattern> holder2) {
		this(holder, holder2, true);
	}

	private static String getColorPaletteSuffix(Holder<TrimMaterial> holder, Holder<ArmorMaterial> holder2) {
		Map<Holder<ArmorMaterial>, String> map = holder.value().overrideArmorMaterials();
		String string = (String)map.get(holder2);
		return string != null ? string : holder.value().assetName();
	}

	public boolean hasPatternAndMaterial(Holder<TrimPattern> holder, Holder<TrimMaterial> holder2) {
		return holder.equals(this.pattern) && holder2.equals(this.material);
	}

	public Holder<TrimPattern> pattern() {
		return this.pattern;
	}

	public Holder<TrimMaterial> material() {
		return this.material;
	}

	public ResourceLocation innerTexture(Holder<ArmorMaterial> holder) {
		return (ResourceLocation)this.innerTexture.apply(holder);
	}

	public ResourceLocation outerTexture(Holder<ArmorMaterial> holder) {
		return (ResourceLocation)this.outerTexture.apply(holder);
	}

	public boolean equals(Object object) {
		return !(object instanceof ArmorTrim armorTrim)
			? false
			: this.showInTooltip == armorTrim.showInTooltip && this.pattern.equals(armorTrim.pattern) && this.material.equals(armorTrim.material);
	}

	public int hashCode() {
		int i = this.material.hashCode();
		i = 31 * i + this.pattern.hashCode();
		return 31 * i + (this.showInTooltip ? 1 : 0);
	}

	@Override
	public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		if (this.showInTooltip) {
			consumer.accept(UPGRADE_TITLE);
			consumer.accept(CommonComponents.space().append(this.pattern.value().copyWithStyle(this.material)));
			consumer.accept(CommonComponents.space().append(this.material.value().description()));
		}
	}

	public ArmorTrim withTooltip(boolean bl) {
		return new ArmorTrim(this.material, this.pattern, bl, this.innerTexture, this.outerTexture);
	}
}
