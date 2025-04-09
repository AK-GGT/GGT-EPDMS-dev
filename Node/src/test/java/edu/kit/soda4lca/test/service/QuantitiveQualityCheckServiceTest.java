package edu.kit.soda4lca.test.service;

import de.iai.ilcd.service.AutomaticQualityCheckService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuantitiveQualityCheckServiceTest {


    @Test
    public void checkMaxDeviation() {
        Double negativeProcessAmount = Double.valueOf(-10);
        Double positiveProcessAmount = Double.valueOf(20);

        double largeNegative = -20d;
        double smallNegative = -5d;
        double smallPositive = 10d;
        double largePositive = 30d;

        double firstDeviationFromNegative = AutomaticQualityCheckService.calculateMaxDeviation(negativeProcessAmount, largeNegative);
        assertTrue(firstDeviationFromNegative > 0);
        assertEquals(firstDeviationFromNegative, 50, 0);

        double secondDeviationFromNegative = AutomaticQualityCheckService.calculateMaxDeviation(negativeProcessAmount, smallNegative);
        assertTrue(secondDeviationFromNegative < 0);
        assertEquals(secondDeviationFromNegative, -100, 0);

        double thirdDeviationFromNegative = AutomaticQualityCheckService.calculateMaxDeviation(negativeProcessAmount, smallPositive);
        assertTrue(thirdDeviationFromNegative < 0);
        assertTrue(thirdDeviationFromNegative < secondDeviationFromNegative);
        assertEquals(thirdDeviationFromNegative, -200, 0);

        double forthDeviationFromNegative = AutomaticQualityCheckService.calculateMaxDeviation(negativeProcessAmount, largePositive);
//		assertTrue(forthDeviationFromNegative < thirdDeviationFromNegative);
        assertEquals(forthDeviationFromNegative, -(400d) / (3d), 0);


        double firstDeviationFromPositive = AutomaticQualityCheckService.calculateMaxDeviation(positiveProcessAmount, largeNegative);
        assertTrue(firstDeviationFromPositive > 0);
        assertEquals(firstDeviationFromPositive, 200, 0);

        double secondDeviationFromPositive = AutomaticQualityCheckService.calculateMaxDeviation(positiveProcessAmount, smallNegative);
        assertTrue(secondDeviationFromPositive > firstDeviationFromPositive);
        assertEquals(secondDeviationFromPositive, 500, 0);

        double thirdDeviationFromPositive = AutomaticQualityCheckService.calculateMaxDeviation(positiveProcessAmount, smallPositive);
        assertTrue(thirdDeviationFromPositive > 0);
        assertTrue(thirdDeviationFromPositive > secondDeviationFromNegative);
        assertEquals(thirdDeviationFromPositive, 100, 0);

        double forthDeviationFromPositive = AutomaticQualityCheckService.calculateMaxDeviation(positiveProcessAmount, largePositive);
        assertTrue(forthDeviationFromPositive < 0);
        assertEquals(forthDeviationFromPositive, -(100d) / (3d), 0);
    }

    @Test
    public void checkMinDeviation() {
        Double negativeProcessAmount = Double.valueOf(-10);
        Double positiveProcessAmount = Double.valueOf(20);

        double largeNegative = -20d;
        double smallNegative = -5d;
        double smallPositive = 10d;
        double largePositive = 30d;

        double firstDeviationFromNegative = AutomaticQualityCheckService.calculateMinDeviation(negativeProcessAmount, largeNegative);
        assertTrue(firstDeviationFromNegative < 0);
        assertEquals(firstDeviationFromNegative, -50, 0);

        double secondDeviationFromNegative = AutomaticQualityCheckService.calculateMinDeviation(negativeProcessAmount, smallNegative);
        assertTrue(secondDeviationFromNegative > 0);
        assertEquals(secondDeviationFromNegative, 100, 0);

        double thirdDeviationFromNegative = AutomaticQualityCheckService.calculateMinDeviation(negativeProcessAmount, smallPositive);
        assertTrue(thirdDeviationFromNegative > 0);
        assertTrue(thirdDeviationFromNegative > secondDeviationFromNegative);
        assertEquals(thirdDeviationFromNegative, 200, 0);

        double forthDeviationFromNegative = AutomaticQualityCheckService.calculateMinDeviation(negativeProcessAmount, largePositive);
        assertTrue(forthDeviationFromNegative < thirdDeviationFromNegative);
        assertEquals(forthDeviationFromNegative, (400d / 3d), 0);


        double firstDeviationFromPositive = AutomaticQualityCheckService.calculateMinDeviation(positiveProcessAmount, largeNegative);
        assertTrue(firstDeviationFromPositive < 0);
        assertEquals(firstDeviationFromPositive, -200, 0);

        double secondDeviationFromPositive = AutomaticQualityCheckService.calculateMinDeviation(positiveProcessAmount, smallNegative);
        assertTrue(secondDeviationFromPositive < firstDeviationFromPositive);
        assertEquals(secondDeviationFromPositive, -500, 0);

        double thirdDeviationFromPositive = AutomaticQualityCheckService.calculateMinDeviation(positiveProcessAmount, smallPositive);
        assertTrue(thirdDeviationFromPositive < 0);
        assertTrue(thirdDeviationFromPositive < secondDeviationFromNegative);
        assertEquals(thirdDeviationFromPositive, -100, 0);

        double forthDeviationFromPositive = AutomaticQualityCheckService.calculateMinDeviation(positiveProcessAmount, largePositive);
        assertTrue(forthDeviationFromPositive > 0);
        assertEquals(forthDeviationFromPositive, (100d) / (3d), 0);
    }
}
