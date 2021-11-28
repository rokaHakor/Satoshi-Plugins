package net.runelite.client.plugins.scripts;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("invWithdrawer")
public interface InvSetupWithdrawConfig extends Config {

    @ConfigItem(
            keyName = "withdrawSetupHotkey",
            name = "Withdraw Inventory Setup Hotkey",
            description = "Configures the hotkey for quick withdraw",
            position = 1
    )
    default Keybind withdrawSetup() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "withdrawEquipmentHotkey",
            name = "Withdraw Equipment Setup Hotkey",
            description = "Configures the hotkey for equipment quick withdraw",
            position = 2
    )
    default Keybind withdrawEquipment() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "withdrawBothHotkey",
            name = "Withdraw Both Setup Hotkey",
            description = "Configures the hotkey for both quick withdraw",
            position = 3
    )
    default Keybind withdrawAll() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            position = 4,
            keyName = "withdrawSpeed",
            name = "Withdraw Speed",
            description = "How fast to withdraw items"
    )
    default Speed speed() {
        return Speed.MEDIUM;
    }

}
