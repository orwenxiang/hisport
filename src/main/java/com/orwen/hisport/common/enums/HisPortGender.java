package com.orwen.hisport.common.enums;

import com.orwen.hisport.utils.EnumIntTyped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HisPortGender implements EnumIntTyped<HisPortGender> {

    UNKNOWN(0),

    MAN(1),

    WOMAN(2),
    ;

    private final int type;

    //TODO
    public static HisPortGender ofHxHisCode(String code) {
        return HisPortGender.UNKNOWN;
    }
}
