package WorkStealingThreadPool;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.Random;

/**
 * represents a work stealing thread pool - to understand what this class does
 * please refer to your assignment.
 */
public class WorkStealingThreadPool {
    protected VersionMonitor vm;
    private int nthreads;
    protected ConcurrentLinkedDeque<Task>[] queueArr;
    protected Processor[] processorsArr;
    protected Thread[] threadArr;

    /**
     * creates a {@link WorkStealingThreadPool} which has nthreads
     * {@link Processor}s. Note, threads should not get started until calling to
     * the {@link #start()} method.
     * @param nthreads the number of threads that should be started by this
     *                 thread pool
     */
    public WorkStealingThreadPool(int nthreads) {
        vm = new VersionMonitor();
        this.nthreads = nthreads;
        queueArr = new ConcurrentLinkedDeque[nthreads];
        processorsArr = new Processor[nthreads];
        threadArr = new Thread[nthreads];
        for (int i = 0; i < nthreads; i++) {
            processorsArr[i] = new Processor(i, this);
            threadArr[i] = new Thread(processorsArr[i]);
            queueArr[i] = new ConcurrentLinkedDeque<Task>();
        }
    }

    /**
     * submits a task to be executed by a processor belongs to this thread pool
     *
     * @param task the task to execute
     */
    public void submit(Task<?> task) {
        Random rand = new Random();
        int n = rand.nextInt(nthreads);
        queueArr[n].addFirst(task);
        vm.inc();
    }

    protected void submitTo(Task<?> task, int process) {
        queueArr[process].addFirst(task);
        vm.inc();
    }

    protected void submitToEnd(Task<?> task, int process) {
        queueArr[process].addLast(task);
        vm.inc();
    }

    /**
     * closes the thread pool - this method interrupts all the threads and wait
     * for them to stop - it is returns *only* when there are no live threads in
     * the queue.
     * @throws InterruptedException          if the thread that shut down the threads is
     *                                       interrupted
     * @throws UnsupportedOperationException if the thread that attempts to
     *                                       shutdown the queue is itself a processor of this queue
     */
    public void shutdown() throws InterruptedException {
        int i = 0;
        for (Thread t : threadArr) {
            processorsArr[i].running = false;
            i++;
            if (t != Thread.currentThread()) {
                t.interrupt();
            }
        }
    }

    protected int getAmount() {
        return this.nthreads;
    }

    /**
     * start the threads belongs to this thread pool
     */
    public void start() {
        for (int i = 0; i < nthreads; i++) {
            this.threadArr[i].start();
        }
    }
}
