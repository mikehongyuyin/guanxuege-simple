package com.guanxuege.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.auth.entity.ConsentRecord;
import com.guanxuege.auth.mapper.ConsentRecordMapper;
import com.guanxuege.auth.service.ConsentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsentServiceImpl extends ServiceImpl<ConsentRecordMapper, ConsentRecord> implements ConsentService {

    @Override
    public ConsentRecord record(Long parentId, Long childId, String consentType, String version, String guardianRelation) {
        ConsentRecord r = new ConsentRecord();
        r.setParentId(parentId);
        r.setChildId(childId);
        r.setConsentType(consentType);
        r.setVersion(version);
        r.setGuardianRelation(guardianRelation);
        r.setAgreed(1);
        r.setAgreedAt(LocalDateTime.now());
        save(r);
        return r;
    }

    @Override
    public void withdraw(Long parentId, Long childId, String consentType) {
        List<ConsentRecord> list = list(Wrappers.<ConsentRecord>lambdaQuery()
                .eq(ConsentRecord::getParentId, parentId)
                .eq(consentType != null, ConsentRecord::getConsentType, consentType)
                .eq(childId != null, ConsentRecord::getChildId, childId)
                .eq(ConsentRecord::getAgreed, 1));
        for (ConsentRecord r : list) {
            r.setAgreed(0);
            r.setWithdrawnAt(LocalDateTime.now());
            updateById(r);
        }
    }

    @Override
    public ConsentRecord getLatestValid(Long parentId, Long childId, String consentType) {
        return getOne(Wrappers.<ConsentRecord>lambdaQuery()
                .eq(ConsentRecord::getParentId, parentId)
                .eq(ConsentRecord::getConsentType, consentType)
                .eq(childId != null, ConsentRecord::getChildId, childId)
                .eq(ConsentRecord::getAgreed, 1)
                .orderByDesc(ConsentRecord::getCreateTime), false);
    }

    @Override
    public List<ConsentRecord> listByParent(Long parentId) {
        return list(Wrappers.<ConsentRecord>lambdaQuery()
                .eq(ConsentRecord::getParentId, parentId)
                .orderByDesc(ConsentRecord::getCreateTime));
    }
}
