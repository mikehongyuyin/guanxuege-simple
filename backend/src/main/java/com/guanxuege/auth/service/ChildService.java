package com.guanxuege.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guanxuege.auth.dto.ChildCreateDTO;
import com.guanxuege.auth.entity.Child;

public interface ChildService extends IService<Child> {

    /**
     * 绑定孩子（含监护人关系声明 + 儿童个人信息专项同意校验）。
     * 校验不通过将抛出 BusinessException；通过则保存孩子并记录专项同意。
     */
    Child createWithConsent(Long parentId, ChildCreateDTO dto);

    /**
     * 删除孩子并撤回其《儿童个人信息保护规则》同意（删除通道）。
     */
    boolean deleteWithConsent(Long parentId, Long childId);
}
