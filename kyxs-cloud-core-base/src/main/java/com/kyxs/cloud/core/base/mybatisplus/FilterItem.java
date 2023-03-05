package com.kyxs.cloud.core.base.mybatisplus;

import java.io.Serializable;

public class FilterItem implements Serializable {
    private String column;
    private String type = "and";
    private String value = "";
    private String operator = "=";

    public FilterItem() {
    }

    public FilterItem(String column, String type, String value, String operator) {
        this.column = column;
        this.type = type;
        this.value = value;
        this.operator = operator;
    }

    public String getColumn() {
        return this.column;
    }

    public String getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }

    public String getOperator() {
        return this.operator;
    }

    public FilterItem setColumn(String column) {
        this.column = column;
        return this;
    }

    public FilterItem setType(String type) {
        this.type = type;
        return this;
    }

    public FilterItem setValue(String value) {
        this.value = value;
        return this;
    }

    public FilterItem setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof FilterItem)) {
            return false;
        } else {
            FilterItem other = (FilterItem)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                String this$type;
                String other$type;
                label51: {
                    this$type = this.getColumn();
                    other$type = other.getColumn();
                    if (this$type == null) {
                        if (other$type == null) {
                            break label51;
                        }
                    } else if (this$type.equals(other$type)) {
                        break label51;
                    }

                    return false;
                }

                this$type = this.getType();
                other$type = other.getType();
                if (this$type == null) {
                    if (other$type != null) {
                        return false;
                    }
                } else if (!this$type.equals(other$type)) {
                    return false;
                }

                Object this$value = this.getValue();
                Object other$value = other.getValue();
                if (this$value == null) {
                    if (other$value != null) {
                        return false;
                    }
                } else if (!this$value.equals(other$value)) {
                    return false;
                }

                Object this$operator = this.getOperator();
                Object other$operator = other.getOperator();
                if (this$operator == null) {
                    if (other$operator != null) {
                        return false;
                    }
                } else if (!this$operator.equals(other$operator)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof FilterItem;
    }

    public int hashCode() {
        int result = 1;
        Object $column = this.getColumn();
        result = result * 59 + ($column == null ? 43 : $column.hashCode());
        Object $type = this.getType();
        result = result * 59 + ($type == null ? 43 : $type.hashCode());
        Object $value = this.getValue();
        result = result * 59 + ($value == null ? 43 : $value.hashCode());
        Object $operator = this.getOperator();
        result = result * 59 + ($operator == null ? 43 : $operator.hashCode());
        return result;
    }

    public String toString() {
        return "FilterItem(column=" + this.getColumn() + ", type=" + this.getType() + ", value=" + this.getValue() + ", operator=" + this.getOperator() + ")";
    }
}