package com.generator.spring.generate.util;

import com.generator.spring.generate.template.seed.TableDescription;

import java.util.List;
import java.util.Map;

public interface GenerateEntity {
    public List<TableDescription> generate();
    public String getTypeAttr(String type);
    public String setNameAttr(String input);
}
