package net.runelite.client.plugins.scripts;

import lombok.Getter;

public enum Speed {
    FAST(30),
    MEDIUM(50),
    SLOW(70);

    @Getter
    private final int speed;

    Speed(int speed) {
        this.speed = speed;
    }
}
