package com.glbank.myatm.blockentity;

import com.glbank.myatm.block.TerminalBlock;
import com.glbank.myatm.config.ATMConfigLoader;
import com.glbank.myatm.item.CardItem;
import com.glbank.myatm.menu.TerminalMenu;
import com.glbank.myatm.network.BankAPIClient;
import com.glbank.myatm.network.PacketHandler;
import com.glbank.myatm.network.TerminalResponsePacket;
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

public class TerminalBlockEntity extends BlockEntity implements MenuProvider {

    private Component customName = null;
    private int activeTicks = 0;

    private final Map<UUID, ItemStack> playerCards = new HashMap<>();

    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TERMINAL_BE.get(), pos, state);
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
                TerminalBlockEntity.this.setChanged();
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

    @Override
    public Component getDisplayName() {
        return customName != null ? customName : Component.translatable("block.myatm.terminal");
    }

    @Nullable
    public String[] parseTerminalName() {
        if (customName == null) return null;
        String name = customName.getString();
        int eqIdx = name.indexOf('=');
        if (eqIdx < 0) return null;
        String configName = name.substring(0, eqIdx).trim();
        String receiverAcc = name.substring(eqIdx + 1).trim();
        if (configName.isEmpty() || receiverAcc.isEmpty()) return null;
        return new String[]{configName, receiverAcc};
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new TerminalMenu(containerId, playerInventory, this, player.getUUID());
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state,
                                  TerminalBlockEntity be) {
        if (be.activeTicks > 0) {
            be.activeTicks--;
            if (be.activeTicks == 0) {
                level.setBlock(pos, state.setValue(TerminalBlock.ACTIVE, false), 3);
                level.updateNeighborsAt(pos, state.getBlock());
            }
        }
    }

    public void processPayment(ServerPlayer player, int amount, String pin) {
        if (level == null || level.isClientSide) return;

        String[] nameParts = parseTerminalName();
        if (nameParts == null) {
            sendResponse(player, false, "Terminal not configured (name: ConfigName=AccountNumber)");
            return;
        }

        String configName = nameParts[0];
        String receiverAccount = nameParts[1];

        String[] config = ATMConfigLoader.getConfig(configName);
        if (config == null) {
            sendResponse(player, false, "Config entry '" + configName + "' not found in myATM.cfg");
            return;
        }

        ItemStack cardStack = playerCards.getOrDefault(player.getUUID(), ItemStack.EMPTY);
        if (cardStack.isEmpty() || !(cardStack.getItem() instanceof CardItem)) {
            sendResponse(player, false, "No card in terminal");
            return;
        }

        CompoundTag tag = cardStack.getTag();
        if (tag == null || !tag.contains("CardNumber") || !tag.contains("CVV")) {
            sendResponse(player, false, "Card has no data (use Card Linker first)");
            return;
        }

        final String cardNumber = tag.getString("CardNumber");
        final String cvv = tag.getString("CVV");
        final String url = config[0];
        final String apiPassword = config[1];
        final ServerLevel serverLevel = (ServerLevel) level;
        final BlockPos blockPos = this.getBlockPos();

        CompletableFuture.supplyAsync(() ->
                BankAPIClient.terminalRequest(url, apiPassword, cardNumber, cvv, receiverAccount, amount, pin)
        ).thenAccept(result -> {
            serverLevel.getServer().execute(() -> {
                if (result.success) {
                    BlockState currentState = serverLevel.getBlockState(blockPos);
                    serverLevel.setBlock(blockPos, currentState.setValue(TerminalBlock.ACTIVE, true), 3);
                    serverLevel.updateNeighborsAt(blockPos, currentState.getBlock());
                    this.activeTicks = 20;
                    this.setChanged();
                }
                sendResponse(player, result.success, result.message);
            });
        });
    }

    private void sendResponse(ServerPlayer player, boolean success, String message) {
        PacketHandler.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new TerminalResponsePacket(success, message));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(customName));
        }
        tag.putInt("ActiveTicks", activeTicks);

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
        if (tag.contains("CustomName")) {
            this.customName = Component.Serializer.fromJson(tag.getString("CustomName"));
        }
        this.activeTicks = tag.getInt("ActiveTicks");
        playerCards.clear();
        if (tag.contains("PlayerCards", Tag.TAG_LIST)) {
            ListTag list = tag.getList("PlayerCards", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                UUID id = entry.getUUID("Player");
                ItemStack item = ItemStack.of(entry.getCompound("Item"));
                if (!item.isEmpty()) {
                    playerCards.put(id, item);
                }
            }
        }
    }
}