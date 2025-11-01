package com.saki.sakiaicodetoolsbackend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.saki.sakiaicodetoolsbackend.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 映射层。
 *
 * @author saki酱
 * @since 2025-10-15
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
