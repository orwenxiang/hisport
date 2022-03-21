package com.orwen.hisport.common.enums;

import com.orwen.hisport.utils.EnumStrTyped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HisPortVirusCheck implements EnumStrTyped<HisPortVirusCheck> {

    ABNORMAL("0"),


    NORMAL("1"),


    CHECKING("2"),

    ;

    private final String type;
}