package frog.dptb.client.utils;

import frog.dptb.client.context.DPTBSession;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class MathSolver {

    private int solveIndex = -1;


    public Optional<Float> trySolve(Minecraft client, DPTBSession session, Logger logger) {
        if (client.player == null) {
            return Optional.empty();
        }
        if (!client.player.hasContainerOpen()) {
            return Optional.empty();
        }
        AbstractContainerMenu menu = client.player.containerMenu;
        if (menu instanceof ChestMenu chestMenu) {
            int containerSize = chestMenu.getContainer().getContainerSize();
            if (solveIndex != -1) {
                Slot slot = chestMenu.getSlot(solveIndex);
                return trySolveSlot(slot, client, session, logger);
            } else {
                for (int i = 0; i < containerSize; i++) {
                    Slot slot = chestMenu.getSlot(i);
                    Optional<Float> maybeResult = trySolveSlot(slot, client, session, logger);
                    if (maybeResult.isPresent()) {
                        solveIndex = i;
                        return maybeResult;
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Float> trySolveSlot(Slot slot, Minecraft client, DPTBSession session, Logger logger) {
        if (slot.hasItem()) {
            ItemStack item = slot.getItem();
            String itemName = item.getHoverName().getString();
            if (itemName.isEmpty()) {
                return Optional.empty();
            }
            if (!itemName.contains("SOLVE")) {
                return Optional.empty();
            }
            Item.TooltipContext context = Item.TooltipContext.of(client.level);
            List<Component> tooltipLinesList = item.getTooltipLines(context, client.player, TooltipFlag.Default.NORMAL);
            String fullTooltipText = tooltipLinesList.stream()
                    .map(Component::getString)
                    .collect(Collectors.joining(" "));

            Matcher mathMatcher = Utils.MATH_EQUATION_REGEX.matcher(fullTooltipText);
            if (mathMatcher.find()) {
                String equation = mathMatcher.group();
                float result = Utils.mathSolver(equation);
                return Optional.of(result);
            } else {
                logger.error("Failed to parse math equation: {}", fullTooltipText);
            }
        }
        return Optional.empty();
    }
}
