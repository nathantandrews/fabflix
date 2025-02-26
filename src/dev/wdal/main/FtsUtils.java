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

public class FtsUtils
{
    private static final String FULL_QUERY =
        "WITH Combined AS (" +
        "  SELECT movie_id, movie_title, movie_year, 1 AS query_order " +
        "  FROM " +
        "  (" +
        "    SELECT m.id AS movie_id, m.title AS movie_title, m.year as movie_year " +
        "    FROM movies m " +
        "    WHERE m.title=? " +
        "    ORDER BY movie_title, movie_year DESC" +
        "  ) AS t1 " +
        "  UNION ALL " +
        "  SELECT movie_id, movie_title, movie_year, 2 AS query_order " +
        "  FROM " +
        "  (" +
        "    SELECT m.id AS movie_id, m.title AS movie_title, m.year as movie_year, MATCH (m.title) AGAINST ( ? IN BOOLEAN MODE) as score " +
        "    FROM movies m " +
        "    WHERE MATCH (m.title) AGAINST ( ? IN BOOLEAN MODE) " +
        "    ORDER BY score DESC, movie_title, movie_year DESC" +
        "  ) AS t2 " +
        "  UNION ALL " +
        "  SELECT movie_id, movie_title, movie_year, 3 AS query_order " +
        "  FROM " +
        "  (" +
        "    SELECT m.id AS movie_id, m.title AS movie_title, m.year as movie_year, ed(LOWER(m.title), ?)/? AS score " +
        "    FROM movies m " +
        "    WHERE m.title LIKE ? OR edth(LOWER(m.title), ?, ?) " +
        "    ORDER BY score DESC, movie_title, movie_year DESC" +
        "  ) AS t3 " +
        "), " +
        "Ranked AS (" +
        "  SELECT movie_id, movie_title, movie_year, query_order, ROW_NUMBER() " +
        "  OVER (PARTITION BY movie_id, movie_title, movie_year ORDER BY query_order) AS row_num " +
        "  FROM Combined AS c" +
        ") " +
        "SELECT movie_id, movie_title, movie_year " +
        "FROM Ranked AS r " +
        "WHERE row_num = 1 " +
        "ORDER BY query_order";

    private static final String AUTOCOMPLETE_QUERY = FULL_QUERY + " LIMIT 10";

    private static void autocompleteResultsToJson(ResultSet rs, JsonWriter jw) throws IOException, SQLException
    {
        String displayString = rs.getString("movie_title") + " (" + rs.getInt("movie_year") + ")";
        jw.beginObject();
        jw.name("value").value(displayString);
        jw.name("data").value(rs.getString("movie_id"));
        jw.endObject();
    }

    public static void search(DataSource dataSource, String queryString, JsonWriter jw, boolean autocomplete)
    { 
        Scanner in = new Scanner(queryString);
        Stream<String> sin = in.tokens();
        String ftsTerms = sin.collect(StringBuilder::new, (sb, element) -> {
            sb.append("+").append(element).append("* "); }, StringBuilder::append).toString().trim();
        sin.close();
        in.close();
        System.out.println(ftsTerms);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(autocomplete ? AUTOCOMPLETE_QUERY : FULL_QUERY))
        {
            
            ps.setString(1, queryString);
            ps.setString(2, ftsTerms);
            ps.setString(3, ftsTerms);
            ps.setString(4, queryString.toLowerCase());
            ps.setInt(5, queryString.length());
            ps.setString(6, "%" + queryString.toLowerCase() + "%");
            ps.setString(7, queryString.toLowerCase());
            ps.setInt(8, 3);
            // fuzzyStmt.setInt(3, queryString.length() / 3);
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

