package com.orwen.hisport.artemis.enums;

import com.orwen.hisport.utils.EnumStrTyped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

    public static final class Deserializer extends EnumStrTyped.Deserializer<ArtemisRole> {

    }
}
