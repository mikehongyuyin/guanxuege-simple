package com.guanxuege.auth.controller;

import com.guanxuege.auth.dto.ConsentDTO;
import com.guanxuege.auth.entity.ConsentRecord;
import com.guanxuege.auth.service.ConsentService;
import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consent")
public class ConsentController {

    @Autowired
    private ConsentService consentService;

    /** 记录同意（注册 / 绑定孩子时由前端在用户确认后调用） */
    @PostMapping
    public R<ConsentRecord> record(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                   @RequestBody ConsentDTO dto) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        ConsentRecord r = consentService.record(userId, dto.getChildId(), dto.getConsentType(),
                dto.getVersion(), dto.getGuardianRelation());
        return R.ok(r);
    }

    /** 撤回同意（删除通道：用户可撤回儿童信息授权，数据相应隐藏） */
    @PostMapping("/withdraw")
    public R<Boolean> withdraw(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                               @RequestBody ConsentDTO dto) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        consentService.withdraw(userId, dto.getChildId(), dto.getConsentType());
        return R.ok(true);
    }

    /** 列出当前家长全部同意记录 */
    @GetMapping("/list")
    public R<List<ConsentRecord>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(consentService.listByParent(userId));
    }

    /** 查询某项同意是否有效及版本（前端据此决定是否弹出强制同意） */
    @GetMapping("/check")
    public R<Map<String, Object>> check(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                        @RequestParam String consentType,
                                        @RequestParam(required = false) Long childId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        ConsentRecord r = consentService.getLatestValid(userId, childId, consentType);
        Map<String, Object> m = new HashMap<>(4);
        m.put("agreed", r != null);
        m.put("version", r == null ? null : r.getVersion());
        m.put("guardianRelation", r == null ? null : r.getGuardianRelation());
        m.put("agreedAt", r == null ? null : r.getAgreedAt());
        return R.ok(m);
    }
}
