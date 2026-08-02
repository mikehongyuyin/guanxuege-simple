package com.guanxuege.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.community.entity.Comment;
import com.guanxuege.community.mapper.CommentMapper;
import com.guanxuege.community.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}
