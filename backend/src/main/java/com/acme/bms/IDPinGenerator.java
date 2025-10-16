package com.acme.bms;

import java.util.Random;

public class IDPinGenerator {
    public static String generateID() {
        int length = 8;
        String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder ();
        Random random = new Random ();
        for (int i = 0; i < length; i ++) {
            sb.append (candidateChars.charAt (random.nextInt (candidateChars
                    .length ())));
        }

        return sb.toString ();
    } 
    
    public static int generatePin() {
        Random rand = new Random();
        return rand.nextInt(1000, 10000);
    }
}
