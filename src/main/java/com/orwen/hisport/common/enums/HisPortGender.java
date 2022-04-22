package com.orwen.hisport.common.enums;

import com.orwen.hisport.utils.EnumStrTyped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum HisPortGender implements EnumStrTyped<HisPortGender> {
    UNKNOWN("0"),

    MAN("1"),

    WOMAN("2"),
    ;

    private final String type;

    public static HisPortGender ofHxHisCode(String code) {
        for (HisPortGender gender : values()) {
            if (Objects.equals(gender.getType(), code)) {
                return gender;
            }
        }
        return HisPortGender.UNKNOWN;
    }

    public static final class Deserializer extends EnumStrTyped.Deserializer<HisPortGender> {

    }
}
