package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

public class AppendLoot implements RuleBlockEntityModifier {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Codec<AppendLoot> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(ResourceLocation.CODEC.fieldOf("loot_table").forGetter(appendLoot -> appendLoot.lootTable)).apply(instance, AppendLoot::new)
	);
	private final ResourceLocation lootTable;

	public AppendLoot(ResourceLocation resourceLocation) {
		this.lootTable = resourceLocation;
	}

	@Override
	public CompoundTag apply(RandomSource randomSource, @Nullable CompoundTag compoundTag) {
		CompoundTag compoundTag2 = compoundTag == null ? new CompoundTag() : compoundTag.copy();
		ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, this.lootTable).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag2.put("LootTable", tag));
		compoundTag2.putLong("LootTableSeed", randomSource.nextLong());
		return compoundTag2;
	}

	@Override
	public RuleBlockEntityModifierType<?> getType() {
		return RuleBlockEntityModifierType.APPEND_LOOT;
	}
}
