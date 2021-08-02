package net.runelite.client.plugins.scripts;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
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
                event.setMenuEntry(new MenuEntry("Drop", "Drop", drop.getId(), MenuAction.ITEM_FIFTH_OPTION.getId(), drop.getIndex(), WidgetInfo.INVENTORY.getId(), false));
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
            int itemId = firstEntry.getIdentifier();

            if (itemId == -1) {
                return;
            }

            MenuEntry[] menuList = new MenuEntry[event.getMenuEntries().length + 1];
            int num = 0;
            boolean hasDrop = false;

            // preserve the 'Cancel' option as the client will reuse the first entry for Cancel and only resets option/action
            menuList[num++] = event.getMenuEntries()[0];

            for (int x = 1; x < event.getMenuEntries().length + 1; x++) {
                final MenuEntry newMenu;
                if (x == 1) {
                    if (event.getMenuEntries()[x].getOption().equals("Examine") && config.removeExamine()) {
                        newMenu = null;
                    } else {
                        newMenu = event.getMenuEntries()[x];
                    }
                } else if (x == 2) {
                    newMenu = new MenuEntry();
                    newMenu.setOption(DROP_ALL);
                    newMenu.setTarget(ColorUtil.prependColorTag(client.getItemComposition(itemId).getName(), new Color(255, 144, 64)));
                    newMenu.setIdentifier(itemId);
                    newMenu.setParam1(widgetId);
                    newMenu.setType(MenuAction.RUNELITE.getId());
                } else {
                    if (event.getMenuEntries()[x - 1].getOption().equals("Drop")) {
                        hasDrop = true;
                    }
                    newMenu = event.getMenuEntries()[x - 1];
                }
                menuList[num++] = newMenu;
            }
            if (hasDrop) {
                client.setMenuEntries(menuList);
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
            final Point point = new Point((int) (pos.getX() * width), (int) (pos.getY() * height));

            long time = System.currentTimeMillis();
            int randomValue = random.nextInt(50) + 50;

            client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 501, time, 0, point.getX(), point.getY(), 1, false, 1));
            client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 502, time + randomValue, 0, point.getX(), point.getY(), 1, false, 1));
            client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 500, time + randomValue, 0, point.getX(), point.getY(), 1, false, 1));
            return;
        }

        long time = System.currentTimeMillis();
        int randomValue = random.nextInt(50) + 50;

        client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 501, time, 0, pos.getX(), pos.getY(), 1, false, 1));
        client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 502, time + randomValue, 0, pos.getX(), pos.getY(), 1, false, 1));
        client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 500, time + randomValue, 0, pos.getX(), pos.getY(), 1, false, 1));
    }

    public List<WidgetItem> getItems(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        List<WidgetItem> matchedItems = new ArrayList<>();

        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
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