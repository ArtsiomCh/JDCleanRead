package com.github.artsiomch.jdcr.utils;

import org.jetbrains.annotations.NotNull;

public class Tag {

  @NotNull private String open;
  @NotNull private String close;

  public Tag(@NotNull String openTag, @NotNull String closeTag) {
    this.open = removeWS(openTag);
    this.close = removeWS(closeTag);
  }

  public boolean openIn(@NotNull String text){
    return removeWS(text).contains(this.open);
  }

  public boolean closeIn(@NotNull String text){
    return removeWS(text).contains(this.close);
  }

  @NotNull
  private String removeWS(@NotNull String text){
    StringBuilder result = new StringBuilder();
    for (char c : text.toCharArray()) {
        if (c!=' ') result.append(c);
    }
    return result.toString();
  }
}
