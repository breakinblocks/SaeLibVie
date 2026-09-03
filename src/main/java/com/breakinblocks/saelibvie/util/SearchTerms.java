package com.breakinblocks.saelibvie.util;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public final class SearchTerms {
    public enum Kind {
        SIMPLE,
        MOD,
        TAG
    }

    public record Term(Kind kind, String value, @Nullable ResourceLocation tag) {
        boolean matches(ResourceLocation id, String displayName, Predicate<ResourceLocation> tagMatcher) {
            if (value.isEmpty()) return true;
            return switch (kind) {
                case MOD -> id.getNamespace().contains(value);
                case TAG -> tag != null && tagMatcher.test(tag);
                case SIMPLE -> displayName.toLowerCase(Locale.ROOT).contains(value);
            };
        }
    }

    public static final SearchTerms EMPTY = new SearchTerms(List.of());

    private final List<Term> terms;

    private SearchTerms(List<Term> terms) {
        this.terms = terms;
    }

    public static SearchTerms parse(String text) {
        if (text == null || text.isEmpty()) return EMPTY;
        List<Term> terms = new ArrayList<>();
        for (String raw : text.toLowerCase(Locale.ROOT).split(" +")) {
            if (raw.isEmpty()) continue;
            if (raw.charAt(0) == '@') {
                terms.add(new Term(Kind.MOD, raw.substring(1), null));
            } else if (raw.charAt(0) == '#') {
                String value = raw.substring(1);
                terms.add(new Term(Kind.TAG, value, value.isEmpty() ? null : ResourceLocation.tryParse(value)));
            } else {
                terms.add(new Term(Kind.SIMPLE, raw, null));
            }
        }
        return terms.isEmpty() ? EMPTY : new SearchTerms(terms);
    }

    public boolean isEmpty() {
        return terms.isEmpty();
    }

    public List<Term> terms() {
        return terms;
    }

    public boolean match(ResourceLocation id, String displayName, Predicate<ResourceLocation> tagMatcher) {
        for (Term term : terms) {
            if (!term.matches(id, displayName, tagMatcher)) return false;
        }
        return true;
    }
}
