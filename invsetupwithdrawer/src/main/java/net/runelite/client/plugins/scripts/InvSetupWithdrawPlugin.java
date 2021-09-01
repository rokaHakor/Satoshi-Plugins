package net.runelite.client.plugins.scripts;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.util.HotkeyListener;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

@Extension
@PluginDescriptor(
        name = "Inv Setup Withdrawer",
        description = "Withdraw inventory setups for specific activities, use with Plugin-hub Inventory Setups"
)
@Slf4j
public class InvSetupWithdrawPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private InvSetupWithdrawConfig config;

    @Inject
    private PluginManager pluginManager;

    @Inject
    private KeyManager keyManager;

    private final Random random = new Random();
    private boolean startWithdraw;
    private boolean withdrawLoop;
    private final LinkedList<InventorySetupsItem> withdraw = new LinkedList<>();
    private MenuEntry targetMenu;
    private long clickTimer;


    private final HotkeyListener quickWithdrawHotkeyListener = new HotkeyListener(() -> config.withdrawSetup()) {
        @Override
        public void hotkeyPressed() {
            log.info("Starting Quick Withdraw");
            quickWithdrawSetup();
        }
    };

    @Provides
    InvSetupWithdrawConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(InvSetupWithdrawConfig.class);
    }

    @Override
    public void startUp() {
        withdraw.clear();
        this.clickTimer = 0;
        this.startWithdraw = false;
        this.withdrawLoop = false;
        this.targetMenu = null;

        keyManager.registerKeyListener(quickWithdrawHotkeyListener);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (targetMenu != null) {
            event.setMenuEntry(targetMenu);
            if (startWithdraw) {
                if (event.getMenuTarget().contains("Withdraw")) {
                    clickTimer = System.currentTimeMillis() + random.nextInt(config.speed().getSpeed()) + random.nextInt(config.speed().getSpeed()) + random.nextInt(config.speed().getSpeed()) + 75;
                }
            }
            targetMenu = null;
        }
    }

    private int skipTick = 0;

    @Subscribe
    private void onGameTick(final GameTick event) {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (startWithdraw && itemContainerEmpty(inventory)) {
            if (skipTick < 1) {
                skipTick++;
                return;
            }
            skipTick = 0;
            withdrawLoop = true;
            withdrawNext();
        }
    }

    @Subscribe
    private void onClientTick(final ClientTick event) {
        if (targetMenu == null && withdrawLoop && startWithdraw && clickTimer < System.currentTimeMillis()) {
            withdrawNext();
        }
    }

    private boolean itemContainerEmpty(ItemContainer container) {
        if (container == null) {
            return true;
        }
        for (Item item : container.getItems()) {
            if (item.getId() != -1) {
                return false;
            }
        }
        return true;
    }

    public void quickWithdrawSetup() {
        ArrayList<InventorySetupsItem> currentSetup = ReflectionAgent.getCurrentSetup(this, pluginManager);
        if (client.getItemContainer(InventoryID.BANK) == null || currentSetup == null) {
            return;
        }
        skipTick = 0;
        startWithdraw = true;
        withdraw.addAll(currentSetup);
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (!itemContainerEmpty(inventory)) {
            targetMenu = new MenuEntry("Deposit inventory", "", 1, MenuAction.CC_OP.getId(), -1, 786473, false);
            click();
        }
    }

    private boolean withdrawNext() {
        if (client.getItemContainer(InventoryID.BANK) == null) {
            startWithdraw = false;
            withdrawLoop = false;
            withdraw.clear();
            return false;
        }

        if (withdraw.isEmpty()) {
            startWithdraw = false;
            withdrawLoop = false;
            return false;
        }

        InventorySetupsItem item = withdraw.pop();
        if (item.getId() == -1) {
            return withdrawNext();
        }

        if (client.getItemComposition(item.getId()).getNote() == -1 && client.getVarbitValue(3958) == 1) {
            withdraw.push(new InventorySetupsItem(item.getId(), item.getName(), item.getQuantity(), item.isFuzzy()));
            targetMenu = new MenuEntry("Item", "", 1, MenuAction.CC_OP.getId(), -1, 786453, false);
            click();
            return true;
        }

        if (client.getItemComposition(item.getId()).getNote() == 799) {
            if (client.getVarbitValue(3958) == 0) {
                withdraw.push(new InventorySetupsItem(item.getId(), item.getName(), item.getQuantity(), item.isFuzzy()));
                targetMenu = new MenuEntry("Note", "", 1, MenuAction.CC_OP.getId(), -1, 786455, false);
                click();
                return true;
            }
            int unnotedId = client.getItemComposition(item.getId()).getLinkedNoteId();
            item = new InventorySetupsItem(unnotedId, item.getName(), item.getQuantity(), item.isFuzzy());
        }

        Widget bankItemWidget = getBankItemWidget(item.getId());
        if (item.isFuzzy()) {
            InventorySetupsItem finalItem = item;
            WidgetItem bankItem = new BankItemQuery().filter(s -> getProcessedID(true, s.getId()) == getProcessedID(true, finalItem.getId())).result(client).first();
            if (bankItem != null) {
                bankItemWidget = bankItem.getWidget();
            }
        }
        if (bankItemWidget == null) {
            return withdrawNext();
        }

        if (onlyOneItemLeft(item)) {
            withdraw.clear();
            targetMenu = new MenuEntry("Withdraw-All", "Withdraw-All", 7, MenuAction.CC_OP.getId(), bankItemWidget.getIndex(), 786444, false);
            click();
            return true;
        }

        if (item.getQuantity() > 1) {
            if (item.getQuantity() == 2) {
                withdraw.push(new InventorySetupsItem(item.getId(), item.getName(), 1, item.isFuzzy()));
                targetMenu = new MenuEntry("Withdraw-1", "Withdraw-1", (client.getVarbitValue(6590) == 0) ? 1 : 2, MenuAction.CC_OP.getId(),
                        bankItemWidget.getIndex(), 786444, false);
                click();
                return true;
            }
            targetMenu = new MenuEntry("Withdraw-All", "Withdraw-All", 7, MenuAction.CC_OP.getId(), bankItemWidget.getIndex(), 786444, false);
            click();
            return true;
        }

        targetMenu = new MenuEntry("Withdraw-1", "Withdraw-1", (client.getVarbitValue(6590) == 0) ? 1 : 2, MenuAction.CC_OP.getId(),
                bankItemWidget.getIndex(), 786444, false);
        click();
        return true;
    }

    private boolean onlyOneItemLeft(InventorySetupsItem item) {
        if (withdraw.isEmpty()) {
            return false;
        }
        for (InventorySetupsItem remaining : withdraw) {
            if (remaining.getId() != item.getId()) {
                return false;
            }
        }
        return true;
    }

    private void click() {
        Point pos = client.getMouseCanvasPosition();

        if (client.isStretchedEnabled()) {
            final Dimension stretched = client.getStretchedDimensions();
            final Dimension real = client.getRealDimensions();
            final double width = (stretched.width / real.getWidth());
            final double height = (stretched.height / real.getHeight());
            final Point point = new Point((int) (pos.getX() * width), (int) (pos.getY() * height));
            client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 501, System.currentTimeMillis(), 0, point.getX(), point.getY(), 1, false, 1));
            client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 502, System.currentTimeMillis(), 0, point.getX(), point.getY(), 1, false, 1));
            client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 500, System.currentTimeMillis(), 0, point.getX(), point.getY(), 1, false, 1));
            return;
        }

        client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 501, System.currentTimeMillis(), 0, pos.getX(), pos.getY(), 1, false, 1));
        client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 502, System.currentTimeMillis(), 0, pos.getX(), pos.getY(), 1, false, 1));
        client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 500, System.currentTimeMillis(), 0, pos.getX(), pos.getY(), 1, false, 1));
    }

    private boolean isOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    private Widget getBankItemWidget(int id) {
        if (!isOpen()) {
            return null;
        }

        WidgetItem bankItem = new BankItemQuery().idEquals(id).result(client).first();
        if (bankItem != null) {
            return bankItem.getWidget();
        } else {
            return null;
        }
    }

    private int getProcessedID(boolean isFuzzy, int itemId) {
        if (isFuzzy) {
            return ItemVariationMapping.map(itemId);
        }
        return itemId;
    }
}
