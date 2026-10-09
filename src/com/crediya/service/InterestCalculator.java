package com.crediya.service;

/** Strategy pattern: new interest formulas can be added without touching LoanService (Open/Closed). */
public interface InterestCalculator {
    /** Total interest for the whole loan. */
    double calculateInterest(double principal, double monthlyRatePercent, int months);
}
