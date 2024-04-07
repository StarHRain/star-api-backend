package star.api.chart.controller;

import cn.hutool.core.io.FileUtil;
import javassist.runtime.Inner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import star.api.chart.service.ImageService;
import star.api.chart.service.impl.AiService;
import star.api.common.BaseResponse;
import star.api.common.ErrorCode;
import star.api.common.ResultUtils;
import star.api.exception.BusinessException;
import star.api.model.dto.image.UploadImageRequest;
import star.api.model.entity.Image;
import star.api.model.entity.User;
import star.api.service.InnerUserService;
import star.api.utils.ThrowUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;

import static star.api.model.enums.FileUploadBizEnum.IMAGE_AI;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 26 日
 */
@RestController
@RequestMapping("/image")
@Slf4j
public class ImageController {


    @DubboReference
    private InnerUserService innerUserService;

    @Resource
    private AiService aiService;

    @Resource
    private ImageService imageService;

    /**
     * 文件上传,并返回ai解析结果
     *
     * @param file
     * @param uploadImageRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadImageAnalysis(@RequestPart("file") MultipartFile file,
                                                    UploadImageRequest uploadImageRequest, HttpServletRequest request) {
        // 校验
        String goal = uploadImageRequest.getGoal();
        ThrowUtils.throwIf(goal==null,ErrorCode.PARAMS_ERROR);
        String fileSuffix = validFile(file);

        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + file.getOriginalFilename();
        String token = request.getHeader("Authorization");
        User loginUser = innerUserService.getLoginUser(token);
        String filepath = String.format("/%s/%s/%s", IMAGE_AI , loginUser.getId(), filename);
        File newFile = null;
        try {
            // 上传文件
            newFile = File.createTempFile(filepath, null);

            file.transferTo(newFile);
            Image image = new Image();
            image.setGoal(goal);
            image.setImageType(fileSuffix);
            boolean save = imageService.save(image);
            ThrowUtils.throwIf(!save,ErrorCode.OPERATION_ERROR);
            //调用ai服务（图片转文字
            String ans = aiService.pictureToText(newFile, goal, image.getId());
            image.setGenResult(ans);
            boolean update = imageService.updateById(image);
            ThrowUtils.throwIf(!update,ErrorCode.OPERATION_ERROR,"更新操作失败");

            // 返回可访问地址
            return ResultUtils.success(ans);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = newFile.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     */
    private String validFile(MultipartFile multipartFile) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 5 * 1024 * 1024L;
        if (fileSize > ONE_M) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
        }
        if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
        }
        return fileSuffix;
    }
}
