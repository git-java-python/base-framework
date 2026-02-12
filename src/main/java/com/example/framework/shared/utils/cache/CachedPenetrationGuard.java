package com.example.framework.shared.utils.cache;

import com.example.framework.service.CalciteQueryService;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * 防止缓存穿透
 * 生产环境误判率仅为0.01%
 */
@Service
public class CachedPenetrationGuard {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CalciteQueryService calciteQueryService;

    // 布隆过滤器（Guava 实现，生产建议用 Redisson 分布式布隆过滤器）
    private BloomFilter<Long> bloomFilter = BloomFilter.
            create(Funnels.longFunnel(), // 预期元素数量 1000万
                    100, 0.0001); // 误判率 0.01%

    @PostConstruct
    public void init() {
        List<Long> query = calciteQueryService.queryUserIds();

        query.forEach(bloomFilter::put);
    }


    public Map<String, Object> queryProduct(Long productId) {

        //第一层  布隆过滤器判断存不存在
        if (!bloomFilter.mightContain(productId)) {

        }


        return null;
    }
}
