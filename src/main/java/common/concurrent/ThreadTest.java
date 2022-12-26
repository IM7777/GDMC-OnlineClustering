package common.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class ThreadTest{
    public static void main(String[] args) throws InterruptedException {
        long st = System.currentTimeMillis();
        ConcurrentHashMap<Integer, ArrayList<Integer>> map = new ConcurrentHashMap<>();
        int num = 5;
        SubThread[] threads = new SubThread[num];
        CountDownLatch countDownLatch = new CountDownLatch(num);

        for (int i = 0; i < num; i++) {
            map.put(i, new ArrayList<>());
        }

        for (int i = 0; i < num; i++) {
            threads[i] = new SubThread(map, i, countDownLatch);
            threads[i].start();
        }
        countDownLatch.await();
        long ed = System.currentTimeMillis();
        System.out.println(map.get(1).size());
        System.out.println(ed - st);

    }
}


class SubThread extends Thread{
    private int num;
    private ConcurrentHashMap<Integer, ArrayList<Integer>> mapList;
    private CountDownLatch countDownLatch;

    public SubThread(ConcurrentHashMap<Integer, ArrayList<Integer>> mapList, int num, CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
        this.num = num;
        this.mapList = mapList;
    }

    public void run() {
        //System.out.println(Thread.currentThread().getName());
        ArrayList<Integer> list = mapList.get(num);
        int count = 1;
        for (int i = 0; i < 10000; i++) {
            count = count * (i + 1);
            list.add(count);
        }
        mapList.put(num, list);
        countDownLatch.countDown();
    }
}