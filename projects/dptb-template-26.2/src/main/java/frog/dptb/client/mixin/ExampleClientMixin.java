package frog.dptb.client.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ExampleClientMixin {

	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("MyHypixelMod");

	@Inject(at = @At("HEAD"), method = "handleContainerSetSlot")
	private void init(final ClientboundContainerSetSlotPacket packet, CallbackInfo info) {
		if (packet.getContainerId() > 0) {
			ItemStack itemStack = packet.getItem();

			// Check if the slot actually contains an item
			if (!itemStack.isEmpty()) {
				// Get the item's display name
				String itemName = itemStack.getHoverName().getString();
				int slotId = packet.getSlot();

				System.out.println("Hypixel Menu Slot " + slotId + " contains: " + itemName);
			}
		}
	}
}