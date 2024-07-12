package com.scoring;

import com.common.ErrorCode;
import com.exception.ThrowUtils;
import com.model.dto.question.QuestionContentDto;
import com.model.entity.App;
import com.model.entity.ScoringResult;
import com.model.entity.UserAnswer;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

/**
 * 角色测评类
 */
@ScoringStrategyConfig(appType = 0,scoringStrategy = 0)
public class CustomScoreScoringStrategyImpl implements ScoringStrategy {
    @Resource
    private ScoringStrategyUtils scoringStrategyUtils;

    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        int score = 0;
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        Long appId = app.getId();
        //1.根据appId获取题目和题目结果信息
        List<ScoringResult> scoringResults = scoringStrategyUtils.getResultListByAppId(appId);
        scoringResults.sort(Comparator.comparing(ScoringResult::getResultScoreRange).reversed());
        List<QuestionContentDto> questionContents = scoringStrategyUtils.getQuestionContentByAppId(appId);
        //2.统计用户得分分数
        for (int i = 0; i < choices.size(); i++) {
            QuestionContentDto questionContentDto = questionContents.get(i);
            List<QuestionContentDto.Option> options = questionContentDto.getOptions();
            for (QuestionContentDto.Option option : options) {
                if (option.getKey().equals(choices.get(i))) {
                    score += option.getScore();
                }
            }
        }
        //3.遍历每种评分结果，计算最后的得分值
        ScoringResult maxScoreResult = scoringResults.get(0);
        for (ScoringResult scoringResult : scoringResults) {
            if (score >= scoringResult.getResultScoreRange()) {
                maxScoreResult = scoringResult;
                break;
            }
        }
        //4.构造返回值，填充答案对象的属性
        UserAnswer userAnswer = scoringStrategyUtils.packagingUserAnswer(app, maxScoreResult, choices);
        userAnswer.setResultScore(score);
        return userAnswer;
    }
}
