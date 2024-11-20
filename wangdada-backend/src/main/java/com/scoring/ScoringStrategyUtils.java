package com.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.ErrorCode;
import com.exception.ThrowUtils;
import com.model.dto.question.QuestionContentDto;
import com.model.entity.App;
import com.model.entity.Question;
import com.model.entity.ScoringResult;
import com.model.entity.UserAnswer;
import com.service.QuestionService;
import com.service.ScoringResultService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ScoringStrategyUtils {
    @Resource
    private QuestionService questionService;
    @Resource
    private ScoringResultService scoringResultService;

    /**
     * 根据appId获取题目内容
     * @param appId
     * @return
     * @throws Exception
     */
    public List<QuestionContentDto> getQuestionContentByAppId(Long appId) throws Exception {
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR, "所选应用项目题目不存在");
        String content = question.getQuestionContent();
        List<QuestionContentDto> questionContents = JSONUtil.toList(content, QuestionContentDto.class);
        ThrowUtils.throwIf(CollectionUtils.isEmpty(questionContents), ErrorCode.PARAMS_ERROR, "所选应用项目题目内容不存在");
        return questionContents;
    }

    /**
     * 根据appId获取题目结果
     * @param appId
     * @return
     * @throws Exception
     */
    public List<ScoringResult> getResultListByAppId(Long appId) throws Exception {
        List<ScoringResult> scoringResults = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class).eq(ScoringResult::getAppId, appId)
        );
        ThrowUtils.throwIf(CollectionUtils.isEmpty(scoringResults), ErrorCode.PARAMS_ERROR, "所选应用项目题目结果不存在");
        return scoringResults;
    }

    /**
     * 封装对象
     */
    public UserAnswer packagingUserAnswer(App app, ScoringResult scoringResult,List<String> choices) {
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        if (ObjectUtils.isNotEmpty(scoringResult.getId())){
            userAnswer.setResultId(scoringResult.getId());
        }
        userAnswer.setResultName(scoringResult.getResultName());
        userAnswer.setResultDesc(scoringResult.getResultDesc());
        if (StringUtils.isNotEmpty(scoringResult.getResultPicture())){
            userAnswer.setResultPicture(scoringResult.getResultPicture());
        }
        return userAnswer;
    }
}
