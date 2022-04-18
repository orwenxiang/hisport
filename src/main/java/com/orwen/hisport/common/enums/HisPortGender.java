package com.orwen.hisport.common.enums;

import com.orwen.hisport.utils.EnumStrTyped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HisPortGender implements EnumStrTyped<HisPortGender> {

    UNKNOWN("0"),

    MAN("1"),

    WOMAN("2"),
    ;

    private final String type;

    //TODO
    public static HisPortGender ofHxHisCode(String code) {
        return HisPortGender.UNKNOWN;
    }

    public static final class Deserializer extends EnumStrTyped.Deserializer<HisPortGender> {

    }
}
