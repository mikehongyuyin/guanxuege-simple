package com.guanxuege.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.auth.entity.Family;
import com.guanxuege.auth.mapper.FamilyMapper;
import com.guanxuege.auth.service.FamilyService;
import org.springframework.stereotype.Service;

@Service
public class FamilyServiceImpl extends ServiceImpl<FamilyMapper, Family> implements FamilyService {

    @Override
    public Family getByParent(Long parentId) {
        return baseMapper.selectOne(
                Wrappers.<Family>lambdaQuery().eq(Family::getParentId, parentId));
    }
}
