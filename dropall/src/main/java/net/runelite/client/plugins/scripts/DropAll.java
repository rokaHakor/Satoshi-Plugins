package net.runelite.client.plugins.scripts;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

@SuppressWarnings("UnnecessaryReturnStatement")
@Extension
@PluginDescriptor(
        name = "Drop All",
        enabledByDefault = false,
        description = "Drops all of an item"
)

@Slf4j
public class DropAll extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private MenuManager menuManager;

    @Inject
    private DropAllConfig config;

    @Provides
    DropAllConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DropAllConfig.class);
    }

    private static final String DROP_ALL = "Drop-all";

    private final LinkedList<WidgetItem> dropItems = new LinkedList<>();
    private final Random random = new Random();

    private long dropTimer;
    private boolean clicked;

    @Override
    protected void startUp() {
        dropItems.clear();
        clicked = false;
        dropTimer = 0;
    }

    @Override
    protected void shutDown() {
        menuManager.removePlayerMenuItem(DROP_ALL);
    }

    @SuppressWarnings("DuplicatedCode")
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuAction() == MenuAction.RUNELITE) {
            if (event.getMenuOption().equals(DROP_ALL)) {
                log.info("Drop-all");
                List<WidgetItem> drop = getItems(event.getId());
                if (!drop.isEmpty()) {
                    dropItems.addAll(drop);
                    return;
                }
            }
            return;
        }

        if (!dropItems.isEmpty()) {
            WidgetItem drop = dropItems.pop();
            if (drop != null) {
                dropTimer = System.currentTimeMillis() + random.nextInt(config.speed().getSpeed()) + random.nextInt(config.speed().getSpeed()) + random.nextInt(config.speed().getSpeed()) + 75;
                event.setMenuEntry(new NewMenuEntry("Drop", "Drop", 7, MenuAction.CC_OP_LOW_PRIORITY.getId(), drop.getIndex(), WidgetInfo.INVENTORY.getId(), false));
                clicked = false;
            }
        }
    }

    @Subscribe
    public void onMenuOpened(final MenuOpened event) {
        final MenuEntry firstEntry = event.getFirstEntry();

        if (firstEntry == null) {
            return;
        }

        final int widgetId = firstEntry.getParam1();

        // Inventory item menu
        if (widgetId == WidgetInfo.INVENTORY.getId()) {
            int itemId = getWidgetItemInSlot(firstEntry.getParam0()).getId();

            if (itemId == -1) {
                return;
            }
            MenuEntry[] menuEntries = client.getMenuEntries();
            MenuEntry[] newEntries = Arrays.copyOf(menuEntries, menuEntries.length - 1);
            int index = 0;
            boolean hasDrop = false;
            for (MenuEntry entry : menuEntries) {
                if (!entry.getOption().equals("Examine")) {
                    newEntries[index] = entry;
                    index++;
                }
                if (entry.getOption().equals("Drop")) {
                    hasDrop = true;
                }
            }
            if (config.removeExamine()) {
                client.setMenuEntries(newEntries);
            }
            if (hasDrop) {
                client.createMenuEntry(config.removeExamine() ? 1 : 2).setOption(DROP_ALL)
                        .setTarget(ColorUtil.prependColorTag(client.getItemComposition(itemId).getName(), new Color(255, 144, 64)))
                        .setIdentifier(itemId)
                        .setParam1(0)
                        .setParam1(widgetId)
                        .setType(MenuAction.RUNELITE);
            }
            return;
        }
    }

    @Subscribe
    private void onClientTick(final ClientTick event) {
        if (dropItems.isEmpty()) {
            return;
        }
        if (!clicked && dropTimer < System.currentTimeMillis()) {
            click();
            clicked = true;
        }
    }

    public void click() {
        Point pos = client.getMouseCanvasPosition();

        if (client.isStretchedEnabled()) {
            final Dimension stretched = client.getStretchedDimensions();
            final Dimension real = client.getRealDimensions();
            final double width = (stretched.width / real.getWidth());
            final double height = (stretched.height / real.getHeight());
            pos = new Point((int) (pos.getX() * width), (int) (pos.getY() * height));
        }

        long time = System.currentTimeMillis();
        int randomValue = random.nextInt(50) + 50;

        client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 501, time, 0, pos.getX(), pos.getY(), 1, false, 1));
        client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 502, time + randomValue, 0, pos.getX(), pos.getY(), 1, false, 1));
        client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 500, time + randomValue, 0, pos.getX(), pos.getY(), 1, false, 1));
    }

    private WidgetItem createWidgetItem(Widget item) {
        boolean isDragged = item.isWidgetItemDragged(item.getItemId());

        int dragOffsetX = 0;
        int dragOffsetY = 0;

        if (isDragged) {
            Point p = item.getWidgetItemDragOffsets();
            dragOffsetX = p.getX();
            dragOffsetY = p.getY();
        }
        // set bounds to same size as default inventory
        Rectangle bounds = item.getBounds();
        bounds.setBounds(bounds.x - 1, bounds.y - 1, 32, 32);
        Rectangle dragBounds = item.getBounds();
        dragBounds.setBounds(bounds.x + dragOffsetX, bounds.y + dragOffsetY, 32, 32);

        return new WidgetItem(item.getItemId(), item.getItemQuantity(), item.getIndex(), bounds, item, dragBounds);
    }

    public Collection<WidgetItem> getWidgetItems() {
        Widget geWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER);

        boolean geOpen = geWidget != null && !geWidget.isHidden();
        boolean bankOpen = !geOpen && client.getItemContainer(InventoryID.BANK) != null;

        Widget inventoryWidget = client.getWidget(
                bankOpen ? WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER :
                        geOpen ? WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER :
                                WidgetInfo.INVENTORY
        );

        if (inventoryWidget == null) {
            return new ArrayList<>();
        }

        if (!bankOpen && !geOpen && inventoryWidget.isHidden()) {
            refreshInventory();
        }

        Widget[] children = inventoryWidget.getDynamicChildren();

        if (children == null) {
            return new ArrayList<>();
        }

        Collection<WidgetItem> widgetItems = new ArrayList<>();
        for (Widget item : children) {
            if (item.getItemId() != 6512) {
                widgetItems.add(createWidgetItem(item));
            }
        }

        return widgetItems;
    }

    public WidgetItem getWidgetItemInSlot(int slot) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null) {
            return new WidgetItem(-1, 0, slot, null, null, null);
        }

        if (inventoryWidget.isHidden()) {
            refreshInventory();
        }

        Widget[] children = inventoryWidget.getDynamicChildren();

        if (children == null || slot >= children.length) {
            return new WidgetItem(-1, 0, slot, null, null, null);
        }
        return createWidgetItem(children[slot]);
    }

    public void refreshInventory() {
        if (client.isClientThread()) {
            client.runScript(6009, 9764864, 28, 1, -1);
        } else {
            clientThread.invokeLater(() -> client.runScript(6009, 9764864, 28, 1, -1));
        }
    }

    public List<WidgetItem> getItems(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        List<WidgetItem> matchedItems = new ArrayList<>();

        if (inventoryWidget != null) {
            Collection<WidgetItem> items = getWidgetItems();
            for (WidgetItem item : items) {
                if (id == item.getId()) {
                    matchedItems.add(item);
                }
            }
            return matchedItems;
        }
        return null;
    }
}