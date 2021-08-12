package net.runelite.client.plugins.scripts;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("invWithdrawer")
public interface InvSetupWithdrawConfig extends Config {

    @ConfigItem(
            keyName = "withdrawSetupHotkey",
            name = "Withdraw Items for Inventory Setup Hotkey",
            description = "Configures the hotkey for quick withdraw",
            position = 1
    )
    default Keybind withdrawSetup() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            position = 2,
            keyName = "withdrawSpeed",
            name = "Withdraw Speed",
            description = "How fast to withdraw items"
    )
    default Speed speed() {
        return Speed.MEDIUM;
    }

}
