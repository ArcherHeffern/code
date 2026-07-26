package frog.dptb.client.utils;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface ComponentSupplier {
    Component get();
}