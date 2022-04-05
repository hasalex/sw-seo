package fr.sewatech.seo;

import java.net.URI;

final class Link {

  private final URI url;
  private final String text;
  private int status;

  Link(URI url, String text) {
    this.url = url;
    this.text = text;
  }

  Link(String url, String text) {
    this(URI.create(url), text);
  }

  public String getHost() {
    return url.getHost();
  }

  public boolean isRelative() {
    return !url.isAbsolute();
  }

  public URI url() {
    return url;
  }

  public String text() {
    return text;
  }

  public int status() {
    return status;
  }

  public void status(int status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "Link{" +
        "url=" + url.getPath() +
        ", text='" + text + '\'' +
        '}';
  }

}
