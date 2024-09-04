package net.minecraft.world.item.equipment.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.equipment.EquipmentModel;

public record ArmorTrim(Holder<TrimMaterial> material, Holder<TrimPattern> pattern, boolean showInTooltip) implements TooltipProvider {
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

	public ArmorTrim(Holder<TrimMaterial> holder, Holder<TrimPattern> holder2) {
		this(holder, holder2, true);
	}

	private static String getColorPaletteSuffix(Holder<TrimMaterial> holder, ResourceLocation resourceLocation) {
		String string = (String)holder.value().overrideArmorMaterials().get(resourceLocation);
		return string != null ? string : holder.value().assetName();
	}

	public boolean hasPatternAndMaterial(Holder<TrimPattern> holder, Holder<TrimMaterial> holder2) {
		return holder.equals(this.pattern) && holder2.equals(this.material);
	}

	public ResourceLocation getTexture(EquipmentModel.LayerType layerType, ResourceLocation resourceLocation) {
		ResourceLocation resourceLocation2 = this.pattern.value().assetId();
		String string = getColorPaletteSuffix(this.material, resourceLocation);
		return resourceLocation2.withPath((UnaryOperator<String>)(string2 -> "trims/entity/" + layerType.getSerializedName() + "/" + string2 + "_" + string));
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
		return new ArmorTrim(this.material, this.pattern, bl);
	}
}
