package com.breakinblocks.saelibvie.ui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class RadioGroup {
    private final List<Checkbox> boxes = new ArrayList<>();
    private int selected = -1;
    private IntConsumer onChange = i -> {
    };

    public RadioGroup add(Checkbox box) {
        int index = boxes.size();
        boxes.add(box);
        box.radio(true);
        box.bind(() -> selected == index, checked -> {
            if (checked) {
                select(index);
            }
        });
        return this;
    }

    public RadioGroup onChange(IntConsumer onChange) {
        this.onChange = onChange;
        return this;
    }

    public void select(int index) {
        if (index < 0 || index >= boxes.size() || index == selected) return;
        selected = index;
        onChange.accept(index);
    }

    public int selectedIndex() {
        return selected;
    }

    public List<Checkbox> boxes() {
        return boxes;
    }
}
