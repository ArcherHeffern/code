package frog.dptb.client;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface ComponentSupplier {
    Component get();
}