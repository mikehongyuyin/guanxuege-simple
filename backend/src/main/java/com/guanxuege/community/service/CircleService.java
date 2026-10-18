package com.guanxuege.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guanxuege.community.entity.Circle;
import com.guanxuege.community.vo.CircleVO;

import java.util.List;

public interface CircleService extends IService<Circle> {

    /** 全部圈子 + 当前用户加入状态 */
    List<CircleVO> listAll(Long userId);

    /** 指定社区的圈子 + 当前用户加入状态 */
    List<CircleVO> listAll(Long userId, String community);

    /** 加入圈子 */
    void join(Long userId, Long circleId);

    /** 退出圈子 */
    void leave(Long userId, Long circleId);
}
