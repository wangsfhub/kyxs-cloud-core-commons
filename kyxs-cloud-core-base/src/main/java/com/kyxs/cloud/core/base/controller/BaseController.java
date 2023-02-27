package com.kyxs.cloud.core.base.controller;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


@Component
public abstract class BaseController {
    //注入模型映射
    public static ModelMapper modelMapper;

    public BaseController() {
        modelMapper = new ModelMapper();
    }


}