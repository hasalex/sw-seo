package fr.sewatech.seo;

public class Main {

  //private static final String ROOT_URL = "localhost:4004";
  //private static final String ROOT_URL = "www.sewatech.fr";
  private static final String HOSTNAME = "www.jtips.info";

  public static void main(String[] args) {
    Site site = Site.withHost(HOSTNAME);
    site.navigate();

    System.out.println(site.getPages().size() + " pages");
    site.getPages()
        .keySet()
        .stream().sorted()
        .forEach(url -> System.out.printf("%s => %s%n", url, site.getPages().get(url)));
  }

}
