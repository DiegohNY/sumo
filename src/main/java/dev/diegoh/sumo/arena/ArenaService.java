package dev.diegoh.sumo.arena;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ArenaService {
    private static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z0-9_-]{1,32}");

    private final ArenaRepository repository;

    public ArenaService(ArenaRepository repository) {
        this.repository = repository;
        this.repository.loadAll();
    }

    public Optional<Arena> create(String id, Arena.Builder template) {
        if (!ID_PATTERN.matcher(id).matches()) return Optional.empty();
        if (repository.find(id).isPresent()) return Optional.empty();
        Arena arena = template.id(id).build();
        repository.save(arena);
        return Optional.of(arena);
    }

    public Optional<Arena> update(Arena arena) {
        if (repository.find(arena.id()).isEmpty()) return Optional.empty();
        repository.save(arena);
        return Optional.of(arena);
    }

    public boolean delete(String id) {
        if (repository.find(id).isEmpty()) return false;
        repository.delete(id);
        return true;
    }

    public Optional<Arena> find(String id) {
        return repository.find(id);
    }

    public Collection<Arena> all() {
        return repository.all();
    }
}
