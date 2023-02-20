package com.hyperlink.server.domain.memberContent.entity;

import lombok.Getter;

@Getter
public enum MemberContentActionType {

  LIKE(1), SUBSCRIPTION(2);

  final int type;

  MemberContentActionType(int type) {
    this.type = type;
  }
}
