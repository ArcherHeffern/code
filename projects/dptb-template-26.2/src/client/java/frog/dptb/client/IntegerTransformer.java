package frog.dptb.client;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface IntegerTransformer {
    int get(int i);
}
