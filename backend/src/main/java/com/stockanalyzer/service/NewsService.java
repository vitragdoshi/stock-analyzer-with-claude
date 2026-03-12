package com.stockanalyzer.service;

import com.stockanalyzer.dto.StockAnalysisResponse.NewsItem;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NewsService {

    private static final String[] CREDIBLE_SOURCES = {
        "Economic Times", "Business Standard", "Mint", "Financial Express",
        "Moneycontrol", "NDTV Profit", "Bloomberg India", "Reuters India"
    };

    private static final String[] LESS_CREDIBLE_SOURCES = {
        "Unknown Blog", "WhatsApp Forward", "Telegram Channel", "Twitter User @StockGuru"
    };

    public List<NewsItem> generateCompanyNews(String symbol, String companyName, Random rng) {
        List<NewsItem> news = new ArrayList<>();

        news.add(createNewsItem(
            companyName + " Q3 Results: Revenue Beats Estimates by 8%, PAT up 22% YoY",
            CREDIBLE_SOURCES[rng.nextInt(CREDIBLE_SOURCES.length)],
            daysAgo(2),
            String.format("%s reported Q3FY25 consolidated revenue of ₹XX,XXX Cr, up 18%% YoY, beating Bloomberg consensus estimate by 8%%. " +
                "PAT came in at ₹X,XXX Cr, up 22%% YoY. EBITDA margins expanded 120 bps to 24.5%%. " +
                "Management guided for 15-18%% revenue growth in FY26, citing strong order book and digital transformation demand.",
                companyName),
            "POSITIVE",
            "HIGH",
            "Published by a SEBI-registered media entity. Financial figures corroborate with BSE filing #" + (100000 + rng.nextInt(900000)) + ". " +
                "Multiple independent sources confirm the same data points.",
            "BULLISH",
            true,
            rng
        ));

        news.add(createNewsItem(
            companyName + " Announces Strategic Partnership with Global Technology Firm",
            CREDIBLE_SOURCES[rng.nextInt(CREDIBLE_SOURCES.length)],
            daysAgo(5),
            companyName + " has signed a multi-year strategic partnership agreement valued at $500M+ with a leading global firm. " +
                "This is expected to add incremental revenue of ₹2,000-3,000 Cr over the next 3 years. " +
                "The deal strengthens the company's capabilities in AI/ML and cloud services.",
            "POSITIVE",
            "HIGH",
            "News confirmed via official press release filed with BSE/NSE. Partnership agreement details verified through company investor relations page. No contradictory information found.",
            "BULLISH",
            true,
            rng
        ));

        news.add(createNewsItem(
            companyName + " Management Insider Buys 50,000 Shares in Open Market",
            CREDIBLE_SOURCES[rng.nextInt(CREDIBLE_SOURCES.length)],
            daysAgo(8),
            "The Managing Director of " + companyName + " purchased 50,000 equity shares through open market transactions, " +
                "as disclosed in SAST/PIT filing with SEBI. Total acquisition value: ~₹XX Cr. " +
                "This signals strong management confidence in the company's near-term prospects.",
            "POSITIVE",
            "HIGH",
            "SEBI filing #SHP-2024-XXXXX verified. Insider purchase disclosures are regulatory mandated and thus highly reliable. " +
                "Cross-verified with BSE bulk deals data.",
            "BULLISH",
            true,
            rng
        ));

        String sentiment4 = rng.nextBoolean() ? "NEGATIVE" : "NEUTRAL";
        news.add(createNewsItem(
            companyName + " Faces Headwinds from Input Cost Inflation; Margin Guidance Cut",
            CREDIBLE_SOURCES[rng.nextInt(CREDIBLE_SOURCES.length)],
            daysAgo(12),
            companyName + " management warned of near-term margin pressure due to elevated input costs and wage inflation. " +
                "EBITDA margin guidance revised downward by 50-80 bps for Q4FY25. " +
                "However, management reiterated full-year revenue guidance, citing strong demand environment.",
            sentiment4,
            "HIGH",
            "Information sourced from official Q3 earnings call transcript available on company IR website. " +
                "Analyst reports from ICICI Securities and Kotak Institutional Equities corroborate the guidance revision.",
            sentiment4.equals("NEGATIVE") ? "BEARISH" : "NEUTRAL",
            true,
            rng
        ));

        // Potentially suspicious news
        news.add(createNewsItem(
            "BREAKING: " + symbol + " Stock Set to 10X! Massive Acquisition Rumor",
            LESS_CREDIBLE_SOURCES[rng.nextInt(LESS_CREDIBLE_SOURCES.length)],
            daysAgo(1),
            "Unverified social media posts claim " + companyName + " is in advanced talks to acquire a major competitor for $10B. " +
                "Stock tipping accounts on Telegram are claiming this will lead to 10X returns. " +
                "No official statement from the company.",
            "POSITIVE",
            "LOW",
            "AUTHENTICITY ALERT: Published on an anonymous blog with no verifiable credentials. " +
                "No official filing with BSE/NSE corroborates this claim. " +
                "No reputable financial media has reported this story. " +
                "Pattern is consistent with potential pump-and-dump misinformation. " +
                "RECOMMENDATION: Treat as UNVERIFIED until official confirmation.",
            "SUSPECT",
            false,
            rng
        ));

        return news;
    }

    public List<NewsItem> generateCompetitorNews(String symbol, List<String> competitors, String sector, Random rng) {
        List<NewsItem> news = new ArrayList<>();

        for (int i = 0; i < Math.min(3, competitors.size()); i++) {
            String competitor = competitors.get(i);

            news.add(createNewsItem(
                competitor + " Reports Strong Quarter — Market Share Pressure on Peers",
                CREDIBLE_SOURCES[rng.nextInt(CREDIBLE_SOURCES.length)],
                daysAgo(3 + i * 2),
                competitor + " posted a blockbuster quarter with 25% revenue growth and market share gains in key segments. " +
                    "Analysts note this could put competitive pressure on " + symbol + " and other sector peers. " +
                    "Pricing pressure may intensify in H1FY26.",
                "NEGATIVE",
                "MEDIUM",
                "Published in reputable financial media. Quarterly results verified against " + competitor + " BSE filings. " +
                    "Impact on " + symbol + " is analyst assessment, not company-stated fact — treat as opinion.",
                "BEARISH",
                true,
                rng
            ));
        }

        if (!competitors.isEmpty()) {
            news.add(createNewsItem(
                "Sector Outlook: " + sector + " — FII Inflows Surge Amid Global Tailwinds",
                CREDIBLE_SOURCES[rng.nextInt(CREDIBLE_SOURCES.length)],
                daysAgo(7),
                "Foreign Institutional Investors pumped ₹8,500 Cr into the " + sector + " sector this week, " +
                    "the highest monthly inflow in 18 months. Global macro tailwinds (Fed rate cuts, INR stability) " +
                    "are driving renewed interest. All sector stocks including " + symbol + " are expected to benefit.",
                "POSITIVE",
                "HIGH",
                "FII/DII flow data verified against NSDL/CDSL depository data. " +
                    "Global macro context corroborated by Bloomberg, Reuters feeds.",
                "BULLISH",
                true,
                rng
            ));
        }

        return news;
    }

    private NewsItem createNewsItem(String headline, String source, String date,
                                     String summary, String sentiment, String authScore,
                                     String authJustification, String impact,
                                     boolean isAuthentic, Random rng) {
        NewsItem item = new NewsItem();
        item.setHeadline(headline);
        item.setSource(source);
        item.setPublishedDate(date);
        item.setSummary(summary);
        item.setSentiment(sentiment);
        item.setAuthenticityScore(authScore);
        item.setAuthenticityJustification(authJustification);
        item.setImpactOnStock(impact);
        item.setAuthentic(isAuthentic);
        item.setUrl("#");
        return item;
    }

    private String daysAgo(int days) {
        return LocalDate.now().minusDays(days).format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }
}
