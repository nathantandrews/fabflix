package dev.wdal.main;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.stream.Stream;

public class SearchUtils
{
    public static final String RELEVANCE_CTE =
        "WITH movies_scored AS (" +
          "SELECT id, title, year, director, cost, " +
                "CASE " +
                    "WHEN (title=?) THEN 1.0 " +
                    "WHEN MATCH (title) AGAINST (? IN BOOLEAN MODE) THEN ((MATCH (title) AGAINST (? IN BOOLEAN MODE))/?)*? " +
                    "WHEN LOWER(title) LIKE LOWER(?) THEN (LOWER(title) LIKE LOWER(?))*? " +
                    "WHEN edth(LOWER(title), ?, ?) THEN (1.0 - ed(LOWER(title), ?)/?)*? " +
                    "ELSE 0 " +
                "END AS relevance " +
        "  FROM movies " +
        ") ";

    private static final String FULL_QUERY = RELEVANCE_CTE +
        "SELECT m.id, title, m.year " +
        "FROM movies_scored AS m " +
        "WHERE relevance > 0 " +
        "ORDER BY relevance DESC, title ASC, m.year DESC ";

    private static final String AUTOCOMPLETE_QUERY = FULL_QUERY + " LIMIT 10";

    private static void autocompleteResultsToJson(ResultSet rs, JsonWriter jw) throws IOException, SQLException
    {
        String displayString = rs.getString("title") + " (" + rs.getInt("m.year") + ")";
        jw.beginObject();
        jw.name("value").value(displayString);
        jw.name("data").value(rs.getString("m.id"));
        jw.endObject();
    }

    public static final double ED_THRESHOLD = 0.5;
    public static final double FTS_CONTRIB_FACTOR = 0.9;
    public static final double FUZZ_A_CONTRIB_FACTOR = 0.75;
    public static final double FUZZ_B_CONTRIB_FACTOR = 1.0;

    public static final String MAX_FTS_RELEVANCE_QUERY =
        "WITH movies_scored AS (SELECT (MATCH (title) AGAINST (? IN BOOLEAN MODE)) AS score FROM movies) SELECT MAX(score) AS max_score FROM movies_scored AS m WHERE score > 0 GROUP BY score ORDER BY score DESC LIMIT 1";

    public static double computeMaxFtsRelevance(Connection conn, String queryTerms)
    {
        double maxRelevance = 1.0;
        try (PreparedStatement ps = conn.prepareStatement(MAX_FTS_RELEVANCE_QUERY))
        {
            ps.setString(1, queryTerms);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    maxRelevance = rs.getDouble("max_score");
                    System.out.println("FTS max score: " + maxRelevance);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return maxRelevance;
    }

    public static void setFetchParameters(PreparedStatement ps, String keywords, String queryTerms, double threshold, double maxFtsRelevance) throws SQLException
    {
        int i = 1;
        ps.setString(i++, keywords); // exact match constraint
        ps.setString(i++, queryTerms); // fts when condition
        ps.setString(i++, queryTerms); // fts score
        ps.setDouble(i++, maxFtsRelevance); // scale by max fts score
        ps.setDouble(i++, SearchUtils.FTS_CONTRIB_FACTOR); // scale by fts contribution factor
        ps.setString(i++, keywords + "%"); // fuzzy a when condition
        ps.setString(i++, keywords + "%"); // fuzzy a score
        ps.setDouble(i++, SearchUtils.FUZZ_A_CONTRIB_FACTOR); // scale by fuzzy A contribution factor
        ps.setString(i++, keywords); // fuzzy b edth arg 2
        ps.setInt(i++, (int)(threshold * keywords.length())); // fuzzy b threshold
        ps.setString(i++, keywords); // fuzzy b distance arg2
        ps.setInt(i++, keywords.length()); // fuzzy b score length scaler
        ps.setDouble(i++, SearchUtils.FUZZ_B_CONTRIB_FACTOR); // scale by fuzzy B contribution factor
    }

    public static void search(DataSource dataSource, String queryString, JsonWriter jw)
    { 
        Scanner in = new Scanner(queryString);
        Stream<String> sin = in.tokens();
        String ftsTerms = sin.collect(StringBuilder::new, (sb, element) -> {
            sb.append("+").append(element).append("* "); }, StringBuilder::append).toString().trim();
        sin.close();
        in.close();
        System.out.println(ftsTerms);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(AUTOCOMPLETE_QUERY))
        {
            setFetchParameters(ps, queryString, ftsTerms, SearchUtils.ED_THRESHOLD, computeMaxFtsRelevance(conn, ftsTerms));
            try (ResultSet rs = ps.executeQuery()) {
                jw.beginArray();
                while (rs.next())
                {
                    autocompleteResultsToJson(rs, jw);
                }
                jw.endArray();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

