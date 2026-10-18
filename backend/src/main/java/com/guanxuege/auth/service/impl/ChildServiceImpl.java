package com.guanxuege.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.auth.ConsentConstants;
import com.guanxuege.auth.dto.ChildCreateDTO;
import com.guanxuege.auth.entity.Child;
import com.guanxuege.auth.mapper.ChildMapper;
import com.guanxuege.auth.service.ChildService;
import com.guanxuege.auth.service.ConsentService;
import com.guanxuege.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChildServiceImpl extends ServiceImpl<ChildMapper, Child> implements ChildService {

    private final ConsentService consentService;

    public ChildServiceImpl(ConsentService consentService) {
        this.consentService = consentService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Child createWithConsent(Long parentId, ChildCreateDTO dto) {
        // 强制：监护人关系声明 + 儿童信息专项同意
        if (dto.getConsentAgreed() == null || !dto.getConsentAgreed()) {
            throw new BusinessException("添加孩子前，请先完成监护人关系声明与《儿童个人信息保护规则》专项同意");
        }
        if (dto.getGuardianRelation() == null || dto.getGuardianRelation().isBlank()) {
            throw new BusinessException("请声明您与孩子的监护关系（父母 / 祖父母 / 其他监护人）");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BusinessException("请输入孩子姓名");
        }

        Child child = new Child();
        BeanUtils.copyProperties(dto, child, "consentAgreed", "consentVersion", "guardianRelation");
        child.setParentId(parentId);
        save(child);

        // 记录儿童信息专项同意，并关联刚创建的孩子
        consentService.record(parentId, child.getId(), ConsentConstants.TYPE_CHILD_INFO,
                dto.getConsentVersion() == null ? ConsentConstants.CHILD_RULES_VERSION : dto.getConsentVersion(),
                dto.getGuardianRelation());
        return child;
    }

    @Override
    public boolean deleteWithConsent(Long parentId, Long childId) {
        boolean removed = removeById(childId);
        // 撤回该孩子的儿童信息同意（保留审计记录，agreed 置 0）
        consentService.withdraw(parentId, childId, ConsentConstants.TYPE_CHILD_INFO);
        return removed;
    }
}
