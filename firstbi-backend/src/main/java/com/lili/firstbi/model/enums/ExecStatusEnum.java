package com.lili.firstbi.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图表执行状态枚举
 *
 */
public enum ExecStatusEnum {

    WAIT("排队中", 0),
    RUNNING("执行中", 1),
    SUCCEED("执行成功", 2),
    FAILED("执行失败", 3);

    private final String text;

    private final Integer value;

    ExecStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ExecStatusEnum getEnumByValue(int value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ExecStatusEnum anEnum : ExecStatusEnum.values()) {
            if (anEnum.value==value) {
                return anEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
