package com.saki.sakiaicodetoolsbackend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.saki.sakiaicodetoolsbackend.mapper.AppMapper;
import com.saki.sakiaicodetoolsbackend.model.entity.App;
import com.saki.sakiaicodetoolsbackend.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author saki酱
 * @since 2025-10-15
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

}
