package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public class LocateCommand {
	private static final DynamicCommandExceptionType ERROR_STRUCTURE_NOT_FOUND = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.locate.structure.not_found", object)
	);
	private static final DynamicCommandExceptionType ERROR_STRUCTURE_INVALID = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.locate.structure.invalid", object)
	);
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.locate.biome.not_found", object)
	);
	private static final DynamicCommandExceptionType ERROR_BIOME_INVALID = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.locate.biome.invalid", object)
	);
	private static final DynamicCommandExceptionType ERROR_POI_NOT_FOUND = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.locate.poi.not_found", object)
	);
	private static final DynamicCommandExceptionType ERROR_POI_INVALID = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.locate.poi.invalid", object)
	);
	private static final int MAX_STRUCTURE_SEARCH_RADIUS = 100;
	private static final int MAX_BIOME_SEARCH_RADIUS = 6400;
	private static final int BIOME_SAMPLE_RESOLUTION_HORIZONTAL = 32;
	private static final int BIOME_SAMPLE_RESOLUTION_VERTICAL = 64;
	private static final int POI_SEARCH_RADIUS = 256;

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("locate")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("structure")
						.then(
							Commands.argument("structure", ResourceOrTagLocationArgument.resourceOrTag(Registry.STRUCTURE_REGISTRY))
								.executes(
									commandContext -> locateStructure(
											commandContext.getSource(),
											ResourceOrTagLocationArgument.getRegistryType(commandContext, "structure", Registry.STRUCTURE_REGISTRY, ERROR_STRUCTURE_INVALID)
										)
								)
						)
				)
				.then(
					Commands.literal("biome")
						.then(
							Commands.argument("biome", ResourceOrTagLocationArgument.resourceOrTag(Registry.BIOME_REGISTRY))
								.executes(
									commandContext -> locateBiome(
											commandContext.getSource(), ResourceOrTagLocationArgument.getRegistryType(commandContext, "biome", Registry.BIOME_REGISTRY, ERROR_BIOME_INVALID)
										)
								)
						)
				)
				.then(
					Commands.literal("poi")
						.then(
							Commands.argument("poi", ResourceOrTagLocationArgument.resourceOrTag(Registry.POINT_OF_INTEREST_TYPE_REGISTRY))
								.executes(
									commandContext -> locatePoi(
											commandContext.getSource(),
											ResourceOrTagLocationArgument.getRegistryType(commandContext, "poi", Registry.POINT_OF_INTEREST_TYPE_REGISTRY, ERROR_POI_INVALID)
										)
								)
						)
				)
		);
	}

	private static Optional<? extends HolderSet.ListBacked<Structure>> getHolders(
		ResourceOrTagLocationArgument.Result<Structure> result, Registry<Structure> registry
	) {
		return result.unwrap().map(resourceKey -> registry.getHolder(resourceKey).map(holder -> HolderSet.direct(holder)), registry::getTag);
	}

	private static int locateStructure(CommandSourceStack commandSourceStack, ResourceOrTagLocationArgument.Result<Structure> result) throws CommandSyntaxException {
		Registry<Structure> registry = commandSourceStack.getLevel().registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		HolderSet<Structure> holderSet = (HolderSet<Structure>)getHolders(result, registry).orElseThrow(() -> ERROR_STRUCTURE_INVALID.create(result.asPrintable()));
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Pair<BlockPos, Holder<Structure>> pair = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel, holderSet, blockPos, 100, false);
		if (pair == null) {
			throw ERROR_STRUCTURE_NOT_FOUND.create(result.asPrintable());
		} else {
			return showLocateResult(commandSourceStack, result, blockPos, pair, "commands.locate.structure.success", false);
		}
	}

	private static int locateBiome(CommandSourceStack commandSourceStack, ResourceOrTagLocationArgument.Result<Biome> result) throws CommandSyntaxException {
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		Pair<BlockPos, Holder<Biome>> pair = commandSourceStack.getLevel().findClosestBiome3d(result, blockPos, 6400, 32, 64);
		if (pair == null) {
			throw ERROR_BIOME_NOT_FOUND.create(result.asPrintable());
		} else {
			return showLocateResult(commandSourceStack, result, blockPos, pair, "commands.locate.biome.success", true);
		}
	}

	private static int locatePoi(CommandSourceStack commandSourceStack, ResourceOrTagLocationArgument.Result<PoiType> result) throws CommandSyntaxException {
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Optional<Pair<Holder<PoiType>, BlockPos>> optional = serverLevel.getPoiManager().findClosestWithType(result, blockPos, 256, PoiManager.Occupancy.ANY);
		if (optional.isEmpty()) {
			throw ERROR_POI_NOT_FOUND.create(result.asPrintable());
		} else {
			return showLocateResult(commandSourceStack, result, blockPos, ((Pair)optional.get()).swap(), "commands.locate.poi.success", false);
		}
	}

	public static int showLocateResult(
		CommandSourceStack commandSourceStack,
		ResourceOrTagLocationArgument.Result<?> result,
		BlockPos blockPos,
		Pair<BlockPos, ? extends Holder<?>> pair,
		String string,
		boolean bl
	) {
		BlockPos blockPos2 = pair.getFirst();
		String string2 = result.unwrap()
			.map(
				resourceKey -> resourceKey.location().toString(),
				tagKey -> "#"
						+ tagKey.location()
						+ " ("
						+ (String)pair.getSecond().unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("[unregistered]")
						+ ")"
			);
		int i = bl ? Mth.floor(Mth.sqrt((float)blockPos.distSqr(blockPos2))) : Mth.floor(dist(blockPos.getX(), blockPos.getZ(), blockPos2.getX(), blockPos2.getZ()));
		String string3 = bl ? String.valueOf(blockPos2.getY()) : "~";
		Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", blockPos2.getX(), string3, blockPos2.getZ()))
			.withStyle(
				style -> style.withColor(ChatFormatting.GREEN)
						.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos2.getX() + " " + string3 + " " + blockPos2.getZ()))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
			);
		commandSourceStack.sendSuccess(Component.translatable(string, string2, component, i), false);
		return i;
	}

	private static float dist(int i, int j, int k, int l) {
		int m = k - i;
		int n = l - j;
		return Mth.sqrt((float)(m * m + n * n));
	}
}
