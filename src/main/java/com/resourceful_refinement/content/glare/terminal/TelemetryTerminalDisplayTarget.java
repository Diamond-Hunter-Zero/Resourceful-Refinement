package com.resourceful_refinement.content.glare.terminal;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

/** Create Display Link target that forwards each source's current readout into the terminal's combined Auto-Send body. */
public class TelemetryTerminalDisplayTarget extends DisplayTarget {
    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        if (context.getTargetBlockEntity() instanceof TelemetryTerminalBlockEntity terminal) {
            String combined = text.stream().map(MutableComponent::getString).reduce((first, second) -> first + "\n" + second).orElse("");
            terminal.acceptDisplayLinkText(context.getSourcePos(), combined);
        }
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(8, 64, this);
    }
}
