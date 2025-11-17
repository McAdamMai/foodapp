package com.foodapp.promotion_service.persistence.entity;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ScopeMemberEntity {
    private String memberId;
    private String scopeId;
    private String memberName;
}
