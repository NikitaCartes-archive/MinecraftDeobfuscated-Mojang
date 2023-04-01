package net.minecraft.voting;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleAction;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.voting.rules.Rules;
import net.minecraft.voting.votes.FinishedVote;
import net.minecraft.voting.votes.OptionId;
import net.minecraft.voting.votes.ServerVote;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class VoteCommand {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final SuggestionProvider<CommandSourceStack> PENDING_VOTES = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
			commandContext.getSource().getServer().getVoteStorage().getPendingVotesIds().map(UUID::toString), suggestionsBuilder
		);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("vote")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("pending")
						.then(
							Commands.literal("start")
								.executes(commandContext -> startApproveVote(commandContext.getSource(), Optional.empty()))
								.then(
									Commands.argument("rule", ResourceArgument.resource(commandBuildContext, Registries.RULE))
										.executes(
											commandContext -> startApproveVote(commandContext.getSource(), Optional.of(ResourceArgument.getResource(commandContext, "rule", Registries.RULE)))
										)
								)
						)
						.then(Commands.literal("repeal").executes(commandContext -> startRepealVote(commandContext.getSource())))
						.then(
							Commands.literal("finish")
								.then(Commands.literal("*").executes(commandContext -> finishAllVotes(commandContext.getSource(), true)))
								.then(
									Commands.argument("id", UuidArgument.uuid())
										.suggests(PENDING_VOTES)
										.executes(commandContext -> finishVote(commandContext.getSource(), UuidArgument.getUuid(commandContext, "id"), true))
								)
						)
						.then(
							Commands.literal("discard")
								.then(Commands.literal("*").executes(commandContext -> finishAllVotes(commandContext.getSource(), false)))
								.then(
									Commands.argument("id", UuidArgument.uuid())
										.suggests(PENDING_VOTES)
										.executes(commandContext -> finishVote(commandContext.getSource(), UuidArgument.getUuid(commandContext, "id"), false))
								)
						)
						.then(
							Commands.literal("vote")
								.then(
									Commands.argument("id", UuidArgument.uuid())
										.suggests(PENDING_VOTES)
										.then(
											Commands.argument("option", IntegerArgumentType.integer(0))
												.executes(
													commandContext -> addVotes(
															commandContext.getSource(), UuidArgument.getUuid(commandContext, "id"), IntegerArgumentType.getInteger(commandContext, "option"), 1
														)
												)
												.then(
													Commands.argument("count", IntegerArgumentType.integer())
														.executes(
															commandContext -> addVotes(
																	commandContext.getSource(),
																	UuidArgument.getUuid(commandContext, "id"),
																	IntegerArgumentType.getInteger(commandContext, "option"),
																	IntegerArgumentType.getInteger(commandContext, "count")
																)
														)
												)
										)
								)
						)
				)
				.then(
					Commands.literal("rule")
						.then(
							Commands.argument("rule", ResourceArgument.resource(commandBuildContext, Registries.RULE))
								.then(
									Commands.literal("approve")
										.executes(
											commandContext -> changeRule(
													commandContext.getSource(), ResourceArgument.getResource(commandContext, "rule", Registries.RULE), RuleAction.APPROVE, new CompoundTag()
												)
										)
										.then(
											Commands.literal("?")
												.executes(
													commandContext -> makeRandomChange(
															commandContext.getSource(), RuleAction.APPROVE, ResourceArgument.getResource(commandContext, "rule", Registries.RULE)
														)
												)
										)
										.then(
											Commands.argument("value", NbtTagArgument.nbtTag())
												.executes(
													commandContext -> changeRule(
															commandContext.getSource(),
															ResourceArgument.getResource(commandContext, "rule", Registries.RULE),
															RuleAction.APPROVE,
															NbtTagArgument.getNbtTag(commandContext, "value")
														)
												)
										)
								)
								.then(
									Commands.literal("repeal")
										.executes(
											commandContext -> changeRule(
													commandContext.getSource(), ResourceArgument.getResource(commandContext, "rule", Registries.RULE), RuleAction.REPEAL, new CompoundTag()
												)
										)
										.then(
											Commands.literal("?")
												.executes(
													commandContext -> makeRandomChange(
															commandContext.getSource(), RuleAction.REPEAL, ResourceArgument.getResource(commandContext, "rule", Registries.RULE)
														)
												)
										)
										.then(
											Commands.literal("*")
												.executes(commandContext -> repealAll(commandContext.getSource(), ResourceArgument.getResource(commandContext, "rule", Registries.RULE)))
										)
										.then(
											Commands.argument("value", NbtTagArgument.nbtTag())
												.executes(
													commandContext -> changeRule(
															commandContext.getSource(),
															ResourceArgument.getResource(commandContext, "rule", Registries.RULE),
															RuleAction.REPEAL,
															NbtTagArgument.getNbtTag(commandContext, "value")
														)
												)
										)
								)
						)
						.then(
							Commands.literal("?")
								.then(Commands.literal("approve").executes(commandContext -> changeRandomRule(commandContext.getSource(), RuleAction.APPROVE)))
								.then(Commands.literal("repeal").executes(commandContext -> changeRandomRule(commandContext.getSource(), RuleAction.REPEAL)))
						)
						.then(Commands.literal("*").then(Commands.literal("repeal").executes(commandContext -> repealAllRules(commandContext.getSource()))))
				)
				.then(
					Commands.literal("dump_all")
						.executes(commandContext -> dumpAll(commandContext.getSource(), false))
						.then(Commands.literal("short").executes(commandContext -> dumpAll(commandContext.getSource(), true)))
						.then(Commands.literal("long").executes(commandContext -> dumpAll(commandContext.getSource(), false)))
				)
				.then(Commands.literal("io").then(Commands.literal("flush").executes(commandContext -> {
					commandContext.getSource().getServer().flushVotes();
					LOGGER.info("ThreadedAnvilChunkStorage: Hey, how are you?");
					commandContext.getSource().sendSuccess(Component.literal("Flushed votes"), true);
					return 1;
				})).then(Commands.literal("reload").executes(commandContext -> {
					commandContext.getSource().getServer().reloadVotes();
					commandContext.getSource().sendSuccess(Component.literal("Reloaded votes"), true);
					return 1;
				})))
		);
	}

	private static Component pendingVoteToString(UUID uUID, ServerVote serverVote) {
		String string = (String)ServerVote.CODEC.encodeStart(JsonOps.INSTANCE, serverVote).get().left().map(JsonElement::toString).orElse("Error! Oh Error!");
		return Component.literal(uUID.toString())
			.withStyle(style -> style.withUnderlined(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(string))));
	}

	private static Integer maybeStartVotePlease(CommandSourceStack commandSourceStack, UUID uUID, MinecraftServer minecraftServer, Optional<ServerVote> optional) {
		return (Integer)optional.map(serverVote -> {
			minecraftServer.startVote(uUID, serverVote);
			commandSourceStack.sendSuccess(Component.literal("Started vote for ").append(pendingVoteToString(uUID, serverVote)), true);
			return 1;
		}).orElseGet(() -> {
			commandSourceStack.sendFailure(Component.literal("Failed to start vote, maybe retry?"));
			return 0;
		});
	}

	private static int startApproveVote(CommandSourceStack commandSourceStack, Optional<Holder.Reference<Rule>> optional) {
		RandomSource randomSource = commandSourceStack.getLevel().random;
		UUID uUID = UUID.randomUUID();
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		Set<Rule> set = minecraftServer.getVoteStorage().rulesWithPendingCommands();
		ServerVote.VoteGenerationOptions voteGenerationOptions = ServerVote.VoteGenerationOptions.createFromRules(randomSource);
		Optional<ServerVote> optional2 = (Optional<ServerVote>)optional.map(
				reference -> ServerVote.createRandomApproveVote(uUID, minecraftServer, voteGenerationOptions, (Rule)reference.value())
			)
			.orElseGet(() -> ServerVote.createRandomApproveVote(uUID, set, minecraftServer, voteGenerationOptions));
		return maybeStartVotePlease(commandSourceStack, uUID, minecraftServer, optional2);
	}

	private static int startRepealVote(CommandSourceStack commandSourceStack) {
		RandomSource randomSource = commandSourceStack.getLevel().random;
		UUID uUID = UUID.randomUUID();
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		Set<Rule> set = minecraftServer.getVoteStorage().rulesWithPendingCommands();
		ServerVote.VoteGenerationOptions voteGenerationOptions = ServerVote.VoteGenerationOptions.createFromRules(randomSource);
		Optional<ServerVote> optional = ServerVote.createRandomRepealVote(uUID, set, minecraftServer, voteGenerationOptions);
		return maybeStartVotePlease(commandSourceStack, uUID, minecraftServer, optional);
	}

	private static int finishAllVotes(CommandSourceStack commandSourceStack, boolean bl) {
		List<UUID> list = commandSourceStack.getServer().getVoteStorage().getPendingVotesIds().toList();
		int i = 0;

		for (UUID uUID : list) {
			i += finishVote(commandSourceStack, uUID, bl);
		}

		return i;
	}

	private static int finishVote(CommandSourceStack commandSourceStack, UUID uUID, boolean bl) {
		FinishedVote finishedVote = commandSourceStack.getServer().finishVote(uUID, bl);
		if (finishedVote != null) {
			commandSourceStack.sendSuccess(
				Component.literal(bl ? "Finished vote for " : "Rejected vote for ").append(pendingVoteToString(uUID, finishedVote.vote())), true
			);
			return 1;
		} else {
			commandSourceStack.sendFailure(Component.literal("Failed to finish vote ").append(String.valueOf(uUID)));
			return 0;
		}
	}

	private static int addVotes(CommandSourceStack commandSourceStack, UUID uUID, int i, int j) throws CommandSyntaxException {
		Entity entity = commandSourceStack.getEntityOrException();
		if (commandSourceStack.getServer().vote(new OptionId(uUID, i), entity, j)) {
			commandSourceStack.sendSuccess(Component.translatable("Added %s votes from %s to option %s of vote %s", j, entity.getDisplayName(), i, uUID), true);
			return 1;
		} else {
			commandSourceStack.sendFailure(Component.literal("Failed to add votes to ").append(String.valueOf(uUID)));
			return 0;
		}
	}

	private static int changeRandomRule(CommandSourceStack commandSourceStack, RuleAction ruleAction) {
		Holder.Reference<Rule> reference = Rules.getRandomRule(commandSourceStack.getLevel().getRandom());
		return makeRandomChange(commandSourceStack, ruleAction, reference);
	}

	private static int makeRandomChange(CommandSourceStack commandSourceStack, RuleAction ruleAction, Holder.Reference<Rule> reference) {
		return (Integer)(switch (ruleAction) {
			case APPROVE -> reference.value().randomApprovableChanges(commandSourceStack.getServer(), commandSourceStack.getLevel().getRandom(), 1);
			case REPEAL -> Util.getRandomSafe(reference.value().repealableChanges().toList(), commandSourceStack.getLevel().getRandom()).stream();
		}).findAny().map(ruleChange -> applyChange(ruleChange, ruleAction, commandSourceStack)).orElseGet(() -> {
			commandSourceStack.sendFailure(Component.literal("Failed to find applicable rule in ").append(reference.key().location().toString()));
			return 0;
		});
	}

	private static int changeRule(CommandSourceStack commandSourceStack, Holder.Reference<Rule> reference, RuleAction ruleAction, Tag tag) {
		return reference.value()
			.codec()
			.parse(new Dynamic<>(NbtOps.INSTANCE, tag))
			.get()
			.<Integer>map(ruleChange -> applyChange(ruleChange, ruleAction, commandSourceStack), partialResult -> {
				LOGGER.warn("Failed to decode {}/{}: {}", reference.key().location(), tag, partialResult.message());
				commandSourceStack.sendFailure(Component.literal("Failed to decode ").append(reference.key().location().toString()));
				return 0;
			});
	}

	private static int applyChange(RuleChange ruleChange, RuleAction ruleAction, CommandSourceStack commandSourceStack) {
		Component component = ruleChange.description(ruleAction);
		ruleChange.apply(ruleAction, commandSourceStack.getServer());
		commandSourceStack.sendSuccess(Component.literal("Applied ").append(component), true);
		return 1;
	}

	private static int repealAllRules(CommandSourceStack commandSourceStack) {
		int i = commandSourceStack.registryAccess().registryOrThrow(Registries.RULE).stream().mapToInt(rule -> rule.repealAll(false)).sum();
		commandSourceStack.sendSuccess(Component.literal("Repealed " + i + " changes from all rules"), true);
		return i;
	}

	private static int repealAll(CommandSourceStack commandSourceStack, Holder.Reference<Rule> reference) {
		int i = reference.value().repealAll(false);
		commandSourceStack.sendSuccess(Component.literal("Repealed " + i + " changes for " + reference.key().location()), true);
		return i;
	}

	private static void printRuleChange(PrintStream printStream, RuleChange ruleChange) {
		RuleChange.CODEC.encodeStart(JsonOps.INSTANCE, ruleChange).resultOrPartial(LOGGER::error).ifPresent(jsonElement -> {
			printStream.println("\t\t" + jsonElement);
			Component component = ruleChange.description(RuleAction.APPROVE);
			printStream.println("\t\t\t Approve: " + component.getString());
			Component component2 = ruleChange.description(RuleAction.REPEAL);
			printStream.println("\t\t\t Repeal: " + component2.getString());
		});
	}

	private static void longDescription(MinecraftServer minecraftServer, PrintStream printStream, RandomSource randomSource, Holder.Reference<Rule> reference) {
		printStream.println(reference.key().location());
		List<RuleChange> list = reference.value().approvedChanges().toList();
		if (!list.isEmpty()) {
			printStream.println("\tApproved:");

			for (RuleChange ruleChange : list) {
				printRuleChange(printStream, ruleChange);
			}
		}

		list = reference.value().randomApprovableChanges(minecraftServer, randomSource, 5).toList();
		if (!list.isEmpty()) {
			printStream.println("\tExample proposals:");

			for (RuleChange ruleChange : list) {
				printRuleChange(printStream, ruleChange);
			}
		}
	}

	private static int dumpAll(CommandSourceStack commandSourceStack, boolean bl) {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		Registry<Rule> registry = commandSourceStack.registryAccess().registryOrThrow(Registries.RULE);
		PrintStream printStream = Bootstrap.STDOUT;
		RandomSource randomSource = RandomSource.create();
		registry.holders()
			.sorted(Comparator.comparing(reference -> reference.key().location()))
			.forEach(
				reference -> {
					if (!bl) {
						longDescription(minecraftServer, printStream, randomSource, reference);
					} else {
						String string = (String)((Rule)reference.value())
							.randomApprovableChanges(minecraftServer, randomSource, 3)
							.map(ruleChange -> "\"" + ruleChange.description(RuleAction.APPROVE).getString() + "\"")
							.collect(Collectors.joining(", "));
						printStream.println(reference.key().location() + ": " + string);
					}
				}
			);
		commandSourceStack.sendSuccess(Component.literal("Whew! That was scary!"), false);
		return 1;
	}
}
