package com.breakinblocks.saelibvie.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MapUtil {
    private MapUtil() {
    }

    public static <K, V> Map<K, V> sortMapByKey(Map<K, V> map, Comparator<? super K> comparator) {
        List<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Map.Entry.comparingByKey(comparator));
        Map<K, V> sorted = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            sorted.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return sorted;
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> sortMapByKey(Map<K, V> map) {
        return sortMapByKey(map, Comparator.naturalOrder());
    }
}
