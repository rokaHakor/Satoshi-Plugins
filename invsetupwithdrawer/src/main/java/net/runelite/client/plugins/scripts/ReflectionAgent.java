package net.runelite.client.plugins.scripts;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

@Slf4j
public class ReflectionAgent {

    public static Object getCurrentSetup(Plugin withdrawer, PluginManager pluginManager, ChatMessageManager chatMessageManager) {
        try {
            Plugin inventorySetupsPlugin = null;

            for (Plugin plugin : pluginManager.getPlugins()) {
                if (plugin.getName().equals("Inventory Setups")) {
                    inventorySetupsPlugin = plugin;
                    break;
                }
            }

            if (inventorySetupsPlugin == null || !pluginManager.isPluginEnabled(inventorySetupsPlugin)) {
                SwingUtilities.invokeLater(() ->
                {
                    try {
                        pluginManager.setPluginEnabled(withdrawer, false);
                        pluginManager.stopPlugin(withdrawer);
                    } catch (PluginInstantiationException ex) {
                        log.error("Stopping plugin, Missing Inventory Setups", ex);
                    }
                });
                String chatMessage = new ChatMessageBuilder()
                        .append(ChatColorType.HIGHLIGHT)
                        .append("Stopping plugin, You need Inventory Setups from plugin hub")
                        .build();

                chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
                log.info("Stopping plugin, Missing Inventory Setups");
                return null;
            }

            Field privateField = inventorySetupsPlugin.getClass().getDeclaredField("panel");
            privateField.setAccessible(true);
            Object panel = privateField.get(inventorySetupsPlugin);

            privateField = panel.getClass().getDeclaredField("currentSelectedSetup");
            privateField.setAccessible(true);
            Object inventorySetup = privateField.get(panel);

            if (inventorySetup == null) {
                log.info("No Inventory setup open");
                String chatMessage = new ChatMessageBuilder()
                        .append(ChatColorType.HIGHLIGHT)
                        .append("No Inventory setup open")
                        .build();

                chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
                return null;
            }

            return inventorySetup;
        } catch (Throwable e) {
            log.info("Error: ", e);
        }
        return null;
    }

    public static ArrayList<InventorySetupsItem> getInventorySetup(Plugin withdrawer, PluginManager pluginManager, ChatMessageManager chatMessageManager) {
        ArrayList<InventorySetupsItem> currentSetup = new ArrayList<>();
        try {
            Object inventorySetup = getCurrentSetup(withdrawer, pluginManager, chatMessageManager);

            if (inventorySetup == null) {
                return null;
            }

            Field privateField = inventorySetup.getClass().getDeclaredField("inventory");
            privateField.setAccessible(true);
            ArrayList<Object> array = (ArrayList<Object>) privateField.get(inventorySetup);

            for (Object obj : array) {
                privateField = obj.getClass().getDeclaredField("id");
                privateField.setAccessible(true);
                int id = (int) privateField.get(obj);

                privateField = obj.getClass().getDeclaredField("name");
                privateField.setAccessible(true);
                String name = (String) privateField.get(obj);

                privateField = obj.getClass().getDeclaredField("quantity");
                privateField.setAccessible(true);
                int quantity = (int) privateField.get(obj);

                privateField = obj.getClass().getDeclaredField("fuzzy");
                privateField.setAccessible(true);
                boolean fuzzy = (boolean) privateField.get(obj);

                currentSetup.add(new InventorySetupsItem(id, name, quantity, fuzzy));
            }
            return currentSetup;
        } catch (Throwable e) {
            log.info("Error: ", e);
        }
        return null;
    }

    public static ArrayList<InventorySetupsItem> getEquipmentSetup(Plugin withdrawer, PluginManager pluginManager, ChatMessageManager chatMessageManager) {
        ArrayList<InventorySetupsItem> currentSetup = new ArrayList<>();
        try {
            Object inventorySetup = getCurrentSetup(withdrawer, pluginManager, chatMessageManager);

            if (inventorySetup == null) {
                return null;
            }

            Field privateField = inventorySetup.getClass().getDeclaredField("equipment");
            privateField.setAccessible(true);
            ArrayList<Object> array = (ArrayList<Object>) privateField.get(inventorySetup);

            for (Object obj : array) {
                privateField = obj.getClass().getDeclaredField("id");
                privateField.setAccessible(true);
                int id = (int) privateField.get(obj);

                privateField = obj.getClass().getDeclaredField("name");
                privateField.setAccessible(true);
                String name = (String) privateField.get(obj);

                privateField = obj.getClass().getDeclaredField("quantity");
                privateField.setAccessible(true);
                int quantity = (int) privateField.get(obj);

                privateField = obj.getClass().getDeclaredField("fuzzy");
                privateField.setAccessible(true);
                boolean fuzzy = (boolean) privateField.get(obj);

                currentSetup.add(new InventorySetupsItem(id, name, quantity, fuzzy));
            }
            return currentSetup;
        } catch (Throwable e) {
            log.info("Error: ", e);
        }
        return null;
    }
}