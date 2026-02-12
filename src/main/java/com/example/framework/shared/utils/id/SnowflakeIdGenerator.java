package com.example.framework.shared.utils.id;

import java.util.Date;

/**
 * 4. 雪花算法 - 分布式ID生成
 * 【ID结构】64位
 * - 1位符号位（0）
 * - 41位时间戳（约69年）
 * - 5位数据中心ID（32个）
 * - 5位机器ID（32个）
 * - 12位序列号（4096/毫秒）
 * 【特点】
 * - 趋势递增
 * - 高性能（本地生成）
 * - 分布式唯一
 */
public class SnowflakeIdGenerator {

    // 起始时间戳（2024-01-01）
    private static final long START_TIMESTAMP = 1704067200000L;

    // 各部分位数
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    // 最大值
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 位移
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID) throw new IllegalArgumentException("workerId超出范围");
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 生成下一个ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }

        if (timestamp == lastTimestamp) {
            // 同一毫秒内，序列号+1
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 序列号用完，等待下一毫秒
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;  // 新的毫秒，序列号重置
        }

        lastTimestamp = timestamp;

        // 组装ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long last) {
        long ts = System.currentTimeMillis();
        while (ts <= last) ts = System.currentTimeMillis();
        return ts;
    }


    public static void main(String[] args) {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);

        // 生成10个ID
        for (int i = 0; i < 10; i++) {
            long id = generator.nextId();
            System.out.println("ID: " + id);
        }

        // 解析ID
        long id = generator.nextId();
        long timestamp = (id >> 22) + 1704067200000L;
        long datacenterId = (id >> 17) & 31;
        long workerId = (id >> 12) & 31;
        long sequence = id & 4095;

        System.out.println("\n解析ID: " + id);
        System.out.println("时间戳: " + new Date(timestamp));
        System.out.println("数据中心: " + datacenterId);
        System.out.println("机器ID: " + workerId);
        System.out.println("序列号: " + sequence);
    }
}
