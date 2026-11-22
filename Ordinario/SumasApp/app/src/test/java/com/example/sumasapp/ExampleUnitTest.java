package com.example.sumasapp;

import org.junit.Test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.MathContext;

public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void addition_bigDecimalPrecision() {
        BigDecimal n1 = new BigDecimal("0.1");
        BigDecimal n2 = new BigDecimal("0.2");
        BigDecimal sum = n1.add(n2, MathContext.DECIMAL128);
        assertEquals("0.3", sum.stripTrailingZeros().toPlainString());
    }
}
