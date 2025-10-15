package com.saki.sakiaicodetoolsbackend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import com.saki.sakiaicodetoolsbackend.mapper.UserMapper;
import com.saki.sakiaicodetoolsbackend.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户表 服务层实现。
 *
 * @author saki酱
 * @since 2025-10-15
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
