package fr.sewatech.seo;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

class Page {

  private String url;
  private String title;
  private String description;

  private Links links;
  private Page parent;

  public static Page empty(Link link) {
    Page page = new Page();
    page.links = new Links();
    if (link != null) {
      page.title = link.url().getPath();
    }
    return page;
  }

  public static Page root(Site site) {
    String hostname = site.getHostname();
    if (hostname.startsWith("localhost")) {
      return of(null, "http://" + hostname, null);
    } else {
      return of(null, "https://" + hostname, null);
    }
  }

  private static Page of(Link link, Page parent) {
    Page page = of(link, parent.url, parent);
    return page;
  }

  private static Page of(Link link, String parentUrl, Page parent) {
    String pageUrl = link == null ? parentUrl : URI.create(parentUrl).resolve(link.url()).toString();
    try {
      Page page = of(Jsoup.connect(pageUrl).get(), parent);
      updateStatus(link, HTTP_OK);
      return page;
    } catch (UnsupportedMimeTypeException e) {
      Page page = Page.empty(link);
      updateStatus(link, HTTP_OK);
      return page;
    } catch (HttpStatusException e) {
      if (e.getStatusCode() == 404) {
        updateStatus(link, HTTP_NOT_FOUND);
        System.err.println(link);
        return null;
      } else {
        throw new SeoException(e);
      }
    } catch (IllegalArgumentException e) {
      return Page.empty(link);
    } catch (IOException e) {
      throw new SeoException(e);
    }
  }

  private static Page of(Document document, Page parent) {
    Page page = new Page();
    page.title = document.title();
    //page.description = document.head()
    page.url = document.baseUri();

    String host = page.getHost();
    Elements links = document.body().select("a");
    page.links = new Links(
        links.stream()
            .map(link -> new Link(link.attr("href"), link.text()))
            .filter(Link::isRelative)
            .filter(link -> !".".equals(link.url().getPath()))
            .filter(link -> !link.url().toString().startsWith("#"))
            .collect(Collectors.toSet()),
        links.stream()
            .map(link -> new Link(link.attr("href"), link.text()))
            .filter(link -> host.equals(link.getHost()))
            .collect(Collectors.toSet()),
        links.stream()
            .map(link -> new Link(link.attr("href"), link.text()))
            .filter(link -> !link.isRelative() && !host.equals(link.getHost()))
            .collect(Collectors.toSet())
    );
    return page;
  }

  private String getHost() {
    return URI.create(url).getHost();
  }

  private static void updateStatus(Link link, int status) {
    if (link != null) {
      link.status(status);
    }
  }

  public Map<URI, Page> navigateSiteLinks(Set<URI> excludedUris) {
    return
        Stream.concat(
                this.links.relative.stream(),
                this.links.internal.stream()
            )
            .filter(link -> !excludedUris.contains(link.url()))
            .map(link -> new LinkedPage(link.url(), Page.of(link, this)))
            .filter(linkedPage -> linkedPage.page() != null)
            .collect(Collectors.toMap(
                LinkedPage::uri,
                LinkedPage::page,
                (page, ignored) -> page
            ));
  }

  public String getTitle() {
    return title;
  }

  @Override
  public String toString() {
    return "Page{" +
        "url='" + url + '\'' +
        ", title='" + title + '\'' +
        '}';
  }

  private static record LinkedPage(URI uri, Page page) {
  }

  private static record Links(Set<Link> relative, Set<Link> internal, Set<Link> external) {
    public Links() {
      this(Set.of(), Set.of(), Set.of());
    }
  }

}
