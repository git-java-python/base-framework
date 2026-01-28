package com.example.framework.utils.cache;

import com.google.common.hash.BloomFilter;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 7. 缓存策略 - 穿透/击穿/雪崩
 *
 * 【缓存穿透】
 * - 问题：查询不存在的数据，每次都打到DB
 * - 方案：布隆过滤器 + 空值缓存
 *
 * 【缓存击穿】
 * - 问题：热点key过期，大量请求打到DB
 * - 方案：互斥锁 + 永不过期
 *
 * 【缓存雪崩】
 * - 问题：大量key同时过期
 * - 方案：随机过期时间 + 集群
 */
public class CacheService {

    @Resource
    private StringRedisTemplate redis;
    @Resource
    private BloomFilter<String> bloomFilter;
}
