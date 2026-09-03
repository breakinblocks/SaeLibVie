package com.breakinblocks.saelibvie.ui.util;

import com.breakinblocks.saelibvie.SaeLibVie;

import java.util.ArrayList;
import java.util.List;

public final class ClientTasks {
    private static final List<Runnable> QUEUE = new ArrayList<>();

    private ClientTasks() {
    }

    public static void later(Runnable task) {
        synchronized (QUEUE) {
            QUEUE.add(task);
        }
    }

    public static void drain() {
        List<Runnable> tasks;
        synchronized (QUEUE) {
            if (QUEUE.isEmpty()) return;
            tasks = new ArrayList<>(QUEUE);
            QUEUE.clear();
        }
        for (Runnable task : tasks) {
            try {
                task.run();
            } catch (Exception e) {
                SaeLibVie.LOGGER.error("Client task failed", e);
            }
        }
    }
}
