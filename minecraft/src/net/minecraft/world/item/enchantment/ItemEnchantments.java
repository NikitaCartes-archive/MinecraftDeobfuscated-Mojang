package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public class ItemEnchantments implements TooltipProvider {
	public static final ItemEnchantments EMPTY = new ItemEnchantments(new Object2IntLinkedOpenHashMap<>(), true);
	public static final int MAX_LEVEL = 255;
	private static final Codec<Integer> LEVEL_CODEC = Codec.intRange(0, 255);
	private static final Codec<Object2IntLinkedOpenHashMap<Holder<Enchantment>>> LEVELS_CODEC = Codec.unboundedMap(
			BuiltInRegistries.ENCHANTMENT.holderByNameCodec(), LEVEL_CODEC
		)
		.xmap(Object2IntLinkedOpenHashMap::new, Function.identity());
	private static final Codec<ItemEnchantments> FULL_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					LEVELS_CODEC.fieldOf("levels").forGetter(itemEnchantments -> itemEnchantments.enchantments),
					Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(itemEnchantments -> itemEnchantments.showInTooltip)
				)
				.apply(instance, ItemEnchantments::new)
	);
	public static final Codec<ItemEnchantments> CODEC = Codec.withAlternative(
		FULL_CODEC, LEVELS_CODEC, object2IntLinkedOpenHashMap -> new ItemEnchantments(object2IntLinkedOpenHashMap, true)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemEnchantments> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.map(Object2IntLinkedOpenHashMap::new, ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT), ByteBufCodecs.VAR_INT),
		itemEnchantments -> itemEnchantments.enchantments,
		ByteBufCodecs.BOOL,
		itemEnchantments -> itemEnchantments.showInTooltip,
		ItemEnchantments::new
	);
	final Object2IntLinkedOpenHashMap<Holder<Enchantment>> enchantments;
	final boolean showInTooltip;

	ItemEnchantments(Object2IntLinkedOpenHashMap<Holder<Enchantment>> object2IntLinkedOpenHashMap, boolean bl) {
		this.enchantments = object2IntLinkedOpenHashMap;
		this.showInTooltip = bl;
	}

	public int getLevel(Enchantment enchantment) {
		return this.enchantments.getInt(enchantment.builtInRegistryHolder());
	}

	@Override
	public void addToTooltip(Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		if (this.showInTooltip) {
			for(Entry<Holder<Enchantment>> entry : this.enchantments.object2IntEntrySet()) {
				consumer.accept(((Enchantment)((Holder)entry.getKey()).value()).getFullname(entry.getIntValue()));
			}
		}
	}

	public ItemEnchantments withTooltip(boolean bl) {
		return new ItemEnchantments(this.enchantments, bl);
	}

	public Set<Holder<Enchantment>> keySet() {
		return Collections.unmodifiableSet(this.enchantments.keySet());
	}

	public Set<Entry<Holder<Enchantment>>> entrySet() {
		return Collections.unmodifiableSet(this.enchantments.object2IntEntrySet());
	}

	public int size() {
		return this.enchantments.size();
	}

	public boolean isEmpty() {
		return this.enchantments.isEmpty();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof ItemEnchantments)) {
			return false;
		} else {
			ItemEnchantments itemEnchantments = (ItemEnchantments)object;
			return this.showInTooltip == itemEnchantments.showInTooltip && this.enchantments.equals(itemEnchantments.enchantments);
		}
	}

	public int hashCode() {
		int i = this.enchantments.hashCode();
		return 31 * i + (this.showInTooltip ? 1 : 0);
	}

	public String toString() {
		return "ItemEnchantments{enchantments=" + this.enchantments + ", showInTooltip=" + this.showInTooltip + "}";
	}

	public static class Mutable {
		private final Object2IntLinkedOpenHashMap<Holder<Enchantment>> enchantments = new Object2IntLinkedOpenHashMap<>();
		private final boolean showInTooltip;

		public Mutable(ItemEnchantments itemEnchantments) {
			this.enchantments.putAll(itemEnchantments.enchantments);
			this.showInTooltip = itemEnchantments.showInTooltip;
		}

		public void set(Enchantment enchantment, int i) {
			if (i <= 0) {
				this.enchantments.removeInt(enchantment.builtInRegistryHolder());
			} else {
				this.enchantments.put(enchantment.builtInRegistryHolder(), i);
			}
		}

		public void upgrade(Enchantment enchantment, int i) {
			if (i > 0) {
				this.enchantments.merge(enchantment.builtInRegistryHolder(), i, Integer::max);
			}
		}

		public void removeIf(Predicate<Holder<Enchantment>> predicate) {
			this.enchantments.keySet().removeIf(predicate);
		}

		public int getLevel(Enchantment enchantment) {
			return this.enchantments.getOrDefault(enchantment.builtInRegistryHolder(), 0);
		}

		public Set<Holder<Enchantment>> keySet() {
			return this.enchantments.keySet();
		}

		public ItemEnchantments toImmutable() {
			return new ItemEnchantments(this.enchantments, this.showInTooltip);
		}
	}
}
