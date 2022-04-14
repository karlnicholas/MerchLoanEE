package com.github.karlnicholas.merchloan.accounts.model;

import org.apache.logging.log4j.util.Strings;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StatementDatesConverter {
    public String convertToDatabaseColumn(List<LocalDate> list) {
        if (list == null)
            return null;
        return Strings.join(list, ',');
    }

    public List<LocalDate> convertToEntityAttribute(String dbJson) {
        if (dbJson == null)
            return Collections.emptyList();
        return Arrays.stream(dbJson.split(","))
                .map(LocalDate::parse)
                .collect(Collectors.toList());
    }
}
