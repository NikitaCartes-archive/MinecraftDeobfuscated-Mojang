package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.Level;

public class SpawnArmorTrimsCommand {
	private static final List<ResourceKey<TrimPattern>> VANILLA_TRIM_PATTERNS = List.of(
		TrimPatterns.SENTRY,
		TrimPatterns.DUNE,
		TrimPatterns.COAST,
		TrimPatterns.WILD,
		TrimPatterns.WARD,
		TrimPatterns.EYE,
		TrimPatterns.VEX,
		TrimPatterns.TIDE,
		TrimPatterns.SNOUT,
		TrimPatterns.RIB,
		TrimPatterns.SPIRE,
		TrimPatterns.WAYFINDER,
		TrimPatterns.SHAPER,
		TrimPatterns.SILENCE,
		TrimPatterns.RAISER,
		TrimPatterns.HOST,
		TrimPatterns.FLOW,
		TrimPatterns.BOLT
	);
	private static final List<ResourceKey<TrimMaterial>> VANILLA_TRIM_MATERIALS = List.of(
		TrimMaterials.QUARTZ,
		TrimMaterials.IRON,
		TrimMaterials.NETHERITE,
		TrimMaterials.REDSTONE,
		TrimMaterials.COPPER,
		TrimMaterials.GOLD,
		TrimMaterials.EMERALD,
		TrimMaterials.DIAMOND,
		TrimMaterials.LAPIS,
		TrimMaterials.AMETHYST
	);
	private static final ToIntFunction<ResourceKey<TrimPattern>> TRIM_PATTERN_ORDER = Util.createIndexLookup(VANILLA_TRIM_PATTERNS);
	private static final ToIntFunction<ResourceKey<TrimMaterial>> TRIM_MATERIAL_ORDER = Util.createIndexLookup(VANILLA_TRIM_MATERIALS);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("spawn_armor_trims")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.executes(commandContext -> spawnArmorTrims(commandContext.getSource(), commandContext.getSource().getPlayerOrException()))
		);
	}

	private static int spawnArmorTrims(CommandSourceStack commandSourceStack, Player player) {
		Level level = player.level();
		NonNullList<ArmorTrim> nonNullList = NonNullList.create();
		Registry<TrimPattern> registry = level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);
		Registry<TrimMaterial> registry2 = level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL);
		HolderLookup<Item> holderLookup = level.holderLookup(Registries.ITEM);
		Map<ResourceLocation, List<Item>> map = (Map<ResourceLocation, List<Item>>)holderLookup.listElements().map(Holder.Reference::value).filter(itemx -> {
			Equippable equippablex = itemx.components().get(DataComponents.EQUIPPABLE);
			return equippablex != null && equippablex.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR && equippablex.model().isPresent();
		}).collect(Collectors.groupingBy(itemx -> (ResourceLocation)itemx.components().get(DataComponents.EQUIPPABLE).model().get()));
		registry.stream()
			.sorted(Comparator.comparing(trimPattern -> TRIM_PATTERN_ORDER.applyAsInt((ResourceKey)registry.getResourceKey(trimPattern).orElse(null))))
			.forEachOrdered(
				trimPattern -> registry2.stream()
						.sorted(Comparator.comparing(trimMaterial -> TRIM_MATERIAL_ORDER.applyAsInt((ResourceKey)registry2.getResourceKey(trimMaterial).orElse(null))))
						.forEachOrdered(trimMaterial -> nonNullList.add(new ArmorTrim(registry2.wrapAsHolder(trimMaterial), registry.wrapAsHolder(trimPattern))))
			);
		BlockPos blockPos = player.blockPosition().relative(player.getDirection(), 5);
		int i = map.size() - 1;
		double d = 3.0;
		int j = 0;
		int k = 0;

		for (ArmorTrim armorTrim : nonNullList) {
			for (List<Item> list : map.values()) {
				double e = (double)blockPos.getX() + 0.5 - (double)(j % registry2.size()) * 3.0;
				double f = (double)blockPos.getY() + 0.5 + (double)(k % i) * 3.0;
				double g = (double)blockPos.getZ() + 0.5 + (double)(j / registry2.size() * 10);
				ArmorStand armorStand = new ArmorStand(level, e, f, g);
				armorStand.setYRot(180.0F);
				armorStand.setNoGravity(true);

				for (Item item : list) {
					Equippable equippable = (Equippable)Objects.requireNonNull(item.components().get(DataComponents.EQUIPPABLE));
					ItemStack itemStack = new ItemStack(item);
					itemStack.set(DataComponents.TRIM, armorTrim);
					armorStand.setItemSlot(equippable.slot(), itemStack);
					if (itemStack.is(Items.TURTLE_HELMET)) {
						armorStand.setCustomName(
							armorTrim.pattern().value().copyWithStyle(armorTrim.material()).copy().append(" ").append(armorTrim.material().value().description())
						);
						armorStand.setCustomNameVisible(true);
					} else {
						armorStand.setInvisible(true);
					}
				}

				level.addFreshEntity(armorStand);
				k++;
			}

			j++;
		}

		commandSourceStack.sendSuccess(() -> Component.literal("Armorstands with trimmed armor spawned around you"), true);
		return 1;
	}
}
