package com.graduation.GMS.Tools;

import java.util.Random;

public class Generators {

    public static String generatePassword() {
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String special = "@#$&*";
        Random random = new Random();
        char[] password = new char[8];
        password[0] = lower.charAt(random.nextInt(lower.length()));
        password[1] = upper.charAt(random.nextInt(upper.length()));
        password[2] = digits.charAt(random.nextInt(digits.length()));
        password[3] = special.charAt(random.nextInt(special.length()));
        String all = lower + upper + digits + special;
        for (int i = 4; i < 8; i++) {
            password[i] = all.charAt(random.nextInt(all.length()));
        }
        for (int i = 0; i < password.length; i++) {
            int j = random.nextInt(password.length);
            char temp = password[i];
            password[i] = password[j];
            password[j] = temp;
        }
        return new String(password);
    }


}
