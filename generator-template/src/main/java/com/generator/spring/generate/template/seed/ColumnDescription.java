package com.generator.spring.generate.template.seed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnDescription {
    private String columnName;
    private String dataType;
    private boolean isUnique;
    private boolean isNullable;
    private boolean isInsertable;
    private boolean isUpdatable;
    private String length;
    private boolean isPrimaryKey;



    @Override
    public String toString() {
        return "" +
                ", unique=" + isUnique +
                ", nullable=" + isNullable +
                ", insertable=" + isInsertable +
                ", updatable=" + isUpdatable +
                (Objects.nonNull(length) && !"-".equalsIgnoreCase(length) ? ",length='" + length + "'" : "");
    }

}
