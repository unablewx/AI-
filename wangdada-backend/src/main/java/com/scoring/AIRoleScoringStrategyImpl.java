package com.scoring;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.ErrorCode;
import com.constant.AIPrompt;
import com.exception.ThrowUtils;
import com.manager.AiManager;
import com.model.dto.question.QuestionAnswerDto;
import com.model.dto.question.QuestionContentDto;
import com.model.entity.App;
import com.model.entity.ScoringResult;
import com.model.entity.UserAnswer;
import com.utils.CacheUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI测评角色类
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
public class AIRoleScoringStrategyImpl implements ScoringStrategy {
    @Resource
    private ScoringStrategyUtils scoringStrategyUtils;

    @Resource
    private AiManager aiManager;

    @Resource
    private CacheUtils cacheUtils;

    @Resource
    private RedissonClient redissonClient;

    //分布式锁的key
    private static final String AI_ANSWER_KEY = "AI_ANSWER_KEY";

    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        List<QuestionAnswerDto> userAnswers = new ArrayList<>();
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        Long appId = app.getId();
        //1.查缓存
        String cacheKey = cacheUtils.buildCacheKey(String.valueOf(appId), JSONUtil.toJsonStr(choices));
        String cacheValue = cacheUtils.getAnswerCacheMap().getIfPresent(cacheKey);
        if (StrUtil.isNotBlank(cacheValue)) {
            ScoringResult scoringResult = JSONUtil.toBean(cacheValue, ScoringResult.class);
            return scoringStrategyUtils.packagingUserAnswer(app, scoringResult, choices);
        }

        RLock clientLock = redissonClient.getLock(AI_ANSWER_KEY + cacheKey);
        try {
            //尝试获取锁
            boolean res = clientLock.tryLock(3, 15, TimeUnit.SECONDS);
            if (!res) {
                return null;
            }
            //2.1 根据appId获取题目
            List<QuestionContentDto> questionContents = scoringStrategyUtils.getQuestionContentByAppId(appId);
            //2.2 拼接用户答案(题目-选项 例如你所在的省是？-广东)
            for (int i = 0; i < choices.size(); i++) {
                QuestionAnswerDto answerDto = new QuestionAnswerDto();
                QuestionContentDto questionContentDto = questionContents.get(i);
                List<QuestionContentDto.Option> options = questionContentDto.getOptions();
                for (QuestionContentDto.Option option : options) {
                    if (option.getKey().equals(choices.get(i))) {
                        answerDto.setTitle(questionContentDto.getTitle());
                        answerDto.setUserAnswer(option.getValue());
                        break;
                    }
                }
                userAnswers.add(answerDto);
            }
            String userAnswersJson = JSONUtil.toJsonStr(userAnswers);
            //2.3 AI分析评测
            String systemMessage = AIPrompt.ANALYZE_ANSWER_SYSTEM_MESSAGE;
            String userMessage = getGenerateUserAnswerMessage(app, userAnswersJson);
            String result = aiManager.doSyncStableRequest(systemMessage, userMessage);
            //2.4 构造返回值，填充答案对象的属性
            int start = result.indexOf("{");
            int end = result.lastIndexOf("}");
            String json = result.substring(start, end + 1);
            ScoringResult scoringResult = JSONUtil.toBean(json, ScoringResult.class);
            //2.5 将ai返回的结果放回缓存
            cacheUtils.getAnswerCacheMap().put(cacheKey, json);
            //2.6 返回用户答案
            return scoringStrategyUtils.packagingUserAnswer(app, scoringResult, choices);
        } finally {
            if (clientLock != null && clientLock.isLocked()) {
                if (clientLock.isHeldByCurrentThread()) {
                    clientLock.unlock();
                }
            }
        }
    }

    /**
     * 生成题目的用户消息
     *
     * @param app
     * @param userAnswers
     * @return
     */
    private String getGenerateUserAnswerMessage(App app, String userAnswers) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        userMessage.append(userAnswers).append("\n");
        return userMessage.toString();
    }
}
