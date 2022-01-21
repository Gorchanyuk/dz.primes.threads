package AdvancedJava;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final int N = 1_000_000_000;
    private static final int sqrtN = (int) Math.sqrt(N);
    private static final Set<Integer> firstPrimes = new HashSet<>();
    private static final Set<Integer> allPrimes = new ConcurrentSkipListSet<>();
    private static final int cache = 6000;
    private static final AtomicInteger size = new AtomicInteger(0);

    public static void createFirstPrimes() {
        boolean[] isPrime = new boolean[sqrtN];
        Arrays.fill(isPrime, true);
        for (int i = 2; i < sqrtN; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j < sqrtN; j += i) {
                    isPrime[j] = false;
                }
            }
        }
        for (int i = 2; i < sqrtN; i++) {
            if (isPrime[i])
                firstPrimes.add(i);
        }
//        allPrimes.addAll(firstPrimes);
        size.addAndGet(firstPrimes.size());
    }


    public static void main(String[] args) throws InterruptedException {
        long startProgram = System.currentTimeMillis();
        createFirstPrimes();
        ExecutorService executorService = Executors.newFixedThreadPool(8);

        for (int start = sqrtN; start < N; start += cache) {
            int localStart = start;

            executorService.submit(() ->
            {
                Set<Integer> localAllPrimes = new HashSet<>();
                boolean[] segment = new boolean[cache];
                Arrays.fill(segment, true);
                for (Integer firstPrime : firstPrimes) {
                    int h = localStart % firstPrime;
                    int j = h > 0 ? firstPrime - h : 0;
                    for (; j < cache; j += firstPrime)
                        segment[j] = false;
                }
                for (int i = 0; i < cache; i++) {
                    if (segment[i] && (i + localStart < N)) {
                        localAllPrimes.add(i + localStart);
                    }
                }
//                allPrimes.addAll(localAllPrimes);
                size.addAndGet(localAllPrimes.size());
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);

        long endProgram = System.currentTimeMillis();

//        System.out.println(allPrimes.size());
        System.out.println(size);
        System.out.println("Время выполнения: " + (endProgram - startProgram));
    }
}