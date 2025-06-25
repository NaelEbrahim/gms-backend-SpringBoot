package com.graduation.GMS.Tools;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
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

    public static String generateQRCode(String text) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 100, 100);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        return Base64.getEncoder().encodeToString(pngData);
    }


}
