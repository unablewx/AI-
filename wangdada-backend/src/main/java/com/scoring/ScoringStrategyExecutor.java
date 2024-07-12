package com.scoring;

import com.common.ErrorCode;
import com.exception.BusinessException;
import com.model.entity.App;
import com.model.entity.UserAnswer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ScoringStrategyExecutor {
    @Resource
    private List<ScoringStrategy> scoringStrategyList;

    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        Integer appScoringStrategy = app.getScoringStrategy();
        Integer appType = app.getAppType();
        if (appScoringStrategy == null || appType == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误,未找到适配的策略");
        }
        for (ScoringStrategy strategy : scoringStrategyList) {
            if (strategy.getClass().isAnnotationPresent(ScoringStrategyConfig.class)) {
                ScoringStrategyConfig config = strategy.getClass().getAnnotation(ScoringStrategyConfig.class);
                if (config.appType() == appType && config.scoringStrategy() == appScoringStrategy) {
                    return strategy.doScore(choices, app);
                }
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误,未找到适配的策略");
    }
}
