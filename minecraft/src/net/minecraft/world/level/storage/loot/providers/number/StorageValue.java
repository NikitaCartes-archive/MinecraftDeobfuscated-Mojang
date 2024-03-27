package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;

public record StorageValue(ResourceLocation storage, NbtPathArgument.NbtPath path) implements NumberProvider {
	public static final MapCodec<StorageValue> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("storage").forGetter(StorageValue::storage), NbtPathArgument.NbtPath.CODEC.fieldOf("path").forGetter(StorageValue::path)
				)
				.apply(instance, StorageValue::new)
	);

	@Override
	public LootNumberProviderType getType() {
		return NumberProviders.STORAGE;
	}

	private Optional<NumericTag> getNumericTag(LootContext lootContext) {
		CompoundTag compoundTag = lootContext.getLevel().getServer().getCommandStorage().get(this.storage);

		try {
			List<Tag> list = this.path.get(compoundTag);
			if (list.size() == 1 && list.get(0) instanceof NumericTag numericTag) {
				return Optional.of(numericTag);
			}
		} catch (CommandSyntaxException var6) {
		}

		return Optional.empty();
	}

	@Override
	public float getFloat(LootContext lootContext) {
		return (Float)this.getNumericTag(lootContext).map(NumericTag::getAsFloat).orElse(0.0F);
	}

	@Override
	public int getInt(LootContext lootContext) {
		return (Integer)this.getNumericTag(lootContext).map(NumericTag::getAsInt).orElse(0);
	}
}
