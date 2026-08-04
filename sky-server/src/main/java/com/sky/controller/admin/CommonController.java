package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用控制类，用于实现阿里云oss文件上传
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil AliOssUtils;

    /**
     * 文件上传
     * @param file  文件
     * @return 文件上传后的地址
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);
    try {
        //原始文件名
        String originalFilename = file.getOriginalFilename();
        //截取原始文件名后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //生成文件上传到阿里云后的名称
        String objectName = UUID.randomUUID().toString() + suffix;
        //文件请求路径
        String url = AliOssUtils.upload(file.getBytes(),objectName);
        return Result.success(url);
    } catch (IOException e) {
        log.error("文件上传失败：{}", e.getMessage(),e);
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
    }
}
