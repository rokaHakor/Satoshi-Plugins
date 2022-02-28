package net.runelite.client.plugins.scripts;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.ScriptEvent;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension
@PluginDescriptor(name = "Auto Dialogue", enabledByDefault = false, description = "Automates Dialogues", tags = {"satoshi"})

@Slf4j
public class AutoDialogue extends Plugin {

    static final String CONFIG = "autoDialogue";

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private AutoDialogueConfig config;

    @Provides
    AutoDialogueConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoDialogueConfig.class);
    }

    private final Map<String, Integer> multiSkillSet = new HashMap<>();
    private String currentMultiSkill = null;

    private final ArrayList<StringTuple> dialogueList = new ArrayList<>();

    @Override
    protected void startUp() {
        multiSkillSet.clear();
        currentMultiSkill = null;
        getConfigValues();
    }

    @Override
    protected void shutDown() {
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals(CONFIG)) {
            return;
        }
        getConfigValues();
    }

    @Subscribe
    private void onScriptPreFired(ScriptPreFired event) {
        ScriptEvent scriptEvent = event.getScriptEvent();
        if (scriptEvent == null) {
            return;
        }

        if (currentMultiSkill == null) {
            return;
        }

        if (event.getScriptId() == 2050) {
            Object[] args = scriptEvent.getArguments();
            int num = Integer.parseInt(args[4].toString());
            multiSkillSet.put(currentMultiSkill, num);

        }

        if (event.getScriptId() == 2051) {
            Object[] args = scriptEvent.getArguments();
            int keychar = scriptEvent.getTypedKeyChar();
            int keycode = scriptEvent.getTypedKeyCode();
            int space = Integer.parseInt(args[6].toString());
            int num = Integer.parseInt(args[5].toString());
            if (keycode == KeyCode.KC_SPACE && space == KeyCode.KC_SPACE) {
                multiSkillSet.put(currentMultiSkill, num);
                return;
            }
            int charValue = Character.getNumericValue((char) keychar);
            if (charValue == num) {
                multiSkillSet.put(currentMultiSkill, num);
                return;
            }
        }
    }

    @Subscribe
    private void onWidgetClosed(WidgetClosed event) {
        int groupId = event.getGroupId();

        if (event.getGroupId() == WidgetID.MULTISKILL_MENU_GROUP_ID) {
            currentMultiSkill = null;
        }
    }

    private final List<Integer> CONTINUE_SET = List.of(
            WidgetInfo.LEVEL_UP_CONTINUE.getGroupId(), WidgetInfo.DIALOG_NOTIFICATION_CONTINUE.getGroupId(),
            WidgetInfo.DIALOG2_SPRITE_CONTINUE.getGroupId(), WidgetID.DIALOG_NPC_GROUP_ID,
            WidgetID.DIALOG_PLAYER_GROUP_ID, WidgetInfo.MINIGAME_DIALOG_CONTINUE.getGroupId(),
            WidgetID.DIALOG_SPRITE_GROUP_ID
    );

    @Subscribe
    private void onWidgetLoaded(WidgetLoaded event) {
        int groupId = event.getGroupId();

        if (CONTINUE_SET.contains(groupId)) {
            if (config.autoContinue()) {
                pressSpace();
            }
            return;
        }

        if (event.getGroupId() == WidgetID.MULTISKILL_MENU_GROUP_ID && config.skillSpam()) {
            clientThread.invokeLater(() -> {
                Widget multiSkillFirst = client.getWidget(WidgetID.MULTISKILL_MENU_GROUP_ID, 14);
                Widget multiSkillSecond = client.getWidget(WidgetID.MULTISKILL_MENU_GROUP_ID, 15);
                if (multiSkillFirst == null) {
                    return;
                }
                if (multiSkillFirst.getName().isEmpty()) {
                    return;
                }
                currentMultiSkill = multiSkillFirst.getName();
                if (multiSkillSecond == null || multiSkillSecond.getName().isEmpty()) {
                    pressSpace();
                    return;
                }
                if (!multiSkillSet.containsKey(multiSkillFirst.getName())) {
                    return;
                }
                pressNum(multiSkillSet.get(multiSkillFirst.getName()));
            });
            return;
        }

        if (groupId == WidgetInfo.DIALOG_OPTION_OPTION1.getGroupId()) {
            clientThread.invokeLater(() -> {
                Widget w = client.getWidget(WidgetInfo.DIALOG_OPTION_OPTION1);
                if (w == null) {
                    return;
                }
                Widget[] wChildren = w.getChildren();
                if (wChildren == null) {
                    return;
                }
                if (wChildren[0] == null) {
                    return;
                }

                if (config.useDialogueList()) {
                    for (StringTuple tuple : dialogueList) {
                        for (int option = 1; option < wChildren.length; option++) {
                            if (WildcardMatcher.matches(tuple.getQuestion(), sanitizeEntry(wChildren[0].getText())) && WildcardMatcher.matches(tuple.getTarget(), sanitizeEntry(wChildren[option].getText()))) {
                                log.info("Dialogue List Input: {}, {}:{}", option, tuple.getQuestion(), tuple.getTarget());
                                pressNum(option);
                                break;
                            }
                        }
                    }
                }

                if (config.questSpam()) {
                    for (int x = wChildren.length - 1; x >= 0; x--) {
                        if (wChildren[x].getText().indexOf('[') == 0 && wChildren[x].getText().indexOf(']') == 2) {
                            log.info("Quest Option Input: " + x);
                            pressNum(x);
                            return;
                        }
                    }
                }
            });
        }
    }

    private void pressSpace() {
        log.info("Press Space");
        long time = System.currentTimeMillis();
        keyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_SPACE, time, ' ');
        keyEvent(KeyEvent.KEY_TYPED, KeyEvent.VK_UNDEFINED, time, ' ');
        keyEvent(KeyEvent.KEY_RELEASED, KeyEvent.VK_SPACE, System.currentTimeMillis() + 100, ' ');
    }

    private void pressNum(int x) {
        if (x > 9) {
            log.info("Error pressNum > 9");
            return;
        }
        long time = System.currentTimeMillis();
        log.info("Pressing " + Character.forDigit(x, 10));
        keyEvent(KeyEvent.KEY_PRESSED, 48 + x, time, Character.forDigit(x, 10));
        keyEvent(KeyEvent.KEY_TYPED, KeyEvent.VK_UNDEFINED, time, Character.forDigit(x, 10));
        keyEvent(KeyEvent.KEY_RELEASED, 48 + x, System.currentTimeMillis() + 100, Character.forDigit(x, 10));
    }

    private void keyEvent(int id, int key, long time, char c) {
        KeyEvent e = new KeyEvent(client.getCanvas(), id, time, 0, key, c);
        client.getCanvas().dispatchEvent(e);
    }

    private String sanitizeEntry(String text) {
        return Text.removeTags(Text.standardize(text));
    }

    private void getConfigValues() {
        String[] lines = config.dialogueList().split("\\r?\\n");
        dialogueList.clear();

        if (lines.length == 1 && lines[0].isBlank()) {
            return;
        }

        for (String line : lines) {
            String[] priority = line.split(":");

            if (priority.length == 2) {
                dialogueList.add(new StringTuple(priority[0].toLowerCase(), priority[1].toLowerCase()));
            }
        }
    }
}