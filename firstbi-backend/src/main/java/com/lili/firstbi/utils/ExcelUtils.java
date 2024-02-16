package com.lili.firstbi.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @projectName: firstbi-backend
 * @package: com.lili.firstbi.utils
 * @className: ExcelUtils
 * @author: lili
 * @description: 关于excel表格相关的处理
 * @date: 2024/2/7 20:26
 * @version: 1.0
 */
public class ExcelUtils {
    public static String toCSV(MultipartFile multipartFile) {
        StringBuffer result = new StringBuffer();
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Map<Integer, String>> list = EasyExcel.read(inputStream)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet()
                .headRowNumber(0)
                .doReadSync();

        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        LinkedHashMap<Integer, String> headMap = (LinkedHashMap<Integer, String>) list.get(0);
        List<String> headList = headMap.values().stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        result.append(StringUtils.join(headList, ",")).append("\n");
        for (int i = 1; i < list.size(); i++) {
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap<Integer, String>) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            result.append(StringUtils.join(dataList, ","));
        }
        return result.toString();
    }
}
