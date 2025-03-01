package dev.wdal.importer;

import java.util.Iterator;
import java.io.*;
import java.sql.*;
import java.util.Set;
import java.util.HashSet;

public abstract class AbstractManager<T> {
	protected PrintWriter insertionLog;
    protected Set<T> toInsert;
    protected Connection connection;
	protected int lastId;

	protected void init() {
	}

    protected AbstractManager() {
        ImporterCleaner.getCleaner().register(this, this::cleanup);
		try {
			insertionLog = new PrintWriter(new FileOutputStream(getType() + "s_good.log"));
        }
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
        this.toInsert = new HashSet<>();
        try {
            connection = Db.getConnection();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return;
        }
		init();
        load();
    }

	public void cleanup() {
		if (insertionLog != null) {
            insertionLog.flush();
			insertionLog.close();
		}
	}

	protected Iterator<T> getIterator() {
		return toInsert.iterator();
	}

    public void print() {
        Iterator<T> it = getIterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
    }

	protected int getId(T value) {
		return 0;
	}

	protected T fromResult(ResultSet rs) throws SQLException {
        return null;
	}

	protected abstract String getType();

	protected void load() {
		try {
			String query = "SELECT * from " + getType() + "s";
			PreparedStatement ps = connection.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				T value = fromResult(rs);
				System.out.println("db " + getType() + " " + value);
                // no import ids
				int id = getId(value);
				if (lastId < id) {
					lastId = id;
				}
			}
			rs.close();
			ps.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected abstract String toCsv(T value);

	public void dumpCsv() {
        try {
			PrintStream out = new PrintStream(new FileOutputStream(getType() + "s.csv"));
			Iterator<T> it = toInsert.iterator();
			while (it.hasNext()) {
				T value = it.next();
				out.println(toCsv(value));
            }
			out.close();
        }
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void updateDb() {
		try {
			String loadStatement = "LOAD DATA LOCAL INFILE '" + getType() + "s.csv' INTO TABLE moviedb." + getType() + "s FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\\'';";
			Statement s = connection.createStatement();
			s.execute(loadStatement);
            s.close();
        }
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

    public boolean has(T value) {
		return toInsert.contains(value);
	}

	public void add(T value) {
		toInsert.add(value);
		insertionLog.println(value);
		insertionLog.flush();
	}
}
