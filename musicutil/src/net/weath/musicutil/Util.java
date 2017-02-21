package net.weath.musicutil;

import java.util.ArrayList;

public class Util {

    /**
     * Return an ArrayList of the prime factors of the given int
     * @param n the int to factor
     * @return ArrayList<Integer> of its factors
     */
    public static ArrayList<Integer> factor(int n) {
        ArrayList<Integer> list = new ArrayList<>();
        int max = (int) Math.ceil(Math.sqrt(n));
        for (int i = 2; i <= max; i++) {
            if (i > 3 && factor(i).size() > 1) {
                continue; // not prime
            }
            if (n % i == 0) {
                list.add(i);
                ArrayList<Integer> other = factor(n / i);
                for (int o : other) {
                    if (o != 1) {
                        list.add(o);
                    }
                }
                break;
            }
        }
        if (list.isEmpty()) {
            list.add(n);
        }
        return list;
    }

    /**
     * Return the Greatest Common Divisor of a and b
     * @param a
     * @param b
     * @return the GCD of a and b
     */
    public static int gcd(int a, int b) {
        while (b > 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Return the Least Common Multiple of a and b
     * @param a
     * @param b
     * @return the LCM of a and b
     */
    public static int lcm(int a, int b) {
        return a * (b / gcd(a, b));
    }

    /**
     * Return the Least Common Multiple of all the Integers in the array
     * @param arr Integer[]
     * @return the LCM of all the Integers in the array
     */
    public static int lcm(Integer[] arr) {
        int result = arr[0];
        for (int i = 1; i < arr.length; i++) {
            result = lcm(result, arr[i]);
        }
        return result;
    }

}
