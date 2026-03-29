package com.glbank.myatm.menu;

import com.glbank.myatm.blockentity.CardLinkerBlockEntity;
import com.glbank.myatm.item.CardItem;
import com.glbank.myatm.setup.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CardLinkerMenu extends AbstractContainerMenu {

    public final CardLinkerBlockEntity blockEntity;
    public final UUID playerId;

    public CardLinkerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                (CardLinkerBlockEntity) playerInventory.player.level()
                        .getBlockEntity(buf.readBlockPos()),
                playerInventory.player.getUUID());
    }

    public CardLinkerMenu(int containerId, Inventory playerInventory, CardLinkerBlockEntity be, UUID playerId) {
        super(ModMenuTypes.CARD_LINKER_MENU.get(), containerId);
        this.blockEntity = be;
        this.playerId = playerId;

        SimpleContainer personalContainer = be.getContainerForPlayer(playerId);

        this.addSlot(new Slot(personalContainer, 0, 139, 22) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof CardItem;
            }
        });

        addPlayerInventory(playerInventory);
    }

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) return ItemStack.EMPTY;
            } else {
                if (slotStack.getItem() instanceof CardItem) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) return ItemStack.EMPTY;
                } else return ItemStack.EMPTY;
            }
            if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity.getLevel() != null
                && AbstractContainerMenu.stillValid(
                        net.minecraft.world.inventory.ContainerLevelAccess.create(
                                blockEntity.getLevel(), blockEntity.getBlockPos()),
                        player, blockEntity.getBlockState().getBlock());
    }
}
