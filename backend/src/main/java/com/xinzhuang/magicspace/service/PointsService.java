package com.xinzhuang.magicspace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinzhuang.magicspace.common.BizException;
import com.xinzhuang.magicspace.common.ErrorCode;
import com.xinzhuang.magicspace.common.PageResult;
import com.xinzhuang.magicspace.entity.PointsRecord;
import com.xinzhuang.magicspace.entity.User;
import com.xinzhuang.magicspace.enums.PointsType;
import com.xinzhuang.magicspace.mapper.PointsRecordMapper;
import com.xinzhuang.magicspace.mapper.UserMapper;
import com.xinzhuang.magicspace.vo.PointsBalanceVO;
import com.xinzhuang.magicspace.vo.PointsRecordVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分服务
 */
@Service
public class PointsService {

    private static final Logger log = LoggerFactory.getLogger(PointsService.class);

    private final UserMapper userMapper;
    private final PointsRecordMapper pointsRecordMapper;

    @Value("${points.generate-cost:10}")
    private int generateCost;

    public PointsService(UserMapper userMapper, PointsRecordMapper pointsRecordMapper) {
        this.userMapper = userMapper;
        this.pointsRecordMapper = pointsRecordMapper;
    }

    /**
     * 获取积分余额
     */
    public PointsBalanceVO getBalance(Long userId) {
        User user = userMapper.selectById(userId);
        return PointsBalanceVO.of(user.getPoints(), generateCost);
    }

    /**
     * 扣减积分（生成消耗）
     * 使用原子 SQL 防止超扣，并记录流水
     */
    @Transactional
    public void deductForGenerate(Long userId, Long taskId) {
        int rows = userMapper.deductPoints(userId, generateCost);
        if (rows == 0) {
            throw new BizException(ErrorCode.POINTS_INSUFFICIENT);
        }

        // 重新读取更新后的积分
        User user = userMapper.selectById(userId);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setChangeAmount(-generateCost);
        record.setBalanceBefore(user.getPoints() + generateCost);
        record.setBalanceAfter(user.getPoints());
        record.setRecordType(PointsType.GENERATE_CONSUME.name());
        record.setRefTaskId(taskId);
        record.setRemark("AI 空间改造生成消耗");
        pointsRecordMapper.insert(record);

        log.info("积分扣减: userId={}, amount={}, afterBalance={}, taskId={}",
                userId, generateCost, user.getPoints(), taskId);
    }

    /**
     * 退回积分（生成失败退款）
     * 幂等：如果该任务已经退回过积分，不再重复退款
     */
    @Transactional
    public void refundForFail(Long userId, Long taskId) {
        // 幂等检查：该任务是否已有退款流水
        boolean alreadyRefunded = pointsRecordMapper.exists(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getRefTaskId, taskId)
                .eq(PointsRecord::getRecordType, PointsType.REFUND.name()));
        if (alreadyRefunded) {
            log.warn("积分已退回，跳过重复退款: userId={}, taskId={}", userId, taskId);
            return;
        }

        userMapper.addPoints(userId, generateCost);

        User user = userMapper.selectById(userId);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setChangeAmount(generateCost);
        record.setBalanceBefore(user.getPoints() - generateCost);
        record.setBalanceAfter(user.getPoints());
        record.setRecordType(PointsType.REFUND.name());
        record.setRefTaskId(taskId);
        record.setRemark("AI 生成失败，退回积分");
        pointsRecordMapper.insert(record);

        log.info("积分退回: userId={}, amount={}, afterBalance={}, taskId={}",
                userId, generateCost, user.getPoints(), taskId);
    }

    /**
     * 查询积分流水
     */
    public PageResult<PointsRecordVO> getRecords(Long userId, int page, int pageSize) {
        int cappedPageSize = Math.min(pageSize, 50);
        LambdaQueryWrapper<PointsRecord> query = new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getUserId, userId)
                .orderByDesc(PointsRecord::getCreatedAt);

        Page<PointsRecord> mpPage = new Page<>(page, cappedPageSize);
        Page<PointsRecord> result = pointsRecordMapper.selectPage(mpPage, query);

        List<PointsRecordVO> list = result.getRecords().stream().map(r -> {
            PointsRecordVO vo = new PointsRecordVO();
            vo.setId(r.getId());
            vo.setChangeAmount(r.getChangeAmount());
            vo.setBalanceBefore(r.getBalanceBefore());
            vo.setBalanceAfter(r.getBalanceAfter());
            vo.setRecordType(r.getRecordType());
            vo.setRefTaskId(r.getRefTaskId());
            vo.setRemark(r.getRemark());
            vo.setCreatedAt(r.getCreatedAt());

            // 流水类型中文描述
            try {
                vo.setRecordTypeDesc(PointsType.valueOf(r.getRecordType()).getDesc());
            } catch (Exception e) {
                vo.setRecordTypeDesc(r.getRecordType());
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), page, cappedPageSize, list);
    }
}
