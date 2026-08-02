package com.guanxuege.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.auth.entity.Child;
import com.guanxuege.auth.mapper.ChildMapper;
import com.guanxuege.auth.service.ChildService;
import org.springframework.stereotype.Service;

@Service
public class ChildServiceImpl extends ServiceImpl<ChildMapper, Child> implements ChildService {
}
