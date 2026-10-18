package com.guanxuege.community.controller;

import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.common.service.OwnershipService;
import com.guanxuege.community.dto.PostToPlanDTO;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.service.PostService;
import com.guanxuege.community.vo.PostVO;
import com.guanxuege.learningplan.entity.Plan;
import com.guanxuege.learningplan.entity.PlanTask;
import com.guanxuege.learningplan.service.PlanService;
import com.guanxuege.learningplan.service.PlanTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanTaskService planTaskService;

    @Autowired
    private OwnershipService ownershipService;

    /**
     * 帖子列表。community 用于区分育儿社区与心声社区，默认育儿社区，保证老调用方行为不变。
     */
    @GetMapping("/list")
    public R<List<PostVO>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                @RequestParam(defaultValue = "recommend") String tab,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) Long circleId,
                                @RequestParam(required = false) String community) {
        return R.ok(postService.listByTab(tab, page, size, circleId, userId, community));
    }

    @PostMapping
    public R<PostVO> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                            @RequestBody Post post) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(postService.create(post, userId));
    }

    @GetMapping("/{id}")
    public R<PostVO> detail(@PathVariable("id") Long id,
                            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return R.ok(postService.getVo(id, userId));
    }

    /** 点赞：幂等 toggle，同一用户再次调用即取消点赞 */
    @PostMapping("/{id}/like")
    public R<Integer> like(@PathVariable("id") Long id,
                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(postService.like(id, userId));
    }

    @PutMapping("/{id}")
    public R<PostVO> update(@PathVariable("id") Long id,
                            @RequestHeader(value = "X-User-Id", required = false) Long userId,
                            @RequestBody Post post) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(postService.updatePost(id, post, userId));
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id,
                             @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(postService.deletePost(id, userId));
    }

    /**
     * 帖子一键转计划：基于帖子内容创建学习计划 + 初始任务
     */
    @PostMapping("/{id}/to-plan")
    public R<Plan> toPlan(@PathVariable("id") Long id,
                          @RequestHeader(value = "X-User-Id", required = false) Long userId,
                          @RequestBody PostToPlanDTO dto) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Post post = postService.getById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        if (dto.getChildId() == null) {
            throw new BusinessException("请选择孩子");
        }
        // 生成的计划挂在自己的孩子名下，禁止挂到他人孩子上
        ownershipService.requireChild(userId, dto.getChildId());
        String title = (dto.getTitle() == null || dto.getTitle().isBlank())
                ? "「" + post.getTitle() + "」践行计划" : dto.getTitle().trim();

        Plan plan = new Plan();
        plan.setChildId(dto.getChildId());
        plan.setTitle(title);
        plan.setDescription("由社区帖子一键转来：\n《" + post.getTitle() + "》\n\n"
                + (post.getContent() == null ? "" : post.getContent()));
        plan.setPlanType("WEEK");
        plan.setStatus(0);
        plan.setProgress(0);
        plan.setStartDate(LocalDate.now());
        plan.setEndDate(LocalDate.now().plusDays(7));
        planService.save(plan);

        List<String> taskTitles = dto.getTaskTitles();
        if (taskTitles != null) {
            for (String t : taskTitles) {
                if (t == null || t.isBlank()) {
                    continue;
                }
                PlanTask task = new PlanTask();
                task.setPlanId(plan.getId());
                task.setTitle(t.trim());
                task.setStatus(0);
                task.setProgress(0);
                planTaskService.save(task);
            }
        }
        return R.ok(plan);
    }
}
