package com.guanxuege.community.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.common.constants.CommunityType;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.community.entity.Circle;
import com.guanxuege.community.entity.UserCircle;
import com.guanxuege.community.mapper.CircleMapper;
import com.guanxuege.community.mapper.UserCircleMapper;
import com.guanxuege.community.service.CircleService;
import com.guanxuege.community.vo.CircleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CircleServiceImpl extends ServiceImpl<CircleMapper, Circle> implements CircleService {

    @Autowired
    private UserCircleMapper userCircleMapper;

    @Override
    public List<CircleVO> listAll(Long userId) {
        return listAll(userId, null);
    }

    @Override
    public List<CircleVO> listAll(Long userId, String community) {
        var query = Wrappers.<Circle>lambdaQuery();
        if (community != null && !community.isBlank()) {
            query.eq(Circle::getCommunity, CommunityType.normalize(community));
        }
        List<Circle> circles = baseMapper.selectList(query.orderByAsc(Circle::getSort));
        Set<Long> mine = new HashSet<>();
        if (userId != null) {
            mine = userCircleMapper.selectList(
                            Wrappers.<UserCircle>lambdaQuery().eq(UserCircle::getUserId, userId))
                    .stream().map(UserCircle::getCircleId).collect(Collectors.toSet());
        }
        Set<Long> finalMine = mine;
        return circles.stream().map(c -> {
            CircleVO vo = new CircleVO();
            BeanUtils.copyProperties(c, vo);
            vo.setJoined(finalMine.contains(c.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void join(Long userId, Long circleId) {
        Circle circle = baseMapper.selectById(circleId);
        if (circle == null) {
            throw new BusinessException("圈子不存在");
        }
        // 物理查询（含已退出的旧记录）：已加入直接返回，曾退出则恢复，首次加入才新增
        Long existId = userCircleMapper.findRawId(userId, circleId);
        if (existId != null) {
            if (userCircleMapper.restore(userId, circleId) == 0) {
                return; // 已是加入状态，幂等返回
            }
        } else {
            UserCircle uc = new UserCircle();
            uc.setUserId(userId);
            uc.setCircleId(circleId);
            userCircleMapper.insert(uc);
        }
        circle.setMemberCount((circle.getMemberCount() == null ? 0 : circle.getMemberCount()) + 1);
        baseMapper.updateById(circle);
    }

    @Override
    public void leave(Long userId, Long circleId) {
        int rows = userCircleMapper.delete(
                Wrappers.<UserCircle>lambdaQuery()
                        .eq(UserCircle::getUserId, userId)
                        .eq(UserCircle::getCircleId, circleId));
        if (rows == 0) {
            throw new BusinessException("尚未加入该圈子");
        }
        Circle circle = baseMapper.selectById(circleId);
        if (circle != null && circle.getMemberCount() != null && circle.getMemberCount() > 0) {
            circle.setMemberCount(circle.getMemberCount() - 1);
            baseMapper.updateById(circle);
        }
    }
}
