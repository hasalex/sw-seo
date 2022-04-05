package fr.sewatech.seo;

import java.io.IOException;

public class SeoException extends RuntimeException {
  public SeoException(IOException cause) {
    super(cause);
  }
}
