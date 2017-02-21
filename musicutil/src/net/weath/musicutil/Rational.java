package net.weath.musicutil;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Rational {

    private int d;
    private int n;

    public int getD() {
        return d;
    }

    public int getN() {
        return n;
    }

    public Rational(int n, int d) {
        this.n = n;
        this.d = d;
        simplify();
    }

    public Rational(double d) {
        d *= 10000;
        this.n = (int) d;
        this.d = 10000;
        simplify();
    }

    public Rational(String s) {
        StringTokenizer st = new StringTokenizer(s, " :/");
        if (st.countTokens() != 2) {
            throw new IllegalArgumentException("expected two ints, separated by spaces, :, or /");
        }
        n = Integer.parseInt(st.nextToken());
        d = Integer.parseInt(st.nextToken());
        simplify();
    }

    private void simplify() {
        // reduce to simplest terms
//		System.err.print("simplify: " + n + "/" + d);
        List<Integer> nFactors = factor(n);
        List<Integer> dFactors = factor(d);
        List<Integer> nFactorsCopy = new ArrayList<>(nFactors);
        for (int i : nFactorsCopy) {
            if (dFactors.contains(i)) {
                nFactors.remove(nFactors.indexOf(i));
                dFactors.remove(dFactors.indexOf(i));
            }
        }
        List<Integer> dFactorsCopy = new ArrayList<>(dFactors);
        for (int i : dFactorsCopy) {
            if (nFactors.contains(i)) {
                nFactors.remove(nFactors.indexOf(i));
                dFactors.remove(dFactors.indexOf(i));
            }
        }
        n = 1;
        d = 1;
        for (int i : nFactors) {
            n *= i;
        }
        for (int i : dFactors) {
            d *= i;
        }
//	System.err.println(" ==> " + n + "/" + d);
    }

    private static List<Integer> factor(int n) {
        ArrayList<Integer> list = new ArrayList<>();
        generatePrimes(n);
        for (int p : primes) {
            while (n % p == 0) {
                list.add(p);
                n /= p;
            }
        }
        list.add(n);
        return list;
    }

    private static final ArrayList<Integer> primes = new ArrayList<>();

    private static int lastPrime = 0;

    static {
        primes.add(2);
        primes.add(3);
        primes.add(5);
        primes.add(7);
        lastPrime = 7;
        // accumulate the rest dynamically
    }

    private static void generatePrimes(int n) {
        for (int i = lastPrime + 1; i <= n; i++) {
            boolean isPrime = true;
            ArrayList<Integer> copy = new ArrayList<>(primes);
            for (int p : copy) {
                if (i % p == 0) {
                    isPrime = false;
                    break;
                }
            }
            if (isPrime) {
                primes.add(i);
                lastPrime = i;
            }
        }
    }

    @Override
    public String toString() {
        return "" + n + "/" + d;
    }

    public static void main(String[] args) {
        for (String s : args) {
            Rational r = new Rational(s);
            System.out.println(s + " --> " + r);
        }
    }

    public double asDouble() {
        return (double) n / (double) d;
    }

    public boolean isUnity() {
        return n == 1 && d == 1;
    }

    public double times(double f) {
        return (f * n) / (double) d;
    }

}
