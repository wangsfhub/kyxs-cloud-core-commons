package com.kyxs.cloud.core.base.mybatisplus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageQueryDTO {
    List<FilterItem> filterItems;
    private String current = "1";
    private String size = "20";
    private String ascs;
    private String descs;
    private Map<String, Object> condition;

    public void addCondition(String key, Object value) {
        if (null == this.condition) {
            this.condition = new HashMap(4);
        }

        this.condition.put(key, value);
    }

    public PageQueryDTO() {
    }

    public List<FilterItem> getFilterItems() {
        return this.filterItems;
    }

    public String getCurrent() {
        return this.current;
    }

    public String getSize() {
        return this.size;
    }

    public String getAscs() {
        return this.ascs;
    }

    public String getDescs() {
        return this.descs;
    }

    public Map<String, Object> getCondition() {
        return this.condition;
    }

    public void setFilterItems(List<FilterItem> filterItems) {
        this.filterItems = filterItems;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setAscs(String ascs) {
        this.ascs = ascs;
    }

    public void setDescs(String descs) {
        this.descs = descs;
    }

    public void setCondition(Map<String, Object> condition) {
        this.condition = condition;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof PageQueryDTO)) {
            return false;
        } else {
            PageQueryDTO other = (PageQueryDTO) o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$filterItems = this.getFilterItems();
                Object other$filterItems = other.getFilterItems();
                if (this$filterItems == null) {
                    if (other$filterItems != null) {
                        return false;
                    }
                } else if (!this$filterItems.equals(other$filterItems)) {
                    return false;
                }

                Object this$current = this.getCurrent();
                Object other$current = other.getCurrent();
                if (this$current == null) {
                    if (other$current != null) {
                        return false;
                    }
                } else if (!this$current.equals(other$current)) {
                    return false;
                }

                Object this$size = this.getSize();
                Object other$size = other.getSize();
                if (this$size == null) {
                    if (other$size != null) {
                        return false;
                    }
                } else if (!this$size.equals(other$size)) {
                    return false;
                }

                String this$descs;
                String other$descs;
                label71:
                {
                    this$descs = this.getAscs();
                    other$descs = other.getAscs();
                    if (this$descs == null) {
                        if (other$descs == null) {
                            break label71;
                        }
                    } else if (this$descs.equals(other$descs)) {
                        break label71;
                    }

                    return false;
                }

                this$descs = this.getDescs();
                other$descs = other.getDescs();
                if (this$descs == null) {
                    if (other$descs != null) {
                        return false;
                    }
                } else if (!this$descs.equals(other$descs)) {
                    return false;
                }

                Object this$condition = this.getCondition();
                Object other$condition = other.getCondition();
                if (this$condition == null) {
                    if (other$condition != null) {
                        return false;
                    }
                } else if (!this$condition.equals(other$condition)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof PageQueryDTO;
    }

    public int hashCode() {
        int result = 1;
        Object $filterItems = this.getFilterItems();
        result = result * 59 + ($filterItems == null ? 43 : $filterItems.hashCode());
        Object $current = this.getCurrent();
        result = result * 59 + ($current == null ? 43 : $current.hashCode());
        Object $size = this.getSize();
        result = result * 59 + ($size == null ? 43 : $size.hashCode());
        Object $ascs = this.getAscs();
        result = result * 59 + ($ascs == null ? 43 : $ascs.hashCode());
        Object $descs = this.getDescs();
        result = result * 59 + ($descs == null ? 43 : $descs.hashCode());
        Object $condition = this.getCondition();
        result = result * 59 + ($condition == null ? 43 : $condition.hashCode());
        return result;
    }

    public String toString() {
        return "PageQueryDTO(filterItems=" + this.getFilterItems() + ", current=" + this.getCurrent() + ", size=" + this.getSize() + ", ascs=" + this.getAscs() + ", descs=" + this.getDescs() + ", condition=" + this.getCondition() + ")";
    }
}