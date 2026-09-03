package com.breakinblocks.saelibvie.ui.core;

import java.util.function.Consumer;

public final class EditSession {
    private final Consumer<Boolean> result;
    private boolean finished;

    public EditSession(Consumer<Boolean> result) {
        this.result = result;
    }

    public static EditSession of(Consumer<Boolean> result) {
        return new EditSession(result);
    }

    public boolean isFinished() {
        return finished;
    }

    public void accept() {
        finish(true);
    }

    public void cancel() {
        finish(false);
    }

    public void finish(boolean accepted) {
        if (finished) return;
        finished = true;
        result.accept(accepted);
    }
}
