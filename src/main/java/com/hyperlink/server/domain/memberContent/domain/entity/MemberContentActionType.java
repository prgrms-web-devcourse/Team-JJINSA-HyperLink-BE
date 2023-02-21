package com.hyperlink.server.domain.memberContent.domain.entity;

import lombok.Getter;

@Getter
public enum MemberContentActionType {

  LIKE(1), BOOKMARK(2);

  final int typeNumber;

  MemberContentActionType(int typeNumber) {
    this.typeNumber = typeNumber;
  }
}
