package com.lili.firstbi.utils;

import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class FileUtil {


    /**
     * 校验excel文件安全
     *
     * @return 文件类型
     * @throws IOException
     */
    public static void checkExcelFile(MultipartFile multipartFile, double fileMaxSize) {
        // 文件类型判断 - 校验文件后缀
        String fileName = multipartFile.getOriginalFilename();
        if (Strings.isEmpty(fileName)) {
            throw new RuntimeException("文件名未找到");
        }
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!Objects.equal(suffix, "xls") && !Objects.equal(suffix, "xlsx")) {
            throw new RuntimeException("文件类型不正确，需要为xls或者xlsx");
        }

        // 文件类型判断 - 校验文件头内容
        try (InputStream inputStream =  new BufferedInputStream(multipartFile.getInputStream())) {
            // 获取到上传文件的文件头信息
            boolean isExcel = checkIsExcel(inputStream);
            if (!isExcel) {
                throw new RuntimeException("文件类型不正确，原文件类型需要为xls");
            }
        } catch (IOException e) {
            log.error("Get file input stream failed.", e);
            throw new RuntimeException("文件上传失败");
        }

        // 文件大小校验 - 单位：MB
        long fileBytes = multipartFile.getSize();
        double fileSize = (double) fileBytes / 1048576;
        if (fileSize <= 0) {
            throw new RuntimeException("文件内容为空");
        }
        if (fileSize > fileMaxSize) {
            throw new RuntimeException("文件上传内容大小超出限制");
        }
    }


    /**
     * 校验文件头
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static boolean checkIsExcel(InputStream inputStream) throws IOException {
        FileMagic fileMagic = FileMagic.valueOf(inputStream);
        if (Objects.equal(fileMagic, FileMagic.OLE2) || Objects.equal(fileMagic, FileMagic.OOXML)) {
            return true;
        }
        return false;
    }

}

