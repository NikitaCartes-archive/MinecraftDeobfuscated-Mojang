package net.minecraft.server.commands;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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
import org.slf4j.Logger;

public class LocateCommand {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final DynamicCommandExceptionType ERROR_STRUCTURE_NOT_FOUND = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.locate.structure.not_found", object)
	);
	private static final DynamicCommandExceptionType ERROR_STRUCTURE_INVALID = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.locate.structure.invalid", object)
	);
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.locate.biome.not_found", object)
	);
	private static final DynamicCommandExceptionType ERROR_POI_NOT_FOUND = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.locate.poi.not_found", object)
	);
	private static final int MAX_STRUCTURE_SEARCH_RADIUS = 100;
	private static final int MAX_BIOME_SEARCH_RADIUS = 6400;
	private static final int BIOME_SAMPLE_RESOLUTION_HORIZONTAL = 32;
	private static final int BIOME_SAMPLE_RESOLUTION_VERTICAL = 64;
	private static final int POI_SEARCH_RADIUS = 256;

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("locate")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("structure")
						.then(
							Commands.argument("structure", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.STRUCTURE))
								.executes(
									commandContext -> locateStructure(
											commandContext.getSource(), ResourceOrTagKeyArgument.getResourceOrTagKey(commandContext, "structure", Registries.STRUCTURE, ERROR_STRUCTURE_INVALID)
										)
								)
						)
				)
				.then(
					Commands.literal("biome")
						.then(
							Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(commandBuildContext, Registries.BIOME))
								.executes(commandContext -> locateBiome(commandContext.getSource(), ResourceOrTagArgument.getResourceOrTag(commandContext, "biome", Registries.BIOME)))
						)
				)
				.then(
					Commands.literal("poi")
						.then(
							Commands.argument("poi", ResourceOrTagArgument.resourceOrTag(commandBuildContext, Registries.POINT_OF_INTEREST_TYPE))
								.executes(
									commandContext -> locatePoi(
											commandContext.getSource(), ResourceOrTagArgument.getResourceOrTag(commandContext, "poi", Registries.POINT_OF_INTEREST_TYPE)
										)
								)
						)
				)
		);
	}

	private static Optional<? extends HolderSet.ListBacked<Structure>> getHolders(ResourceOrTagKeyArgument.Result<Structure> result, Registry<Structure> registry) {
		return result.unwrap().map(resourceKey -> registry.get(resourceKey).map(holder -> HolderSet.direct(holder)), registry::get);
	}

	private static int locateStructure(CommandSourceStack commandSourceStack, ResourceOrTagKeyArgument.Result<Structure> result) throws CommandSyntaxException {
		Registry<Structure> registry = commandSourceStack.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);
		HolderSet<Structure> holderSet = (HolderSet<Structure>)getHolders(result, registry).orElseThrow(() -> ERROR_STRUCTURE_INVALID.create(result.asPrintable()));
		BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
		Pair<BlockPos, Holder<Structure>> pair = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel, holderSet, blockPos, 100, false);
		stopwatch.stop();
		if (pair == null) {
			throw ERROR_STRUCTURE_NOT_FOUND.create(result.asPrintable());
		} else {
			return showLocateResult(commandSourceStack, result, blockPos, pair, "commands.locate.structure.success", false, stopwatch.elapsed());
		}
	}

	private static int locateBiome(CommandSourceStack commandSourceStack, ResourceOrTagArgument.Result<Biome> result) throws CommandSyntaxException {
		BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
		Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
		Pair<BlockPos, Holder<Biome>> pair = commandSourceStack.getLevel().findClosestBiome3d(result, blockPos, 6400, 32, 64);
		stopwatch.stop();
		if (pair == null) {
			throw ERROR_BIOME_NOT_FOUND.create(result.asPrintable());
		} else {
			return showLocateResult(commandSourceStack, result, blockPos, pair, "commands.locate.biome.success", true, stopwatch.elapsed());
		}
	}

	private static int locatePoi(CommandSourceStack commandSourceStack, ResourceOrTagArgument.Result<PoiType> result) throws CommandSyntaxException {
		BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
		Optional<Pair<Holder<PoiType>, BlockPos>> optional = serverLevel.getPoiManager().findClosestWithType(result, blockPos, 256, PoiManager.Occupancy.ANY);
		stopwatch.stop();
		if (optional.isEmpty()) {
			throw ERROR_POI_NOT_FOUND.create(result.asPrintable());
		} else {
			return showLocateResult(commandSourceStack, result, blockPos, ((Pair)optional.get()).swap(), "commands.locate.poi.success", false, stopwatch.elapsed());
		}
	}

	public static int showLocateResult(
		CommandSourceStack commandSourceStack,
		ResourceOrTagArgument.Result<?> result,
		BlockPos blockPos,
		Pair<BlockPos, ? extends Holder<?>> pair,
		String string,
		boolean bl,
		Duration duration
	) {
		String string2 = result.unwrap().map(reference -> result.asPrintable(), named -> result.asPrintable() + " (" + pair.getSecond().getRegisteredName() + ")");
		return showLocateResult(commandSourceStack, blockPos, pair, string, bl, string2, duration);
	}

	public static int showLocateResult(
		CommandSourceStack commandSourceStack,
		ResourceOrTagKeyArgument.Result<?> result,
		BlockPos blockPos,
		Pair<BlockPos, ? extends Holder<?>> pair,
		String string,
		boolean bl,
		Duration duration
	) {
		String string2 = result.unwrap()
			.map(resourceKey -> resourceKey.location().toString(), tagKey -> "#" + tagKey.location() + " (" + pair.getSecond().getRegisteredName() + ")");
		return showLocateResult(commandSourceStack, blockPos, pair, string, bl, string2, duration);
	}

	private static int showLocateResult(
		CommandSourceStack commandSourceStack,
		BlockPos blockPos,
		Pair<BlockPos, ? extends Holder<?>> pair,
		String string,
		boolean bl,
		String string2,
		Duration duration
	) {
		BlockPos blockPos2 = pair.getFirst();
		int i = bl ? Mth.floor(Mth.sqrt((float)blockPos.distSqr(blockPos2))) : Mth.floor(dist(blockPos.getX(), blockPos.getZ(), blockPos2.getX(), blockPos2.getZ()));
		String string3 = bl ? String.valueOf(blockPos2.getY()) : "~";
		Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", blockPos2.getX(), string3, blockPos2.getZ()))
			.withStyle(
				style -> style.withColor(ChatFormatting.GREEN)
						.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos2.getX() + " " + string3 + " " + blockPos2.getZ()))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
			);
		commandSourceStack.sendSuccess(() -> Component.translatable(string, string2, component, i), false);
		LOGGER.info("Locating element " + string2 + " took " + duration.toMillis() + " ms");
		return i;
	}

	private static float dist(int i, int j, int k, int l) {
		int m = k - i;
		int n = l - j;
		return Mth.sqrt((float)(m * m + n * n));
	}
}
