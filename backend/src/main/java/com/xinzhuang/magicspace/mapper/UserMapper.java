package com.xinzhuang.magicspace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinzhuang.magicspace.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 扣减积分（原子操作，防止超扣）
     */
    @Update("UPDATE user SET points = points - #{amount}, updated_at = NOW() WHERE id = #{userId} AND points >= #{amount}")
    int deductPoints(@Param("userId") Long userId, @Param("amount") int amount);

    /**
     * 增加积分
     */
    @Update("UPDATE user SET points = points + #{amount}, updated_at = NOW() WHERE id = #{userId}")
    int addPoints(@Param("userId") Long userId, @Param("amount") int amount);
}
