package com.utils;

import cn.hutool.crypto.digest.DigestUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Data
public class CacheUtils {

    //本地缓存初始化
    public final Cache<String,String> answerCacheMap =Caffeine.newBuilder().initialCapacity(1024)
            //缓存5分钟移除
            .expireAfterAccess( 5L, TimeUnit.MINUTES)
            .build();

    /**
     * 拼接本地缓存 key
     * @param appId 应用id
     * @param choiceJson 用户选项Json
     * @return 缓存key
     *
     * 用appId拼接json的md5值作为key好处是 当某个应用的题目改变后，可以把相关的缓存全部清除
     */
    public String buildCacheKey(String appId,String choiceJson) {
        return appId+":"+ DigestUtil.md5Hex(choiceJson);
    }

}
