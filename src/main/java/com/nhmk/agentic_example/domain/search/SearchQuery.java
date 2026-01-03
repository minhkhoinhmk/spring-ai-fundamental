package com.nhmk.agentic_example.domain.search;

public final class SearchQuery {
    private final String q;
    private final String country;
    private final String language;
    private final String dateRestrict;
    private final int num;

    public SearchQuery(String q, String country, String language, String dateRestrict, int num) {
        this.q = q;
        this.country = country;
        this.language = language;
        this.dateRestrict = dateRestrict;
        this.num = num;
    }

    public String q() { return q; }
    public String country() { return country; }
    public String language() { return language; }
    public String dateRestrict() { return dateRestrict; }
    public int num() { return num; }
}
