package net.runelite.client.plugins.scripts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class InventorySetupsItem {
    @Getter
    private final int id;
    @Getter
    private final String name;
    @Getter
    private final int quantity;
    @Getter
    @Setter
    private boolean fuzzy;

    public void toggleIsFuzzy() {
        fuzzy = !fuzzy;
    }

    public static InventorySetupsItem getDummyItem() {
        return new InventorySetupsItem(-1, "", 0, false);
    }

}
