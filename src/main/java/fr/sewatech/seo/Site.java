package fr.sewatech.seo;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Site {

  private final String hostname;
  private final Map<URI, Page> pages = new HashMap<>();

  public Site(String hostname) {
    this.hostname = hostname;
  }

  public static Site withHost(String hostname) {
    return new Site(hostname);
  }

  public Site navigate() {
    Page rootPage = Page.root(this);
    pages.putAll(rootPage.navigateSiteLinks(Set.of()));
    int previousSize;
    do {
      previousSize = pages.size();
      navigateNextLevel(pages.keySet());
      System.out.printf("(%s => %s)%n", previousSize, pages.size());
    } while (previousSize < pages.size());

    return this;
  }

  private void navigateNextLevel(Set<URI> excludedUris) {
    Map<URI, Page> nextLevelPages = pages.values().stream()
        .flatMap(page -> page.navigateSiteLinks(excludedUris).entrySet().stream())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (page, ignored) -> page
        ));
    pages.putAll(nextLevelPages);
  }

  public Map<URI, Page> getPages() {
    return pages;
  }

  public String getHostname() {
    return hostname;
  }
}
