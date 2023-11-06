package com.generator.spring.generate.util.impl;

import com.generator.spring.generate.template.seed.ColumnDescription;
import com.generator.spring.generate.template.seed.TableDescription;
import com.generator.spring.generate.util.GenerateDDL;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class GenerateDDLImpl implements GenerateDDL {

    @Override
    public Map<String, List<String>> generate(List<TableDescription> listTable) {
        if (listTable == null) {
            log.error("LIST TABLE NULL");
        }

        Map<String, List<String>> listData = new HashMap<>();
        for (TableDescription tableDescription : listTable) {
            listData.put(tableDescription.getTableName(), getListColumnTable(tableDescription));
        }
        return listData;
    }

    @Override
    public Map<String, List<String>> generate(TableDescription tableDescription) {
        if (tableDescription == null) {
            log.error("TABLE NULL");
        }

        Map<String, List<String>> listData = new HashMap<>();
        listData.put(tableDescription.getTableName(), getListColumnTable(tableDescription));
        return listData;
    }

    public List getListColumnTable(TableDescription tableDescription) {
        if (tableDescription == null) {
            log.error("TABLE DESCRIPTION NULL");
        }
        if (tableDescription.getColumnDescriptions() == null) {
            log.error("LIST COLUMN NULL");
        }
        List listColumn = new ArrayList();
        int count = 0;
        String PK = null;
        for (ColumnDescription columnDescription : tableDescription.getColumnDescriptions()) {
            StringBuffer column = new StringBuffer();
            column.append(columnDescription.getColumnName()).append(" ");
            column.append(getTypeVarchar(columnDescription)).append(" ");
            column.append(columnDescription.isUnique() ? "UNIQUE" : "").append(" ");
            column.append(columnDescription.isNullable() ? "NULL" : "NOT NULL").append(" ");

            PK = columnDescription.isPrimaryKey() ? columnDescription.getColumnName() : PK;

            if (count < tableDescription.getColumnDescriptions().size()) {
                column.append(",");
            }
            count++;
            listColumn.add(column.toString());
        }

        if (Objects.nonNull(PK)) {
            StringBuffer sbPK = new StringBuffer();
            sbPK.append("CONSTRAINT").append(" ");
            sbPK.append(tableDescription.getTableName() + "_pk").append(" ");
            sbPK.append("PRIMARY KEY").append(" ");
            sbPK.append("(").append(PK).append(")");
            listColumn.add(sbPK.toString());
        }
        return listColumn;
    }

    public String getTypeVarchar(ColumnDescription columnDescription) {
        if (columnDescription.getDataType().toLowerCase().equalsIgnoreCase("varchar")) {
            return columnDescription.getDataType() + "(" + columnDescription.getLength().replace("-", "100") + ")";
        } else if (columnDescription.getDataType().toLowerCase().equalsIgnoreCase("date")) {
            return "timestamptz";
        }
        return columnDescription.getDataType();
    }
}
