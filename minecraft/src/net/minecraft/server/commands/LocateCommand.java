package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class LocateCommand {
	private static final DynamicCommandExceptionType ERROR_FAILED = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.locate.failed", object)
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("locate")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("structure", ResourceLocationArgument.id())
						.suggests(SuggestionProviders.AVAILABLE_STRUCTURES)
						.executes(commandContext -> locate(commandContext.getSource(), ResourceLocationArgument.getStructureFeature(commandContext, "structure")))
				)
		);
	}

	private static int locate(CommandSourceStack commandSourceStack, ResourceLocationArgument.LocatedResource<StructureFeature<?>> locatedResource) throws CommandSyntaxException {
		StructureFeature<?> structureFeature = locatedResource.resource();
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		BlockPos blockPos2 = commandSourceStack.getLevel().findNearestMapFeature(structureFeature, blockPos, 100, false);
		ResourceLocation resourceLocation = locatedResource.id();
		if (blockPos2 == null) {
			throw ERROR_FAILED.create(resourceLocation);
		} else {
			return showLocateResult(commandSourceStack, resourceLocation.toString(), blockPos, blockPos2, "commands.locate.success");
		}
	}

	public static int showLocateResult(CommandSourceStack commandSourceStack, String string, BlockPos blockPos, BlockPos blockPos2, String string2) {
		int i = Mth.floor(dist(blockPos.getX(), blockPos.getZ(), blockPos2.getX(), blockPos2.getZ()));
		Component component = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", blockPos2.getX(), "~", blockPos2.getZ()))
			.withStyle(
				style -> style.withColor(ChatFormatting.GREEN)
						.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos2.getX() + " ~ " + blockPos2.getZ()))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip")))
			);
		commandSourceStack.sendSuccess(new TranslatableComponent(string2, string, component, i), false);
		return i;
	}

	private static float dist(int i, int j, int k, int l) {
		int m = k - i;
		int n = l - j;
		return Mth.sqrt((float)(m * m + n * n));
	}
}
