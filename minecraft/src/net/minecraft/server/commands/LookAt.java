package net.minecraft.server.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface LookAt {
	void perform(CommandSourceStack commandSourceStack, Entity entity);

	public static record LookAtEntity(Entity entity, EntityAnchorArgument.Anchor anchor) implements LookAt {
		@Override
		public void perform(CommandSourceStack commandSourceStack, Entity entity) {
			if (entity instanceof ServerPlayer serverPlayer) {
				serverPlayer.lookAt(commandSourceStack.getAnchor(), this.entity, this.anchor);
			} else {
				entity.lookAt(commandSourceStack.getAnchor(), this.anchor.apply(this.entity));
			}
		}
	}

	public static record LookAtPosition(Vec3 position) implements LookAt {
		@Override
		public void perform(CommandSourceStack commandSourceStack, Entity entity) {
			entity.lookAt(commandSourceStack.getAnchor(), this.position);
		}
	}
}
