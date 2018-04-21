package com.example.heartx.assistant.util.adaptive;

import java.math.BigDecimal;

/**
 * 用于解决浮点数运算精度问题
 * BigDecimal类的封装工具类
 * Created by HeartX on 2017/12/15.
 */

public class BigDecimalUtil {

    public static double add(double a, double b){

        BigDecimal b1 = new BigDecimal(a);
        BigDecimal b2 = new BigDecimal(b);
        BigDecimal value = b1.add(b2);

        return value.doubleValue();
    }

    public static double subtract(double minuend, double subtrahend){

        BigDecimal b1 = new BigDecimal(minuend);
        BigDecimal b2 = new BigDecimal(subtrahend);
        BigDecimal value = b1.subtract(b2);

        return value.doubleValue();
    }

    public static double multiply(double a, double b){

        BigDecimal b1 = new BigDecimal(a);
        BigDecimal b2 = new BigDecimal(b);
        BigDecimal value = b1.multiply(b2);

        return value.doubleValue();
    }

    public static double divide(double dividend, double divisor){

        BigDecimal b1 = new BigDecimal(dividend);
        BigDecimal b2 = new BigDecimal(divisor);
        BigDecimal value = b1.divide(b2, 3, BigDecimal.ROUND_UP);

        return value.doubleValue();
    }

}
