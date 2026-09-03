package com.breakinblocks.saelibvie.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class ItemStackSet extends AbstractSet<ItemStack> {
    private final LinkedHashMap<ItemKey, ItemStack> map = new LinkedHashMap<>();

    public ItemStackSet() {
    }

    public ItemStackSet(Collection<ItemStack> stacks) {
        addAll(stacks);
    }

    @Override
    public boolean add(ItemStack stack) {
        ItemKey key = new ItemKey(stack);
        if (map.containsKey(key)) return false;
        map.put(key, stack);
        return true;
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof ItemStack stack && map.containsKey(new ItemKey(stack));
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof ItemStack stack && map.remove(new ItemKey(stack)) != null;
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return map.values().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

    public List<ItemStack> sortedList() {
        List<ItemStack> list = new ArrayList<>(map.values());
        list.sort(Comparator.<ItemStack, String>comparing(s -> BuiltInRegistries.ITEM.getKey(s.getItem()).getNamespace())
                .thenComparing(s -> s.getHoverName().getString()));
        return list;
    }
}
