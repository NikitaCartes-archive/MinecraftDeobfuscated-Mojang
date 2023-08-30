package net.minecraft.advancements;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import javax.annotation.Nullable;

public class AdvancementNode {
	private final AdvancementHolder holder;
	@Nullable
	private final AdvancementNode parent;
	private final Set<AdvancementNode> children = new ReferenceOpenHashSet<>();

	@VisibleForTesting
	public AdvancementNode(AdvancementHolder advancementHolder, @Nullable AdvancementNode advancementNode) {
		this.holder = advancementHolder;
		this.parent = advancementNode;
	}

	public Advancement advancement() {
		return this.holder.value();
	}

	public AdvancementHolder holder() {
		return this.holder;
	}

	@Nullable
	public AdvancementNode parent() {
		return this.parent;
	}

	public AdvancementNode root() {
		return getRoot(this);
	}

	public static AdvancementNode getRoot(AdvancementNode advancementNode) {
		AdvancementNode advancementNode2 = advancementNode;

		while (true) {
			AdvancementNode advancementNode3 = advancementNode2.parent();
			if (advancementNode3 == null) {
				return advancementNode2;
			}

			advancementNode2 = advancementNode3;
		}
	}

	public Iterable<AdvancementNode> children() {
		return this.children;
	}

	@VisibleForTesting
	public void addChild(AdvancementNode advancementNode) {
		this.children.add(advancementNode);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof AdvancementNode advancementNode && this.holder.equals(advancementNode.holder)) {
				return true;
			}

			return false;
		}
	}

	public int hashCode() {
		return this.holder.hashCode();
	}

	public String toString() {
		return this.holder.id().toString();
	}
}
