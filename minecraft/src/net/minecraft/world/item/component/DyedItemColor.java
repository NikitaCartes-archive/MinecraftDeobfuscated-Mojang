package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public record DyedItemColor(int rgb, boolean showInTooltip) implements TooltipProvider {
	private static final Codec<DyedItemColor> FULL_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("rgb").forGetter(DyedItemColor::rgb),
					Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(DyedItemColor::showInTooltip)
				)
				.apply(instance, DyedItemColor::new)
	);
	public static final Codec<DyedItemColor> CODEC = Codec.withAlternative(FULL_CODEC, Codec.INT, integer -> new DyedItemColor(integer, true));
	public static final StreamCodec<ByteBuf, DyedItemColor> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, DyedItemColor::rgb, ByteBufCodecs.BOOL, DyedItemColor::showInTooltip, DyedItemColor::new
	);
	public static final int LEATHER_COLOR = -6265536;

	public static int getOrDefault(ItemStack itemStack, int i) {
		DyedItemColor dyedItemColor = itemStack.get(DataComponents.DYED_COLOR);
		return dyedItemColor != null ? FastColor.ARGB32.opaque(dyedItemColor.rgb()) : i;
	}

	public static ItemStack applyDyes(ItemStack itemStack, List<DyeItem> list) {
		if (!itemStack.is(ItemTags.DYEABLE)) {
			return ItemStack.EMPTY;
		} else {
			ItemStack itemStack2 = itemStack.copyWithCount(1);
			int i = 0;
			int j = 0;
			int k = 0;
			int l = 0;
			int m = 0;
			DyedItemColor dyedItemColor = itemStack2.get(DataComponents.DYED_COLOR);
			if (dyedItemColor != null) {
				int n = FastColor.ARGB32.red(dyedItemColor.rgb());
				int o = FastColor.ARGB32.green(dyedItemColor.rgb());
				int p = FastColor.ARGB32.blue(dyedItemColor.rgb());
				l += Math.max(n, Math.max(o, p));
				i += n;
				j += o;
				k += p;
				m++;
			}

			for (DyeItem dyeItem : list) {
				float[] fs = dyeItem.getDyeColor().getTextureDiffuseColors();
				int q = (int)(fs[0] * 255.0F);
				int r = (int)(fs[1] * 255.0F);
				int s = (int)(fs[2] * 255.0F);
				l += Math.max(q, Math.max(r, s));
				i += q;
				j += r;
				k += s;
				m++;
			}

			int n = i / m;
			int o = j / m;
			int p = k / m;
			float f = (float)l / (float)m;
			float g = (float)Math.max(n, Math.max(o, p));
			n = (int)((float)n * f / g);
			o = (int)((float)o * f / g);
			p = (int)((float)p * f / g);
			int s = FastColor.ARGB32.color(0, n, o, p);
			boolean bl = dyedItemColor == null || dyedItemColor.showInTooltip();
			itemStack2.set(DataComponents.DYED_COLOR, new DyedItemColor(s, bl));
			return itemStack2;
		}
	}

	@Override
	public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		if (this.showInTooltip) {
			if (tooltipFlag.isAdvanced()) {
				consumer.accept(Component.translatable("item.color", String.format(Locale.ROOT, "#%06X", this.rgb)).withStyle(ChatFormatting.GRAY));
			} else {
				consumer.accept(Component.translatable("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			}
		}
	}

	public DyedItemColor withTooltip(boolean bl) {
		return new DyedItemColor(this.rgb, bl);
	}
}
