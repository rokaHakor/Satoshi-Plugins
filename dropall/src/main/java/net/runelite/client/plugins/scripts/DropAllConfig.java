package net.runelite.client.plugins.scripts;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("dropAll")
public interface DropAllConfig extends Config {

    @ConfigItem(
            position = 1,
            keyName = "speed",
            name = "Speed",
            description = "How fast to drop items"
    )
    default Speed speed() {
        return Speed.MEDIUM;
    }

    @ConfigItem(
            position = 2,
            keyName = "removeExamine",
            name = "Remove Examine",
            description = "Removes Examine option for items"
    )
    default boolean removeExamine() {
        return false;
    }

}