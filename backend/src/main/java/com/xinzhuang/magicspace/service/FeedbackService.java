package com.xinzhuang.magicspace.service;

import com.xinzhuang.magicspace.dto.FeedbackDTO;
import com.xinzhuang.magicspace.entity.UserFeedback;
import com.xinzhuang.magicspace.mapper.UserFeedbackMapper;
import org.springframework.stereotype.Service;

/**
 * 用户反馈服务
 */
@Service
public class FeedbackService {

    private final UserFeedbackMapper feedbackMapper;

    public FeedbackService(UserFeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    /**
     * 提交反馈
     */
    public void submit(Long userId, FeedbackDTO dto) {
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(userId);
        feedback.setFeedbackType(dto.getFeedbackType());
        feedback.setContent(dto.getContent());
        feedback.setContact(dto.getContact());
        feedback.setStatus(0);
        feedbackMapper.insert(feedback);
    }
}
