package com.lili.firstbi.model.vo;

import lombok.Data;

/**
 * @projectName: firstbi-backend
 * @package: com.lili.firstbi.model.vo
 * @className: BiResponse
 * @author: lili
 * @description: TODO
 * @date: 2024/2/8 19:23
 * @version: 1.0
 */
@Data
public class BiResponse {
    private String genChart;
    private String genResult;
    private Long chartId;
}
