package com.graduation.GMS.Tools;

public class BMI_Calculator {

    public static Float calculateBMI(Float weightKg, Float heightCm) {
        if (weightKg == 0.0f && heightCm == 0.0f) return 0.0f;
        return weightKg / ((heightCm / 100) * (heightCm / 100));
    }

    public static String calculateBMIStatus(Float weightKg, Float heightCm) {
        float bmi = calculateBMI(weightKg, heightCm);
        if (bmi == 0.0f) return "--";
        if (bmi < 18.5f) return "Weight loss";
        if (bmi < 25f) return "Normal weight";
        if (bmi < 30f) return "Weight gain";
        return "Corpulence";
    }

}
