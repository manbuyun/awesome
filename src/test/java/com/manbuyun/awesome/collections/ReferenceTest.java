package com.manbuyun.awesome.collections;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableWeakReference;
import com.manbuyun.awesome.common.Sleeper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * User: cs
 * Date: 2018-04-30
 */
@Slf4j
public class ReferenceTest {

    @Test
    public void concurrentWeakReference() {
        ConcurrentReferenceHashMap<String, String> map = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);
//        ConcurrentMap<String, String> map = new MapMaker().weakValues().makeMap();
        map.put("key", "value");
        log.info("map: {}", map);

        System.gc();

        Sleeper.sleepForQuietly(3, TimeUnit.SECONDS);
        log.info("map: {}", map);
    }

    @Test
    public void concurrentSoftReference() {
        ConcurrentReferenceHashMap<String, String> map = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.SOFT);
        map.put("key", "value");
        log.info("map: {}", map);

        System.gc();

        Sleeper.sleepForQuietly(3, TimeUnit.SECONDS);
        log.info("map: {}", map);
    }

    @Test
    public void weakReference() throws InterruptedException {
        ReferenceQueue<String> queue = new ReferenceQueue();

        // 这里必须要new String(); 字符串会直接加入常量池，不会被gc回收
        String name = new String("hi");

        // r1弱引用于对象name，也就是name对象有个弱引用对象r1。r1属于弱引用对象，name是强引用对象(引用于"jack")
        // 同时r1和queue关联，也就是当r1的引用对象name生命周期结束(没有别的对象的强引用)，被gc回收后，r1这个弱引用对象会被加入到queue，等待被回收
        WeakReference<String> r1 = new WeakReference<>(name, queue);

        // 继承WeakReference，被回收时可以获取额外信息，比如这里的id值(因为只有name是referent)
        Model r2 = new Model(1, name, queue);

        // 模拟name生命周期结束
        name = null;

        long startTime = System.currentTimeMillis();
        System.gc();
        // r1.get()返回referent，也就是name对象，当name对象被gc回收后，返回null
        // 因为System.gc()不一定会触发gc回收，所以验证r1.get()方法的返回值，就能判断name有没有被gc
        while (r1.get() != null && System.currentTimeMillis() - startTime < 10000) {
            System.gc();
            Sleeper.sleepForQuietly(3, TimeUnit.MILLISECONDS);
        }

        // 这里的r11就是弱引用r1对象，r11对象本身还没被回收，所以不为null，但调用r11.get()会返回null
        // queue.remove()会阻塞直到获取到Reference。queue.poll()可能会返回null，也就是当gc后，弱引用对象还没放到queue里，此时执行poll()会返回null
        // remove()不需要null判定，除非加了超时时间remove(200)。poll()需要null判定
        Reference<? extends String> r11 = queue.remove();

        // 这里的r21就是弱引用r2对象，同样的，r21对象本身还没被回收，不为null；r21.get()返回null
        Reference<? extends String> r21 = queue.remove();

        // 可以强转，因为r21的本质是Mode类，父类分别为WeakReference、Reference
        Model model = (Model) r21;

        // model.getName()返回null，但id会返回1。因为只有name是referent，和queue关联
        // 利用这个特性，可以在强引用对象上(比如name)，加一个继承于WeakReference，并传递标识信息(比如Model类以及属性id)
        // 当强引用对象被回收时，可以触发WeakReference的机制，然后执行额外的清理工作(gc只回收强引用对象的内存)
        System.out.println(model.getId());
        System.out.println(model.getName());
    }

    @Test
    public void finalizableReference() {
        String name = new String("hi");
        int id = 1;

        // 触发FinalizableReferenceQueue类的静态代码块，也就是调用guava的Finalizer.startFinalizer(): 新建Thread，run()循环执行queue.remove()，获取弱引用对象，并执行finalizeReferent方法
        FinalizableReferenceQueue queue = new FinalizableReferenceQueue();
        Reference<String> r1 = new FinalizableWeakReference<String>(name, queue) {
            public void finalizeReferent() {
                // 闭包实现，当name对象被回收时，获取到业务id，触发额外清理工作
                System.out.println("id: " + id);
            }
        };

        name = null;

        long startTime = System.currentTimeMillis();
        System.gc();
        while (r1.get() != null && System.currentTimeMillis() - startTime < 10000) {
            System.gc();
            Sleeper.sleepForQuietly(3, TimeUnit.MILLISECONDS);
        }

        Sleeper.sleepForQuietly(1, TimeUnit.SECONDS);
    }

    private static class Model extends WeakReference<String> {
        @Getter
        private int id;
        @Getter
        private String name;

        public Model(int id, String name, ReferenceQueue<String> queue) {
            super(name, queue);
            this.id = id;
        }
    }
}
