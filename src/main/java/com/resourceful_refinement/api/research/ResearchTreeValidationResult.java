package com.resourceful_refinement.api.research;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable validation report for datapack-authored research node graphs.
 * Later reload stages will use this to report missing parents, cycles, and duplicate recipe locks.
 */
public record ResearchTreeValidationResult(List<Issue> issues) {
    public ResearchTreeValidationResult {
        issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }

    public static ResearchTreeValidationResult ok() {
        return new ResearchTreeValidationResult(List.of());
    }

    public static ResearchTreeValidationResult of(List<Issue> issues) {
        return new ResearchTreeValidationResult(issues);
    }

    public boolean isValid() {
        return issues.stream().noneMatch(issue -> issue.severity() == Severity.ERROR);
    }

    public List<Issue> errors() {
        return issues.stream().filter(issue -> issue.severity() == Severity.ERROR).toList();
    }

    public List<Issue> warnings() {
        return issues.stream().filter(issue -> issue.severity() == Severity.WARNING).toList();
    }

    public enum Severity {
        WARNING,
        ERROR
    }

    public record Issue(Severity severity, Optional<ResourceLocation> nodeId, String message) {
        public Issue {
            Objects.requireNonNull(severity, "severity");
            nodeId = Objects.requireNonNull(nodeId, "nodeId");
            Objects.requireNonNull(message, "message");
            if (message.isBlank()) {
                throw new IllegalArgumentException("Validation issue message cannot be blank");
            }
        }

        public static Issue error(ResourceLocation nodeId, String message) {
            return new Issue(Severity.ERROR, Optional.of(nodeId), message);
        }

        public static Issue warning(ResourceLocation nodeId, String message) {
            return new Issue(Severity.WARNING, Optional.of(nodeId), message);
        }

        public static Issue globalError(String message) {
            return new Issue(Severity.ERROR, Optional.empty(), message);
        }

        public static Issue globalWarning(String message) {
            return new Issue(Severity.WARNING, Optional.empty(), message);
        }
    }
}
