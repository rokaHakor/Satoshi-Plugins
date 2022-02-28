package net.runelite.client.plugins.scripts;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(AutoDialogue.CONFIG)
public interface AutoDialogueConfig extends Config {

    @ConfigItem(
            position = 1,
            keyName = "autoContinue",
            name = "Auto Continue",
            description = "Presses space to continue"
    )
    default boolean autoContinue() {
        return true;
    }

    @ConfigItem(
            position = 2,
            keyName = "skillSpam",
            name = "Auto Skill Input",
            description = "Remembers and inputs number for multi-skill dialogue such as cooking, fletching, etc."
    )
    default boolean skillSpam() {
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "questSpam",
            name = "Quest Dialogues",
            description = "Presses number to advance dialogue while using Quest Helper"
    )
    default boolean questSpam() {
        return true;
    }

    @ConfigItem(
            position = 4,
            keyName = "useDialogueList",
            name = "Enable Dialogue List",
            description = "Enables/Disables dialogue list below."
    )
    default boolean useDialogueList() {
        return true;
    }

    @ConfigItem(
            position = 5,
            keyName = "dialogueList",
            name = "Dialogue List",
            description = "List of dialogue options. Syntax `header:option` such as `Select an Option*:Exchange All*`. Newline for each dialogue, case is insensitive, use * for wildcard."
    )
    default String dialogueList() {
        return "";
    }
}