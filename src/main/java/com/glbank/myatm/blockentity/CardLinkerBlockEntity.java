package com.glbank.myatm.blockentity;

import com.glbank.myatm.config.ATMConfigLoader;
import com.glbank.myatm.item.CardItem;
import com.glbank.myatm.menu.CardLinkerMenu;
import com.glbank.myatm.network.BankAPIClient;
import com.glbank.myatm.network.CardLinkerResponsePacket;
import com.glbank.myatm.network.PacketHandler;
import com.glbank.myatm.setup.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CardLinkerBlockEntity extends BlockEntity implements MenuProvider {

    private Component customName = null;
    private final Map<UUID, ItemStack> playerCards = new HashMap<>();

    public CardLinkerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CARD_LINKER_BE.get(), pos, state);
    }

    public SimpleContainer getContainerForPlayer(UUID playerId) {
        SimpleContainer container = new SimpleContainer(1) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return stack.getItem() instanceof CardItem;
            }
            @Override
            public void setChanged() {
                ItemStack stack = this.getItem(0);
                if (stack.isEmpty()) {
                    playerCards.remove(playerId);
                } else {
                    playerCards.put(playerId, stack.copy());
                }
                CardLinkerBlockEntity.this.setChanged();
            }
        };
        ItemStack stored = playerCards.getOrDefault(playerId, ItemStack.EMPTY);
        container.setItem(0, stored.copy());
        return container;
    }

    public void setCustomName(Component name) {
        this.customName = name;
        this.setChanged();
    }

    @Nullable
    public String getBlockConfigName() {
        if (customName == null) return null;
        return customName.getString();
    }

    @Override
    public Component getDisplayName() {
        return customName != null ? customName : Component.translatable("block.myatm.card_linker");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CardLinkerMenu(containerId, playerInventory, this, player.getUUID());
    }

    public void processRequest(ServerPlayer player, String login, String password, String pin) {
        if (level == null || level.isClientSide) return;

        String blockName = getBlockConfigName();
        if (blockName == null || blockName.isBlank()) {
            send(player, new CardLinkerResponsePacket(false, "", "", "Card Linker not configured (rename it in anvil)"));
            return;
        }

        String[] config = ATMConfigLoader.getConfig(blockName);
        if (config == null) {
            send(player, new CardLinkerResponsePacket(false, "", "", "Name '" + blockName + "' not found in myATM.cfg"));
            return;
        }

        final String url = config[0];
        final String apiPassword = config[1];
        final ServerLevel serverLevel = (ServerLevel) level;
        final UUID playerId = player.getUUID();

        CompletableFuture.supplyAsync(() ->
                BankAPIClient.cardLinkerRequest(url, apiPassword, login, password, pin)
        ).thenAccept(result -> {
            serverLevel.getServer().execute(() -> {
                if (result.success) {
                    ItemStack cardStack = playerCards.getOrDefault(playerId, ItemStack.EMPTY);
                    if (!cardStack.isEmpty() && cardStack.getItem() instanceof CardItem) {
                        ItemStack written = cardStack.copy();
                        written.setHoverName(Component.literal(login));
                        CompoundTag tag = written.getOrCreateTag();
                        tag.putString("CardNumber", result.cardNumber);
                        tag.putString("CVV", result.cvv);

                        playerCards.remove(playerId);
                        this.setChanged();

                        if (player.containerMenu instanceof CardLinkerMenu menu && menu.blockEntity == this) {
                            menu.getSlot(0).set(ItemStack.EMPTY);
                            menu.broadcastChanges();
                        }

                        boolean added = player.getInventory().add(written);
                        if (!added) player.drop(written, false);
                    }
                }
                send(player, new CardLinkerResponsePacket(result.success, result.cardNumber, result.cvv, result.message));
            });
        });
    }

    private void send(ServerPlayer player, CardLinkerResponsePacket pkt) {
        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), pkt);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (customName != null) tag.putString("CustomName", Component.Serializer.toJson(customName));
        ListTag list = new ListTag();
        for (Map.Entry<UUID, ItemStack> entry : playerCards.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putUUID("Player", entry.getKey());
                CompoundTag itemTag = new CompoundTag();
                entry.getValue().save(itemTag);
                entryTag.put("Item", itemTag);
                list.add(entryTag);
            }
        }
        tag.put("PlayerCards", list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("CustomName")) this.customName = Component.Serializer.fromJson(tag.getString("CustomName"));
        playerCards.clear();
        if (tag.contains("PlayerCards", Tag.TAG_LIST)) {
            ListTag list = tag.getList("PlayerCards", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                UUID id = entry.getUUID("Player");
                ItemStack item = ItemStack.of(entry.getCompound("Item"));
                if (!item.isEmpty()) playerCards.put(id, item);
            }
        }
    }
}
