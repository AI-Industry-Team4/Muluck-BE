package com.muluck.domain;

public enum CaseType {
    CERTAIN_DISEASE,      // 확실한 병충해 (top1 >= 0.6, healthy 아님)
    CANDIDATES_3,         // 병충해 의심 (0.3 <= top1 < 0.6)
    CANDIDATES_2_RETAKE,  // 판단 불가 + 재촬영 권장 (0.2 <= top1 < 0.3)
    UNDETERMINED_RETAKE,  // 완전히 판단 불가 (top1 < 0.2)
    HEALTHY               // 작물이 건강함 (top1 >= 0.6 이면서 healthy)
}
