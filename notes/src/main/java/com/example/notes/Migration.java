package com.example.notes;

public class Migration {
    public String SQL;
    public String DownSQL;
    public int Version;

    public Migration(int version, String sql, String downSql) {
        SQL = sql;
        DownSQL = downSql;
        Version = version;
    }
}
