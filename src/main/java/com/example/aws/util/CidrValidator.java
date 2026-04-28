package com.example.aws.util;

import java.util.regex.Pattern;

  public class CidrValidator {

      private static final Pattern CIDR_PATTERN = Pattern.compile(
          "^((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])/([0-9]|[1-2][0-9]|3[0-2])$"
      );

      public static boolean isValidCidr(String cidr) {
          return cidr != null && CIDR_PATTERN.matcher(cidr).matches();
      }
  }
