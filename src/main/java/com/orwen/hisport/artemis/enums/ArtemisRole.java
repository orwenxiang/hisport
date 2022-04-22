package com.orwen.hisport.artemis.enums;

import com.orwen.hisport.utils.EnumStrTyped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum ArtemisRole implements EnumStrTyped<ArtemisRole> {

    /**
     * 医生
     */
    DOCTOR("0"),

    /**
     * 护士
     */
    NURSE("1"),

    /**
     * 护工
     */
    WORKER("2"),

    /**
     * 其它
     */
    OTHER("3"),
    ;

    private final String type;

    public static ArtemisRole ofHxHisCode(String code) {
        for (ArtemisRole gender : values()) {
            if (Objects.equals(gender.getType(), code)) {
                return gender;
            }
        }
        return ArtemisRole.OTHER;
    }

    public static final class Deserializer extends EnumStrTyped.Deserializer<ArtemisRole> {

    }
}
