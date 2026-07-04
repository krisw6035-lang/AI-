package com.xinzhuang.magicspace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinzhuang.magicspace.entity.UserFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户反馈 Mapper
 */
@Mapper
public interface UserFeedbackMapper extends BaseMapper<UserFeedback> {
}
