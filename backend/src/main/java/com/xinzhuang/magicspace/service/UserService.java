package com.xinzhuang.magicspace.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinzhuang.magicspace.common.BizException;
import com.xinzhuang.magicspace.common.ErrorCode;
import com.xinzhuang.magicspace.entity.PointsRecord;
import com.xinzhuang.magicspace.entity.User;
import com.xinzhuang.magicspace.enums.PointsType;
import com.xinzhuang.magicspace.mapper.PointsRecordMapper;
import com.xinzhuang.magicspace.mapper.UserMapper;
import com.xinzhuang.magicspace.vo.LoginVO;
import com.xinzhuang.magicspace.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserMapper userMapper;
    private final PointsRecordMapper pointsRecordMapper;

    @Value("${points.register-gift:100}")
    private int registerGiftPoints;

    public UserService(UserMapper userMapper, PointsRecordMapper pointsRecordMapper) {
        this.userMapper = userMapper;
        this.pointsRecordMapper = pointsRecordMapper;
    }

    /**
     * 微信登录 / 注册
     * 根据 openid 查找用户，不存在则创建新用户并赠送积分
     */
    @Transactional
    public LoginVO wxLogin(String openid, String nickname, String avatarUrl) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));

        boolean isNewUser = false;

        if (user == null) {
            // 注册新用户
            user = new User();
            user.setOpenid(openid);
            user.setNickname(nickname != null ? nickname : "新装用户");
            user.setAvatarUrl(avatarUrl);
            user.setPoints(registerGiftPoints);
            user.setStatus(1);
            userMapper.insert(user);

            // 记录注册赠送积分
            PointsRecord record = new PointsRecord();
            record.setUserId(user.getId());
            record.setChangeAmount(registerGiftPoints);
            record.setBalanceBefore(0);
            record.setBalanceAfter(registerGiftPoints);
            record.setRecordType(PointsType.REGISTER_GIFT.name());
            record.setRemark("注册赠送" + registerGiftPoints + "积分");
            pointsRecordMapper.insert(record);

            isNewUser = true;
            log.info("新用户注册: userId={}, openid={}, giftPoints={}", user.getId(), openid, registerGiftPoints);
        } else {
            // 更新信息
            if (nickname != null && avatarUrl != null) {
                user.setNickname(nickname);
                user.setAvatarUrl(avatarUrl);
            }
            userMapper.updateById(user);
        }

        // 登录
        StpUtil.login(user.getId());

        String token = StpUtil.getTokenValue();
        UserVO userVO = toUserVO(user);

        return LoginVO.of(token, userVO, isNewUser);
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    /**
     * 获取当前用户信息视图
     */
    public UserVO getCurrentUserVO() {
        return toUserVO(getCurrentUser());
    }

    /**
     * 更新用户资料
     */
    public UserVO updateProfile(String nickname, String avatarUrl) {
        User user = getCurrentUser();

        if (nickname != null) {
            String trimmed = nickname.trim();
            if (trimmed.isEmpty()) {
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "昵称不能为空");
            }
            if (trimmed.length() > 32) {
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "昵称长度不能超过32个字符");
            }
            user.setNickname(trimmed);
        }
        if (avatarUrl != null) {
            if (avatarUrl.length() > 500) {
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "头像地址长度不合法");
            }
            if (!avatarUrl.startsWith("https://") && !avatarUrl.startsWith("http://")) {
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "头像地址必须以 http:// 或 https:// 开头");
            }
            user.setAvatarUrl(avatarUrl);
        }
        userMapper.updateById(user);
        return toUserVO(user);
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPoints(user.getPoints());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
