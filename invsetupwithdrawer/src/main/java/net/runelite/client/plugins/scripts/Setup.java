package net.runelite.client.plugins.scripts;

import lombok.Getter;

public enum Setup {
    EQUIPMENT("equipment"),
    INVENTORY("inventory");

    @Getter
    private final String name;

    Setup(String name) {
        this.name = name;
    }
}
