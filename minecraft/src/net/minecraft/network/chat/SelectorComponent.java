package net.minecraft.network.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SelectorComponent extends BaseComponent implements ContextAwareComponent {
	private static final Logger LOGGER = LogManager.getLogger();
	private final String pattern;
	@Nullable
	private final EntitySelector selector;

	public SelectorComponent(String string) {
		this.pattern = string;
		EntitySelector entitySelector = null;

		try {
			EntitySelectorParser entitySelectorParser = new EntitySelectorParser(new StringReader(string));
			entitySelector = entitySelectorParser.parse();
		} catch (CommandSyntaxException var4) {
			LOGGER.warn("Invalid selector component: {}", string, var4.getMessage());
		}

		this.selector = entitySelector;
	}

	public String getPattern() {
		return this.pattern;
	}

	@Override
	public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
		return (MutableComponent)(commandSourceStack != null && this.selector != null
			? EntitySelector.joinNames(this.selector.findEntities(commandSourceStack))
			: new TextComponent(""));
	}

	@Override
	public String getContents() {
		return this.pattern;
	}

	public SelectorComponent plainCopy() {
		return new SelectorComponent(this.pattern);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof SelectorComponent)) {
			return false;
		} else {
			SelectorComponent selectorComponent = (SelectorComponent)object;
			return this.pattern.equals(selectorComponent.pattern) && super.equals(object);
		}
	}

	@Override
	public String toString() {
		return "SelectorComponent{pattern='" + this.pattern + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
	}
}
