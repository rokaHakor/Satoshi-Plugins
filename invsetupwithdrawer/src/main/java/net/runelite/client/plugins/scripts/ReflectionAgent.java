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
import java.lang.reflect.Method;
import java.util.ArrayList;

@Slf4j
public class ReflectionAgent {

    public static Plugin getInventorySetupPlugin(Plugin withdrawer, PluginManager pluginManager, ChatMessageManager chatMessageManager) {
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

            return inventorySetupsPlugin;
        } catch (Throwable e) {
            log.info("Error: ", e);
        }
        return null;
    }

    public static Object getSetupPanel(Plugin inventorySetupsPlugin) {
        try {
            Field privateField = inventorySetupsPlugin.getClass().getDeclaredField("panel");
            privateField.setAccessible(true);
            return privateField.get(inventorySetupsPlugin);
        } catch (Throwable e) {
            log.info("Error: ", e);
        }
        return null;
    }

    public static void openInventorySetup(Plugin withdrawer, PluginManager pluginManager, ChatMessageManager chatMessageManager, String setupName) {
        try {
            Plugin inventorySetupsPlugin = getInventorySetupPlugin(withdrawer, pluginManager, chatMessageManager);

            if (inventorySetupsPlugin == null) {
                return;
            }

            Field privateField = inventorySetupsPlugin.getClass().getDeclaredField("inventorySetups");
            privateField.setAccessible(true);
            ArrayList<Object> setups = (ArrayList<Object>) privateField.get(inventorySetupsPlugin);

            Object setup = null;
            for (Object obj : setups) {
                Field nameField = obj.getClass().getDeclaredField("name");
                nameField.setAccessible(true);
                String name = (String) nameField.get(obj);
                if (setupName.equals(name)) {
                    setup = obj;
                    break;
                }
            }

            if (setup == null) {
                String chatMessage = new ChatMessageBuilder()
                        .append(ChatColorType.HIGHLIGHT)
                        .append(setupName + " Inventory Setup not found")
                        .build();

                chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
                log.info(setupName + " Inventory Setup not found");
                return;
            }

            Object panel = getSetupPanel(inventorySetupsPlugin);
            if (panel == null) {
                return;
            }

            Method clearCurrentSetup = panel.getClass().getMethod("setCurrentInventorySetup", setup.getClass(), boolean.class);
            clearCurrentSetup.invoke(panel, setup, false);
        } catch (Throwable e) {
            log.info("Error: ", e);
        }
    }

    public static void closeCurrentSetup(Plugin withdrawer, PluginManager pluginManager, ChatMessageManager chatMessageManager) {
        try {
            Plugin inventorySetupsPlugin = getInventorySetupPlugin(withdrawer, pluginManager, chatMessageManager);

            if (inventorySetupsPlugin == null) {
                return;
            }

            Object panel = getSetupPanel(inventorySetupsPlugin);
            if (panel == null) {
                return;
            }

            Method clearCurrentSetup = panel.getClass().getMethod("returnToOverviewPanel", boolean.class);
            clearCurrentSetup.invoke(panel, false);
        } catch (Throwable e) {
            log.info("Error: ", e);
        }
    }

    public static Object getCurrentSetup(Object panel, ChatMessageManager chatMessageManager) {
        try {
            Field privateField = panel.getClass().getDeclaredField("currentSelectedSetup");
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

    private static ArrayList<InventorySetupsItem> getInventorySetupItems(ArrayList<InventorySetupsItem> currentSetup, Object inventorySetup, Field privateField) throws IllegalAccessException, NoSuchFieldException {
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
    }

    public static ArrayList<InventorySetupsItem> getInventorySetup(Plugin withdrawer, PluginManager pluginManager, ChatMessageManager chatMessageManager, Setup setup) {
        ArrayList<InventorySetupsItem> currentSetup = new ArrayList<>();
        try {
            Plugin inventorySetupsPlugin = getInventorySetupPlugin(withdrawer, pluginManager, chatMessageManager);

            if (inventorySetupsPlugin == null) {
                return null;
            }

            Object panel = getSetupPanel(inventorySetupsPlugin);

            if (panel == null) {
                return null;
            }

            Object inventorySetup = getCurrentSetup(panel, chatMessageManager);

            if (inventorySetup == null) {
                return null;
            }

            Field privateField = inventorySetup.getClass().getDeclaredField(setup.getName());
            privateField.setAccessible(true);
            return getInventorySetupItems(currentSetup, inventorySetup, privateField);
        } catch (Throwable e) {
            log.info("Error: ", e);
        }
        return null;
    }
}