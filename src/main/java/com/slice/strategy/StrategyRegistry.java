package com.slice.strategy;

import com.slice.exception.BusinessException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic registry that wires a collection of {@link ProcessingStrategy} beans
 * into a lookup map keyed by {@link ProcessingStrategy#getStrategyName()}.
 *
 * Spring injects all implementations of a given strategy type into the constructor
 * as a List. The registry indexes them so callers never write a switch/if-else.
 *
 * Usage:
 * <pre>
 *   // 1. Define a typed registry as a Spring bean
 *   {@literal @}Bean
 *   public StrategyRegistry{@literal <NotificationInput, Void>} notificationRegistry(
 *           List{@literal <ProcessingStrategy<NotificationInput, Void>>} strategies) {
 *       return new StrategyRegistry<>(strategies);
 *   }
 *
 *   // 2. Inject and resolve at runtime
 *   ProcessingStrategy{@literal <NotificationInput, Void>} strategy =
 *           notificationRegistry.resolve(channel.name());
 *   strategy.process(input);
 * </pre>
 *
 * @param <T> strategy input type
 * @param <R> strategy output type
 */
public class StrategyRegistry<T, R> {

    private final Map<String, ProcessingStrategy<T, R>> strategies;

    public StrategyRegistry(List<ProcessingStrategy<T, R>> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        s -> s.getStrategyName().toUpperCase(),
                        Function.identity()
                ));
    }

    /**
     * Resolve a strategy by name (case-insensitive).
     *
     * @param name strategy name, e.g. "EMAIL", "SMS"
     * @return the matching strategy
     * @throws BusinessException if no strategy is registered for that name
     */
    public ProcessingStrategy<T, R> resolve(String name) {
        ProcessingStrategy<T, R> strategy = strategies.get(name.toUpperCase());
        if (strategy == null) {
            throw new BusinessException("No strategy registered for: " + name);
        }
        return strategy;
    }

    /**
     * Check whether a strategy is registered without throwing.
     */
    public boolean supports(String name) {
        return strategies.containsKey(name.toUpperCase());
    }
}
