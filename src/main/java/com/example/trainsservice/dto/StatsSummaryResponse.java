package com.example.trainsservice.dto;

/**
 * Агрегированная сводка по залогированным подходам (таблица {@code progress}).
 */
public record StatsSummaryResponse(long loggedSetsCount, double totalVolume) {}
