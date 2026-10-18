package com.guanxuege.learningplan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.learningplan.entity.Contract;
import com.guanxuege.learningplan.mapper.ContractMapper;
import com.guanxuege.learningplan.service.ContractService;
import org.springframework.stereotype.Service;

@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements ContractService {
}
