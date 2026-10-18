package com.guanxuege.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guanxuege.auth.entity.Family;

public interface FamilyService extends IService<Family> {

    /** 查询家长的家庭档案，未创建时返回 null */
    Family getByParent(Long parentId);
}
