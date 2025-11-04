package com.saki.sakiaicodetoolsbackend.controller;

import com.mybatisflex.core.paginate.Page;
import com.saki.sakiaicodetoolsbackend.model.entity.App;
import com.saki.sakiaicodetoolsbackend.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用 控制层。
 *
 * @author saki酱
 * @since 2025-10-15
 */
@RestController
@Tag(name = "应用接口")
@RequestMapping("/app")
public class AppController {

    @Autowired
    private AppService appService;

    /**
     * 保存应用。
     *
     * @param app 应用
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @Operation(description = "保存应用")
    public boolean save(@RequestBody @Parameter(description = "应用") App app) {
        return appService.save(app);
    }

    /**
     * 根据主键删除应用。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description = "根据主键删除应用")
    public boolean remove(@PathVariable @Parameter(description = "应用主键") Long id) {
        return appService.removeById(id);
    }

    /**
     * 根据主键更新应用。
     *
     * @param app 应用
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description = "根据主键更新应用")
    public boolean update(@RequestBody @Parameter(description = "应用主键") App app) {
        return appService.updateById(app);
    }

    /**
     * 查询所有应用。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description = "查询所有应用")
    public List<App> list() {
        return appService.list();
    }

    /**
     * 根据主键获取应用。
     *
     * @param id 应用主键
     * @return 应用详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description = "根据主键获取应用")
    public App getInfo(@PathVariable @Parameter(description = "应用主键") Long id) {
        return appService.getById(id);
    }

    /**
     * 分页查询应用。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description = "分页查询应用")
    public Page<App> page(@Parameter(description = "分页信息") Page<App> page) {
        return appService.page(page);
    }

}
