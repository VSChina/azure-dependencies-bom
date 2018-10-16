package com.microsoft.azure.dependencies.verify.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static <T> List<T> toDistinctList(List<T> list) {
        LinkedHashSet<T> set = new LinkedHashSet<>(list.size());

        set.addAll(list);

        return new ArrayList<>(set);
    }

}
