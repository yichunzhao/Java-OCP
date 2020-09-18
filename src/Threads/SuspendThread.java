package Threads;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Demo to create a blocking Queue.
 *
 * <p>
 * to access a protected resource, a thread need to achieve its monitor first.
 * <p>
 * one object has a monitor; once object holds his monitor, all external threads that intend to access the object, then
 * have to suspend their actions; until the object release the monitor by sending a notification, i.e. object.notify().
 * <p>
 * Object methods
 * notify(): wake up threads that are waiting for this object's monitor.
 * wait(): causes current thread that own the object monitor to give up the monitor;
 */
class Event {
    private String title;

    public Event(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                '}';
    }
}

class EventQueue {
    public static final int qSize = 20;
    private Queue<Event> q = new ArrayDeque<>(qSize);

    synchronized public void insert(Event e) throws InterruptedException {
        //if the queue has a certain amount of elements, then allowing threads to achieve the monitor.
        while (q.size() > 20) wait();
        q.offer(e);
    }

    synchronized public Event fetch() throws InterruptedException {
        //if queue is empty, threads waiting
        while (q.isEmpty()) notifyAll();

        return q.poll();
    }

}

class Consumer implements Runnable {
    private EventQueue dataStore;
    private int sequence;

    public Consumer(EventQueue dataStore, int sequence) {
        this.dataStore = dataStore;
        this.sequence = sequence;
    }

    @Override
    public void run() {
        while (true) {
            Event x = null;
            try {
                x = dataStore.fetch();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Consumer " + " i :" + Thread.currentThread().getName() + " event: " + x);

        }
    }
}

class Producer implements Runnable {
    private EventQueue dataStore;
    private String title = "producer: ";

    public Producer(EventQueue dataStore, int sequence) {
        this.dataStore = dataStore;
        this.title += sequence;
    }


    @Override
    public void run() {
        try {
            dataStore.insert(new Event(this.title));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class SuspendThread {

    public static void main(String[] args) throws InterruptedException {
        //event queue
        EventQueue queue = new EventQueue();

        //a thread pool to carry out tasks
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        //commit producer tasks into queue
        IntStream.rangeClosed(0, 12).forEach(i -> executorService.submit(new Producer(queue, i)));

        //commit consumer tasks into queue
        IntStream.range(0, 4).forEach(i -> executorService.submit(new Consumer(queue, i)));

        //wait executor until it shuts down
        executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        executorService.shutdown();
        System.out.println("executor service shut down? " + executorService.isShutdown());
        //quit from system
        System.exit(1);
    }

}
