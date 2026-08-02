package com.guanxuege.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.vo.PostVO;

import java.util.List;

public interface PostService extends IService<Post> {

    /** 按标签分页查询：recommend 推荐 / latest 最新 / hot 热议 */
    List<PostVO> listByTab(String tab, int page, int size);

    /** 发帖：落库并投递 RocketMQ 事件 */
    PostVO create(Post post, Long authorId);

    PostVO getVo(Long id);

    /** 点赞 +1，返回最新点赞数 */
    Integer like(Long id);
}
