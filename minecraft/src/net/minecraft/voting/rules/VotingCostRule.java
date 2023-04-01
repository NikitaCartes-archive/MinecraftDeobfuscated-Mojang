package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.votes.VotingMaterial;

public class VotingCostRule implements Rule {
	private static final List<VotingMaterial.Cost> DEFAULT = List.of(
		new VotingMaterial.Cost(VotingMaterial.VOTES_PER_PROPOSAL, 1), new VotingMaterial.Cost(VotingMaterial.VOTES_PER_OPTION, 1)
	);
	final List<VotingMaterial.Cost> defaultValue = DEFAULT;
	List<VotingMaterial.Cost> currentValue = DEFAULT;
	private final Codec<RuleChange> codec = Rule.puntCodec(
		VotingMaterial.Cost.CODEC.listOf().xmap(list -> new VotingCostRule.Change(list), change -> change.newValue)
	);

	@Override
	public Codec<RuleChange> codec() {
		return this.codec;
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.currentValue.equals(this.defaultValue) ? Stream.empty() : Stream.of();
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		List<List<VotingMaterial.Cost>> list = createPermutations(randomSource, this.currentValue);
		Util.shuffle(list, randomSource);
		return list.stream().limit((long)i).map(listx -> new VotingCostRule.Change(listx));
	}

	public static List<List<VotingMaterial.Cost>> createPermutations(RandomSource randomSource, List<VotingMaterial.Cost> list) {
		List<VotingMaterial.Cost> list2 = getPermutations(list, List.of(VotingMaterial.Type.PER_PROPOSAL), 0, randomSource);
		List<VotingMaterial.Cost> list3 = getPermutations(list, List.of(VotingMaterial.Type.PER_OPTION), 1, randomSource);
		List<VotingMaterial.Cost> list4 = getPermutations(list, List.of(VotingMaterial.Type.ITEM, VotingMaterial.Type.RESOURCE), 2, randomSource);
		List<List<VotingMaterial.Cost>> list5 = new ObjectArrayList<>();
		generatePermutations(list5, list2, list3, list4, list);
		return list5;
	}

	private static List<VotingMaterial.Cost> getPermutations(@Nullable VotingMaterial.Cost cost, List<VotingMaterial.Type> list, RandomSource randomSource) {
		List<VotingMaterial.Cost> list2 = new ArrayList();

		while (list2.size() < 3) {
			for (VotingMaterial.Type type : list) {
				type.random(randomSource).filter(cost2 -> !cost2.equals(cost)).ifPresent(list2::add);
			}
		}

		int i = list2.size();
		if (cost != null) {
			for (int j = 0; j < i; j++) {
				list2.add(cost);
			}
		}

		for (int j = 0; j < i; j++) {
			list2.add(null);
		}

		return list2;
	}

	private static List<VotingMaterial.Cost> getPermutations(List<VotingMaterial.Cost> list, List<VotingMaterial.Type> list2, int i, RandomSource randomSource) {
		VotingMaterial.Cost cost = i < list.size() ? (VotingMaterial.Cost)list.get(i) : null;
		return getPermutations(cost, list2, randomSource);
	}

	private static void generatePermutations(
		List<List<VotingMaterial.Cost>> list,
		List<VotingMaterial.Cost> list2,
		List<VotingMaterial.Cost> list3,
		List<VotingMaterial.Cost> list4,
		List<VotingMaterial.Cost> list5
	) {
		ObjectArrayList<VotingMaterial.Cost> objectArrayList = new ObjectArrayList<>();

		for (VotingMaterial.Cost cost : list2) {
			if (cost != null) {
				objectArrayList.push(cost);
			}

			for (VotingMaterial.Cost cost2 : list3) {
				if (!isSameMaterial(cost, cost2)) {
					if (cost2 != null) {
						objectArrayList.push(cost2);
					}

					for (VotingMaterial.Cost cost3 : list4) {
						if (!isSameMaterial(cost, cost3) && !isSameMaterial(cost2, cost3)) {
							if (cost3 != null) {
								objectArrayList.push(cost3);
							}

							if (!objectArrayList.isEmpty() && !objectArrayList.equals(list5)) {
								list.add(List.copyOf(objectArrayList));
							}

							if (cost3 != null) {
								objectArrayList.pop();
							}
						}
					}

					if (cost2 != null) {
						objectArrayList.pop();
					}
				}
			}

			if (cost != null) {
				objectArrayList.pop();
			}
		}
	}

	private static boolean isSameMaterial(@Nullable VotingMaterial.Cost cost, @Nullable VotingMaterial.Cost cost2) {
		return cost != null && cost2 != null ? cost.material().equals(cost2.material()) : false;
	}

	public List<VotingMaterial.Cost> get() {
		return this.currentValue;
	}

	class Change implements RuleChange.Simple {
		private static final Component SEPARATOR = Component.literal(", ");
		final List<VotingMaterial.Cost> newValue;
		private final Component description;

		Change(List<VotingMaterial.Cost> list) {
			this.newValue = list;
			List<Component> list2 = list.stream().map(cost -> cost.display(true)).toList();
			this.description = Component.translatable("rule.new_vote_cost", ComponentUtils.formatList(list2, SEPARATOR));
		}

		@Override
		public Rule rule() {
			return VotingCostRule.this;
		}

		@Override
		public void update(RuleAction ruleAction) {
			VotingCostRule.this.currentValue = switch (ruleAction) {
				case APPROVE -> this.newValue;
				case REPEAL -> VotingCostRule.this.defaultValue;
			};
		}

		@Override
		public Component description() {
			return this.description;
		}
	}
}
