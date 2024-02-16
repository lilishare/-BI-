package com.lili.firstbi.manager;

import com.lili.firstbi.common.ErrorCode;
import com.lili.firstbi.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @projectName: firstbi-backend
 * @package: com.lili.firstbi.manager
 * @className: AiManager
 * @author: lili
 * @description: 调用ai接口
 * @date: 2024/2/8 18:37
 * @version: 1.0
 */
@Service
public class AiManagerByYu {
    @Resource
    private YuCongMingClient client;

    public String doChart(long modelId, String message){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        if(response == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取ai信息失败");
        }
        System.out.println(response.getData());
        return response.getData().getContent();
    }

}
