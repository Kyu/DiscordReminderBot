package Util;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;

import java.util.List;
import java.util.function.Function;

public class StringSelectDropdownWithAction extends StringSelectMenuImpl  {
    private final Function<StringSelectInteractionEvent, Void> actionAfter;
    public StringSelectDropdownWithAction(DataObject data, Function<StringSelectInteractionEvent, Void> actionAfter) {
        super(data);
        this.actionAfter = actionAfter;
    }

    public StringSelectDropdownWithAction(String id, String placeholder, int minValues, int maxValues, boolean disabled, List<SelectOption> options, Function<StringSelectInteractionEvent, Void> actionAfter) {
        super(id, placeholder, minValues, maxValues, disabled, options);
        this.actionAfter = actionAfter;
    }

    public void interact(StringSelectInteractionEvent event) {
        actionAfter.apply(event);
    }
}
