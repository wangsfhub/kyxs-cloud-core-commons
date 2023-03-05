package com.kyxs.cloud.core.base.mybatisplus;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.*;

public class PageQuery<T> extends Page<T> implements IPageQuery<T> {
    private static final String PAGE = "page";
    private static final String LIMIT_FIELD_NAME = "limit";
    private static final String ORDER_FIELD_NAME = "orders";
    private static final String DESC_FIELD_NAME = "descs";
    private static final String ASC_FIELD_NAME = "ascs";
    private static final String ORDERS_COLUMN = "column";
    private static final String ORDERS_IS_ASC = "asc";
    private static final String FILTERS = "filters";
    private static final String FILTER_ITEMS = "filterItems";
    private static final String FILTERS_COLUMN = "column";
    private static final String FILTERS_TYPE = "type";
    private static final String FILTERS_VALUE = "value";
    private static final String FILTERS_OPERATOR = "operator";
    private Map<String, Object> condition;
    private List<FilterItem> filterItems = new ArrayList();

    public PageQuery(PageQueryDTO dto) {

        super((long)Integer.parseInt(dto.getCurrent()), (long)Integer.parseInt(dto.getSize()));
        String descsStr = dto.getDescs();
        int var5;
        if (StringUtils.isNotBlank(descsStr)) {
            String[] var3 = descsStr.split(",");
            int var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
                String str = var3[var5];
                this.addOrder(new OrderItem[]{OrderItem.desc(this.camelToUnderlineAndReplaceSqlInject(str))});
            }
        }

        String ascsStr = dto.getAscs();
        String field;
        if (StringUtils.isNotBlank(ascsStr)) {
            String[] var9 = ascsStr.split(",");
            var5 = var9.length;

            for(int var12 = 0; var12 < var5; ++var12) {
                field = var9[var12];
                this.addOrder(new OrderItem[]{OrderItem.asc(this.camelToUnderlineAndReplaceSqlInject(field))});
            }
        }

        List<FilterItem> filterList = dto.getFilterItems();
        if (null != filterList && !filterList.isEmpty()) {
            Iterator var11 = filterList.iterator();

            while(var11.hasNext()) {
                FilterItem filterItem = (FilterItem)var11.next();
                field = filterItem.getColumn();
                if (StringUtils.isNotBlank(field)) {
                    field = StringUtils.camelToUnderline(field);
                    field = this.replaceSqlInject(field);
                    filterItem.setColumn(field);
                    this.filterItems.add(filterItem);
                }
            }
        }

        Map<String, Object> condition = dto.getCondition();
        if (condition != null) {
            condition.remove("page");
            condition.remove("limit");
            condition.remove("orders");
            condition.remove("filters");
            condition.remove("filterItems");
            condition.remove("descs");
            condition.remove("ascs");
            this.setCondition(condition);
        }

    }

    public String camelToUnderlineAndReplaceSqlInject(String input) {
        String result = StringUtils.camelToUnderline(input);
        return this.replaceSqlInject(result);
    }

    public String replaceSqlInject(String str) {
        return str.replace("<", "＜").replace(">", "＞").replace(";", "；").replace("--", "——").replace("'", "\"\"");
    }

    public List<FilterItem> filters() {
        return this.filterItems;
    }

    public void addCondition(String key, Object value) {
        if (null == this.condition) {
            this.condition = new HashMap(4);
        }

        this.condition.put(key, value);
    }

    public Map<String, Object> getCondition() {
        return this.condition;
    }

    public void setCondition(Map<String, Object> condition) {
        this.condition = condition;
    }
}
