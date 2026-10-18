package com.guanxuege.learningplan.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.common.exception.ForbiddenException;
import com.guanxuege.common.service.OwnershipService;
import com.guanxuege.learningplan.entity.Contract;
import com.guanxuege.learningplan.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 家庭契约：孩子与家长双向承诺、奖励与违约约定
 */
@RestController
@RequestMapping("/api/contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private OwnershipService ownershipService;

    @GetMapping("/list")
    public R<List<Contract>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                  @RequestParam(required = false) Long childId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        var query = Wrappers.<Contract>lambdaQuery().eq(Contract::getParentId, userId);
        if (childId != null) {
            query.eq(Contract::getChildId, childId);
        }
        query.orderByDesc(Contract::getCreateTime);
        return R.ok(contractService.list(query));
    }

    @PostMapping
    public R<Contract> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                              @RequestBody Contract contract) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (contract.getTitle() == null || contract.getTitle().isBlank()) {
            throw new BusinessException("请填写契约标题");
        }
        ownershipService.requireChild(userId, contract.getChildId());
        contract.setParentId(userId);
        contract.setStatus(contract.getStatus() == null ? 0 : contract.getStatus());
        contract.setChildConfirm(0);
        contractService.save(contract);
        return R.ok(contract);
    }

    @PutMapping
    public R<Contract> update(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                              @RequestBody Contract contract) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Contract exist = contractService.getById(contract.getId());
        if (exist == null || !exist.getParentId().equals(userId)) {
            throw new BusinessException("契约不存在");
        }
        exist.setTitle(contract.getTitle());
        exist.setChildId(contract.getChildId());
        exist.setChildPromise(contract.getChildPromise());
        exist.setParentPromise(contract.getParentPromise());
        exist.setReward(contract.getReward());
        exist.setPenalty(contract.getPenalty());
        exist.setStartDate(contract.getStartDate());
        exist.setEndDate(contract.getEndDate());
        contractService.updateById(exist);
        return R.ok(exist);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                             @PathVariable("id") Long id) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Contract exist = contractService.getById(id);
        if (exist == null || !exist.getParentId().equals(userId)) {
            throw new BusinessException("契约不存在");
        }
        return R.ok(contractService.removeById(id));
    }

    /** 孩子确认契约（仅限契约所属家长操作） */
    @PostMapping("/{id}/confirm")
    public R<Contract> confirm(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                               @PathVariable("id") Long id) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Contract exist = contractService.getById(id);
        if (exist == null || !exist.getParentId().equals(userId)) {
            throw new ForbiddenException("契约不存在");
        }
        exist.setChildConfirm(1);
        contractService.updateById(exist);
        return R.ok(exist);
    }

    /** 标记达成(1) / 违约(2) */
    @PostMapping("/{id}/status")
    public R<Contract> markStatus(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                  @PathVariable("id") Long id,
                                  @RequestParam Integer status) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Contract exist = contractService.getById(id);
        if (exist == null || !exist.getParentId().equals(userId)) {
            throw new BusinessException("契约不存在");
        }
        if (status == null || status < 1 || status > 2) {
            throw new BusinessException("状态不合法");
        }
        exist.setStatus(status);
        contractService.updateById(exist);
        return R.ok(exist);
    }
}
