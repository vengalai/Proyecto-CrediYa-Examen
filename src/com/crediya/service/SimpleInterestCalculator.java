package com.crediya.service;

import com.crediya.util.MoneyUtil;

/** Simple interest: interest = principal x (monthly rate / 100) x months. */
public class SimpleInterestCalculator implements InterestCalculator {
    @Override
    public double calculateInterest(double principal, double monthlyRatePercent, int months) {
        return MoneyUtil.round2(principal * (monthlyRatePercent / 100.0) * months);
    }
}
