package main.java;

public class SyncDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("===== 场景 1：没有 synchronized (危险！由于并发导致重复扣款) =====");
        testUnsafeCall();

        System.out.println("\n\n------------------------------------------------\n");

        System.out.println("===== 场景 2：加了 synchronized (安全！第二个线程被挡在门外) =====");
        testSafeCall();
    }

    // ---------------------------------------------------------------
    // 1. 这是一个【不安全】的任务类 (模拟 RealCall)
    // ---------------------------------------------------------------
    static class UnsafeCall {
        private boolean executed = false; // 标记是否执行过

        public void execute(String threadName) {
            System.out.println("[" + threadName + "] 正在检查 executed 状态...");

            // ⚠️ 危险区域：没有锁！
            if (executed) {
                System.out.println("❌ [" + threadName + "] 发现任务已执行，抛出异常！");
                return;
            }

            // 【模拟时间差】
            // 假设线程 A 刚检查完 executed=false，还没来得及改状态，
            // 突然 CPU 切到了线程 B，线程 B 也进来了。
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }

            // 标记为已执行
            executed = true;

            // 执行扣款
            System.out.println("💰 [" + threadName + "] 检查通过 -> 扣款 100 元！");
        }
    }

    // ---------------------------------------------------------------
    // 2. 这是一个【安全】的任务类 (加了 synchronized)
    // ---------------------------------------------------------------
    static class SafeCall {
        private boolean executed = false;

        public void execute(String threadName) {
            System.out.println("[" + threadName + "] 尝试获取锁...");

            // 🔒 加锁！(核心代码)
            // 这一块代码变成了“原子操作”，同一时间只能有一个线程进来
            synchronized (this) {
                System.out.println("[" + threadName + "] 拿到锁了！正在检查状态...");

                if (executed) {
                    System.out.println("✅ [" + threadName + "] 发现任务已执行，拦截成功！(抛出异常)");
                    return;
                }

                // 【模拟时间差】
                // 即使在这里睡上一觉，因为锁还没释放，别的线程也进不来
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }

                executed = true;
            } // 🔓 此时释放锁

            // 执行扣款
            System.out.println("💰 [" + threadName + "] 检查通过 -> 扣款 100 元！");
        }
    }

    // ================== 测试辅助方法 ==================

    private static void testUnsafeCall() throws InterruptedException {
        // 创建一个任务对象 (同一个 Call)
        UnsafeCall call = new UnsafeCall();

        // 创建两个线程，同时去执行这个 call
        Thread threadA = new Thread(() -> call.execute("线程A"));
        Thread threadB = new Thread(() -> call.execute("线程B"));

        threadA.start();
        threadB.start();

        // 等待两个线程跑完
        threadA.join();
        threadB.join();
    }

    private static void testSafeCall() throws InterruptedException {
        // 创建一个任务对象
        SafeCall call = new SafeCall();

        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                call.execute("线程A");
            }
        });
        Thread threadB = new Thread(() -> call.execute("线程B"));

        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();
    }
}