package ru.anarok.audit.internal;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.anarok.audit.ClickhouseDatatype;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ColumnSchema {
    private String name;
    private String type;
    private String defaultType;
    private String defaultValue;
}
