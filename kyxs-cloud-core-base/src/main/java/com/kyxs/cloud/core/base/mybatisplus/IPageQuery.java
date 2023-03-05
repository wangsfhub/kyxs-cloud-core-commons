package com.kyxs.cloud.core.base.mybatisplus;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface IPageQuery<T> extends IPage<T> {
    List<FilterItem> filters();
}