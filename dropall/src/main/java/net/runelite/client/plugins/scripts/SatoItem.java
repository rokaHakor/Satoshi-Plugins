package net.runelite.client.plugins.scripts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.runelite.api.widgets.Widget;

import java.util.Objects;

@AllArgsConstructor
@ToString
@Getter
public class SatoItem {

    private final int id;

    private final int quantity;

    private final int index;

    private final Widget widget;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SatoItem satoItem = (SatoItem) o;
        return id == satoItem.id && quantity == satoItem.quantity && index == satoItem.index && Objects.equals(widget, satoItem.widget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity, index, widget);
    }
}
