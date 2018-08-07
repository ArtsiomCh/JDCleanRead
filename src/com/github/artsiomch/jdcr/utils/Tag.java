package com.github.artsiomch.jdcr.utils;

import org.jetbrains.annotations.NotNull;

public class Tag {

  public Tag(@NotNull String openTag, @NotNull String closeTag) {
    this.open = openTag;
    this.close = closeTag;
  }

  @NotNull public String open;
  @NotNull public String close;
}
