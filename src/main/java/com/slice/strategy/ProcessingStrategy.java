package com.slice.strategy;

/**
 * STRATEGY PATTERN — Pluggable algorithm interface.
 *
 * Use this during the interview to swap business logic at runtime without
 * changing the calling code. Classic examples:
 *
 *  ┌──────────────────────────────────────────────────────────────┐
 *  │  Problem               │  Strategy variants                  │
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  Notification System   │  EmailStrategy, SmsStrategy,        │
 *  │                        │  PushNotificationStrategy           │
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  Parking Lot           │  FirstFitStrategy, BestFitStrategy, │
 *  │                        │  NearestExitStrategy                │
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  Reward / Wallet       │  CashbackStrategy, PointsStrategy,  │
 *  │                        │  VoucherStrategy                    │
 *  └──────────────────────────────────────────────────────────────┘
 *
 * Usage pattern:
 * <pre>
 *   Map<String, ProcessingStrategy<Input, Output>> strategyMap = ...
 *   ProcessingStrategy<Input, Output> strategy = strategyMap.get(type);
 *   Output result = strategy.process(input);
 * </pre>
 *
 * @param <T> Input type
 * @param <R> Return/Output type
 */
public interface ProcessingStrategy<T, R> {

    /**
     * Execute the strategy on the given input.
     *
     * @param input the domain-specific input object
     * @return the processed result
     */
    R process(T input);

    /**
     * Unique identifier for this strategy.
     * Used as the key in strategy registries / Maps.
     *
     * @return strategy name (e.g., "EMAIL", "CASHBACK")
     */
    String getStrategyName();
}
