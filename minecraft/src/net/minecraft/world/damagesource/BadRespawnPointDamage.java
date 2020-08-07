package net.minecraft.world.damagesource;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;

public class BadRespawnPointDamage extends DamageSource {
	protected BadRespawnPointDamage() {
		super("badRespawnPoint");
		this.setScalesWithDifficulty();
		this.setExplosion();
	}

	@Override
	public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
		Component component = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("death.attack.badRespawnPoint.link"))
			.withStyle(
				style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723"))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("MCPE-28723")))
			);
		return new TranslatableComponent("death.attack.badRespawnPoint.message", livingEntity.getDisplayName(), component);
	}
}
