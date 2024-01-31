package net.minecraft.world.level.block.entity.vault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public record VaultConfig(
	ResourceLocation lootTable,
	double activationRange,
	double deactivationRange,
	ItemStack keyItem,
	Optional<ResourceLocation> overrideLootTableToDisplay,
	PlayerDetector playerDetector,
	PlayerDetector.EntitySelector entitySelector
) {
	static final String TAG_NAME = "config";
	static VaultConfig DEFAULT = new VaultConfig();
	static Codec<VaultConfig> CODEC = ExtraCodecs.validate(
		RecordCodecBuilder.create(
			instance -> instance.group(
						ResourceLocation.CODEC.optionalFieldOf("loot_table", DEFAULT.lootTable()).forGetter(VaultConfig::lootTable),
						Codec.DOUBLE.optionalFieldOf("activation_range", Double.valueOf(DEFAULT.activationRange())).forGetter(VaultConfig::activationRange),
						Codec.DOUBLE.optionalFieldOf("deactivation_range", Double.valueOf(DEFAULT.deactivationRange())).forGetter(VaultConfig::deactivationRange),
						ItemStack.CODEC.optionalFieldOf("key_item", DEFAULT.keyItem()).forGetter(VaultConfig::keyItem),
						ResourceLocation.CODEC.optionalFieldOf("override_loot_table_to_display").forGetter(VaultConfig::overrideLootTableToDisplay)
					)
					.apply(instance, VaultConfig::new)
		),
		VaultConfig::validate
	);

	private VaultConfig() {
		this(
			BuiltInLootTables.TRIAL_CHAMBERS_REWARD,
			4.0,
			4.5,
			new ItemStack(Items.TRIAL_KEY),
			Optional.empty(),
			PlayerDetector.INCLUDING_CREATIVE_PLAYERS,
			PlayerDetector.EntitySelector.SELECT_FROM_LEVEL
		);
	}

	public VaultConfig(ResourceLocation resourceLocation, double d, double e, ItemStack itemStack, Optional<ResourceLocation> optional) {
		this(resourceLocation, d, e, itemStack, optional, DEFAULT.playerDetector(), DEFAULT.entitySelector());
	}

	private DataResult<VaultConfig> validate() {
		return this.activationRange > this.deactivationRange
			? DataResult.error(() -> "Activation range must (" + this.activationRange + ") be less or equal to deactivation range (" + this.deactivationRange + ")")
			: DataResult.success(this);
	}
}
