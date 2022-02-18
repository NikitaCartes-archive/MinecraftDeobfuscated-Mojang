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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

public class LocateCommand {
	private static final DynamicCommandExceptionType ERROR_FAILED = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.locate.failed", object)
	);
	private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.locate.invalid", object)
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("locate")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("structure", ResourceOrTagLocationArgument.resourceOrTag(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY))
						.executes(commandContext -> locate(commandContext.getSource(), ResourceOrTagLocationArgument.getStructureFeature(commandContext, "structure")))
				)
		);
	}

	private static int locate(CommandSourceStack commandSourceStack, ResourceOrTagLocationArgument.Result<ConfiguredStructureFeature<?, ?>> result) throws CommandSyntaxException {
		Registry<ConfiguredStructureFeature<?, ?>> registry = commandSourceStack.getLevel()
			.registryAccess()
			.registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
		HolderSet<ConfiguredStructureFeature<?, ?>> holderSet = (HolderSet<ConfiguredStructureFeature<?, ?>>)result.unwrap()
			.<Optional>map(resourceKey -> registry.getHolder(resourceKey).map(holder -> HolderSet.direct(holder)), registry::getTag)
			.orElseThrow(() -> ERROR_INVALID.create(result.asPrintable()));
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pair = serverLevel.getChunkSource()
			.getGenerator()
			.findNearestMapFeature(serverLevel, holderSet, blockPos, 100, false);
		if (pair == null) {
			throw ERROR_FAILED.create(result.asPrintable());
		} else {
			return showLocateResult(commandSourceStack, result, blockPos, pair, "commands.locate.success");
		}
	}

	public static int showLocateResult(
		CommandSourceStack commandSourceStack,
		ResourceOrTagLocationArgument.Result<?> result,
		BlockPos blockPos,
		Pair<BlockPos, ? extends Holder<?>> pair,
		String string
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
		int i = Mth.floor(dist(blockPos.getX(), blockPos.getZ(), blockPos2.getX(), blockPos2.getZ()));
		Component component = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", blockPos2.getX(), "~", blockPos2.getZ()))
			.withStyle(
				style -> style.withColor(ChatFormatting.GREEN)
						.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos2.getX() + " ~ " + blockPos2.getZ()))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip")))
			);
		commandSourceStack.sendSuccess(new TranslatableComponent(string, string2, component, i), false);
		return i;
	}

	private static float dist(int i, int j, int k, int l) {
		int m = k - i;
		int n = l - j;
		return Mth.sqrt((float)(m * m + n * n));
	}
}
