package dev.tr7zw.itemswapper.overlay.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.itemswapper.api.AvailableSlot;
import dev.tr7zw.itemswapper.api.client.ItemSwapperClientAPI.OnSwap;
import dev.tr7zw.itemswapper.api.client.ItemSwapperClientAPI.SwapSent;
import dev.tr7zw.itemswapper.overlay.SwitchItemOverlay;
import dev.tr7zw.itemswapper.util.ItemUtil;
import dev.tr7zw.itemswapper.util.NetworkUtil;
import dev.tr7zw.itemswapper.util.RenderHelper;
import dev.tr7zw.itemswapper.util.WidgetUtil;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;

public class ListContentWidget extends ItemGridWidget {

    private final Item[] itemSelection;
    private final List<AvailableSlot> entries = new ArrayList<>();

    public ListContentWidget(Item[] items, int x, int y) {
        super(x, y);
        this.itemSelection = items;
        refreshList();
        WidgetUtil.setupDynamicSlots(widgetArea, slots, entries.size());
    }

    private void refreshList() {
        entries.clear();
        for (Item item : itemSelection) {
            List<AvailableSlot> ids = providerManager.findSlotsMatchingItem(item, false, false);
            for (AvailableSlot id : ids) {
                if (!entries.contains(id)) {
                    entries.add(id);
                }
            }
        }
    }

    private List<AvailableSlot> getItem(int id) {
        return id > entries.size() - 1 ? Collections.emptyList()
                : Collections.singletonList(entries.get(id));
    }

    @Override
    protected void renderSlot(PoseStack poseStack, int x, int y, List<Runnable> itemRenderList, GuiSlot guiSlot,
            boolean overwrideAvailable) {
        List<AvailableSlot> slots = getItem(guiSlot.id());
        if (!slots.isEmpty() && !overwrideAvailable) {
            itemRenderList.add(
                    () -> RenderHelper.renderSlot(poseStack, x + 3, y + 4, minecraft.player, slots.get(0).item(), 1,
                            false, slots.get(0).amount().get()));

        } else if (guiSlot.id() <= entries.size() - 1) {
            itemRenderList.add(
                    () -> RenderHelper.renderSlot(poseStack, x + 3, y + 4, minecraft.player,
                            entries.get(guiSlot.id()).item(), 1,
                            !overwrideAvailable, 1));
        }
    }

    @Override
    public void onClick(SwitchItemOverlay overlay, GuiSlot slot) {
        // doesn't link anywhere
    }

    @Override
    public void onClose(SwitchItemOverlay overlay, GuiSlot guiSlot) {
        if (guiSlot.id() > entries.size() - 1) {
            return;
        }
        AvailableSlot entry = entries.get(guiSlot.id());
        if (entry != null && !entry.item().isEmpty()) {
            OnSwap event = clientAPI.prepareItemSwapEvent.callEvent(new OnSwap(entry, new AtomicBoolean()));
            if (event.canceled().get()) {
                // interaction canceled by some other mod
                return;
            }
            if (entry.inventory() == -1) {
                int hudSlot = ItemUtil.inventorySlotToHudSlot(entry.slot());
                this.minecraft.gameMode.handleInventoryMouseClick(minecraft.player.inventoryMenu.containerId,
                        hudSlot, minecraft.player.getInventory().selected,
                        ClickType.SWAP, this.minecraft.player);
            } else {
                NetworkUtil.swapItem(entry.inventory(), entry.slot());
            }
            clientAPI.itemSwapSentEvent.callEvent(new SwapSent(entry));
        }
    }

    @Override
    public void renderSelectedSlotName(GuiSlot selected, int yOffset, boolean overwrideAvailable) {
        if (selected.id() > entries.size() - 1) {
            return;
        }
        AvailableSlot slot = entries.get(selected.id());
        if (slot == null) {
            return;
        }
        RenderHelper.renderSelectedItemName(ItemUtil.getDisplayname(slot.item()),
                slot.item(), false, yOffset);

    }

}