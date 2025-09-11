package org.event.backend.dto.inbox;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public record InboxPage<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int size,
        int number
) {
    public static <T> InboxPage<T> of(Page<T> p) {
        return new InboxPage<>(
                p.getContent(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.getSize(),
                p.getNumber()
        );
    }

    public static <S, T> InboxPage<T> ofMapped(Page<S> p, Function<S, T> mapper) {
        List<T> mapped = p.getContent().stream().map(mapper).collect(Collectors.toList());
        return new InboxPage<>(mapped, p.getTotalElements(), p.getTotalPages(), p.getSize(), p.getNumber());
    }
}
