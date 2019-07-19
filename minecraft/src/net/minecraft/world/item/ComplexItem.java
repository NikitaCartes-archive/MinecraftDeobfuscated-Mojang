package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ComplexItem extends Item {
	public ComplexItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isComplex() {
		return true;
	}

	@Nullable
	public Packet<?> getUpdatePacket(ItemStack itemStack, Level level, Player player) {
		return null;
	}
}
