package com.xinzhuang.magicspace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinzhuang.magicspace.common.BizException;
import com.xinzhuang.magicspace.entity.PointsRecord;
import com.xinzhuang.magicspace.entity.User;
import com.xinzhuang.magicspace.enums.PointsType;
import com.xinzhuang.magicspace.mapper.PointsRecordMapper;
import com.xinzhuang.magicspace.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PointsServiceTest {

    private PointsService pointsService;
    private UserMapper userMapper;
    private PointsRecordMapper pointsRecordMapper;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        pointsRecordMapper = mock(PointsRecordMapper.class);
        pointsService = new PointsService(userMapper, pointsRecordMapper);
        ReflectionTestUtils.setField(pointsService, "generateCost", 10);
    }

    @Test
    void deductForGenerate_sufficientPoints_shouldDeduct() {
        User user = new User();
        user.setId(1L);
        user.setPoints(90); // After deduction: 100 - 10 = 90

        when(userMapper.deductPoints(1L, 10)).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(pointsRecordMapper.insert(any(PointsRecord.class))).thenReturn(1);

        assertDoesNotThrow(() -> pointsService.deductForGenerate(1L, 1L));
    }

    @Test
    void deductForGenerate_insufficientPoints_shouldThrow() {
        when(userMapper.deductPoints(1L, 10)).thenReturn(0);

        assertThrows(BizException.class, () -> pointsService.deductForGenerate(1L, 1L));
    }

    @Test
    void refundForFail_shouldAddPointsAndRecord() {
        User user = new User();
        user.setId(1L);
        user.setPoints(110);

        when(pointsRecordMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
        when(userMapper.addPoints(1L, 10)).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(pointsRecordMapper.insert(any(PointsRecord.class))).thenReturn(1);

        assertDoesNotThrow(() -> pointsService.refundForFail(1L, 1L));
    }

    @Test
    void refundForFail_alreadyRefunded_shouldSkip() {
        // Mock that a REFUND record already exists for this task
        when(pointsRecordMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

        pointsService.refundForFail(1L, 1L);

        // Should NOT call addPoints because already refunded
        verify(userMapper, never()).addPoints(anyLong(), anyInt());
        verify(pointsRecordMapper, never()).insert(any(PointsRecord.class));
    }

    @Test
    void refundForFail_firstRefund_shouldCreateRecord() {
        when(pointsRecordMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);

        User user = new User();
        user.setId(1L);
        user.setPoints(110);
        when(userMapper.addPoints(1L, 10)).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(pointsRecordMapper.insert(any(PointsRecord.class))).thenReturn(1);

        pointsService.refundForFail(1L, 5L);

        verify(userMapper, times(1)).addPoints(1L, 10);
        verify(pointsRecordMapper, times(1)).insert(any(PointsRecord.class));
    }

    @Test
    void getBalance_shouldReturnCorrectBalance() {
        User user = new User();
        user.setId(1L);
        user.setPoints(85);

        when(userMapper.selectById(1L)).thenReturn(user);

        var balance = pointsService.getBalance(1L);

        assertEquals(85, balance.getBalance());
        assertEquals(10, balance.getCostPerGenerate());
        assertEquals(8, balance.getRemainingGenerations()); // 85 / 10 = 8.5 -> 8
    }
}
