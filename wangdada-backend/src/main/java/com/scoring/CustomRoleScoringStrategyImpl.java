package com.scoring;

import cn.hutool.json.JSONUtil;
import com.common.ErrorCode;
import com.exception.ThrowUtils;
import com.model.dto.question.QuestionContentDto;
import com.model.entity.App;
import com.model.entity.ScoringResult;
import com.model.entity.UserAnswer;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 自定义测评角色类
 */
@ScoringStrategyConfig(appType = 1,scoringStrategy = 0)
public class CustomRoleScoringStrategyImpl implements ScoringStrategy {
    @Resource
    private ScoringStrategyUtils scoringStrategyUtils;

    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        HashMap<String, Integer> optionCount = new HashMap<>();
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        Long appId = app.getId();
        //1.根据appId获取题目和题目结果信息
        List<ScoringResult> scoringResults = scoringStrategyUtils.getResultListByAppId(appId);
        List<QuestionContentDto> questionContents = scoringStrategyUtils.getQuestionContentByAppId(appId);
        //2.统计用户每个选择的属性个数,如I = 10个,E = 5个
        for (int i = 0; i < choices.size(); i++) {
            QuestionContentDto questionContentDto = questionContents.get(i);
            List<QuestionContentDto.Option> options = questionContentDto.getOptions();
            for (QuestionContentDto.Option option : options) {
                if (option.getKey().equals(choices.get(i))) {
                    optionCount.put(option.getResult(), optionCount.getOrDefault(option.getResult(), 0) + 1);
                }
            }
        }
        //3.遍历每种评分结果，计算那个结果的得分更高
        int maxScore = 0;
        ScoringResult maxScoreResult = scoringResults.get(0);
        for (ScoringResult scoringResult : scoringResults) {
            String prop = scoringResult.getResultProp();
            List<String> resultProp = JSONUtil.toList(prop, String.class);
            int score = resultProp.stream().mapToInt(p -> optionCount.getOrDefault(p, 0)).sum();
            if (score > maxScore) {
                maxScore = score;
                maxScoreResult = scoringResult;
            }
        }
        //4.构造返回值，填充答案对象的属性
        return scoringStrategyUtils.packagingUserAnswer(app, maxScoreResult, choices);
    }
}
