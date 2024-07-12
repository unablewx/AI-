package com.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.ErrorCode;
import com.constant.CommonConstant;
import com.exception.ThrowUtils;
import com.mapper.UserAnswerMapper;
import com.model.dto.useranswer.UserAnswerQueryRequest;
import com.model.entity.App;
import com.model.entity.User;
import com.model.entity.UserAnswer;
import com.model.enums.ReviewStatusEnum;
import com.model.vo.UserAnswerVO;
import com.model.vo.UserVO;
import com.service.AppService;
import com.service.UserAnswerService;
import com.service.UserService;
import com.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户答题记录服务实现
 */
@Service
@Slf4j
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer> implements UserAnswerService {

    @Resource
    private UserService userService;
    @Resource
    private AppService appService;

    /**
     * 校验数据
     *
     * @param userAnswer
     * @param add        对创建的数据进行校验
     */
    @Override
    public void validUserAnswer(UserAnswer userAnswer, boolean add) {
        ThrowUtils.throwIf(userAnswer == null, ErrorCode.PARAMS_ERROR);
        Long appId = userAnswer.getAppId();
//        Integer appType = userAnswer.getAppType();
//        Integer scoringStrategy = userAnswer.getScoringStrategy();
//        String choices = userAnswer.getChoices();
//        Long resultId = userAnswer.getResultId();
//        String resultName = userAnswer.getResultName();
//        String resultDesc = userAnswer.getResultDesc();
//        String resultPicture = userAnswer.getResultPicture();
//        Integer resultScore = userAnswer.getResultScore();

        // 创建数据时，参数不能为空
        if (add) {
//            ThrowUtils.throwIf(StringUtils.isBlank(choices), ErrorCode.PARAMS_ERROR, "答案选项不能为空");
//            ThrowUtils.throwIf(StringUtils.isBlank(resultName), ErrorCode.PARAMS_ERROR, "答案名称不能为空");
//            ThrowUtils.throwIf(StringUtils.isBlank(resultDesc), ErrorCode.PARAMS_ERROR, "答案描述不能为空");
//            ThrowUtils.throwIf(StringUtils.isBlank(resultPicture), ErrorCode.PARAMS_ERROR, "答案图片不能为空");
            ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "所选应用不能为空");
//            ThrowUtils.throwIf(resultId == null, ErrorCode.PARAMS_ERROR, "所选结果不能为空");
//            ThrowUtils.throwIf(resultScore == null, ErrorCode.PARAMS_ERROR, "结果分数不能为空");
//            ThrowUtils.throwIf(AppTypeEnum.getEnumByValue(appType) == null, ErrorCode.PARAMS_ERROR, "应用类型不能为空");
//            ThrowUtils.throwIf(AppScoringStrategyEnum.getEnumByValue(scoringStrategy) == null, ErrorCode.PARAMS_ERROR, "应用类型不能为空");
        }
        // 修改数据时，有参数则校验
        if (appId != null) {
            App app = appService.getById(appId);
            ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
            ThrowUtils.throwIf(!Objects.equals(app.getReviewStatus(), ReviewStatusEnum.PASS.getValue()), ErrorCode.PARAMS_ERROR, "应用还未完成审核");
        }
    }

    /**
     * 获取查询条件
     *
     * @param userAnswerQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest userAnswerQueryRequest) {
        QueryWrapper<UserAnswer> queryWrapper = new QueryWrapper<>();
        if (userAnswerQueryRequest == null) {
            return queryWrapper;
        }
        Long id = userAnswerQueryRequest.getId();
        Long appId = userAnswerQueryRequest.getAppId();
        Integer appType = userAnswerQueryRequest.getAppType();
        Integer scoringStrategy = userAnswerQueryRequest.getScoringStrategy();
        String choices = userAnswerQueryRequest.getChoices();
        Long resultId = userAnswerQueryRequest.getResultId();
        String resultName = userAnswerQueryRequest.getResultName();
        String resultDesc = userAnswerQueryRequest.getResultDesc();
        String resultPicture = userAnswerQueryRequest.getResultPicture();
        Integer resultScore = userAnswerQueryRequest.getResultScore();
        Long userId = userAnswerQueryRequest.getUserId();
        Long notId = userAnswerQueryRequest.getNotId();
        String searchText = userAnswerQueryRequest.getSearchText();
        String sortField = userAnswerQueryRequest.getSortField();
        String sortOrder = userAnswerQueryRequest.getSortOrder();

        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("choices", searchText).or().like("resultName", searchText).or().like("resultDesc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(choices), "choices", choices);
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "resultDesc", resultDesc);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultId), "resultId", resultId);
        queryWrapper.eq(StringUtils.isNotEmpty(resultPicture), "resultPicture", resultPicture);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultScore), "resultScore", resultScore);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    /**
     * 获取用户答题记录封装
     *
     * @param userAnswer
     * @param request
     * @return
     */
    @Override
    public UserAnswerVO getUserAnswerVO(UserAnswer userAnswer, HttpServletRequest request) {
        // 对象转封装类
        UserAnswerVO userAnswerVO = UserAnswerVO.objToVo(userAnswer);

        // 1. 关联查询用户信息
        Long userId = userAnswer.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        userAnswerVO.setUser(userVO);
        return userAnswerVO;
    }

    /**
     * 分页获取用户答题记录封装
     *
     * @param userAnswerPage
     * @param request
     * @return
     */
    @Override
    public Page<UserAnswerVO> getUserAnswerVOPage(Page<UserAnswer> userAnswerPage, HttpServletRequest request) {
        List<UserAnswer> userAnswerList = userAnswerPage.getRecords();
        Page<UserAnswerVO> userAnswerVOPage = new Page<>(userAnswerPage.getCurrent(), userAnswerPage.getSize(), userAnswerPage.getTotal());
        if (CollUtil.isEmpty(userAnswerList)) {
            return userAnswerVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserAnswerVO> userAnswerVOList = userAnswerList.stream().map(userAnswer -> {
            return UserAnswerVO.objToVo(userAnswer);
        }).collect(Collectors.toList());

        // 1. 关联查询用户信息
        Set<Long> userIdSet = userAnswerList.stream().map(UserAnswer::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 填充信息
        userAnswerVOList.forEach(userAnswerVO -> {
            Long userId = userAnswerVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            userAnswerVO.setUser(userService.getUserVO(user));
        });

        userAnswerVOPage.setRecords(userAnswerVOList);
        return userAnswerVOPage;
    }

}
