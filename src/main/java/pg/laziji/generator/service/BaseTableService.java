package pg.laziji.generator.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import pg.laziji.generator.model.Column;
import pg.laziji.generator.model.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseTableService implements TableService {


    @Value("${generator.datasource.url}")
    private String url;

    @Value("${generator.datasource.username}")
    private String username;

    @Value("${generator.datasource.password}")
    private String password;

    protected abstract String getDriverClassName();

    @Value("#{'${generator.ignore-col}'.split(',')}")
    private List<String> ignoreColList;

    @Value("${generator.pk-name}")
    private String pkName;

    @Override
    public Table getTable(String tableName) throws Exception {
        try (Connection connection = getConnection()) {
            Table table = getMetaDataTable(connection, tableName);
            if (table == null) {
                return null;
            }
            List<Column> columns = listMetaDataColumn(connection, tableName);
            table.setColumns(columns);
            return table;
        }
    }


    private Table getMetaDataTable(Connection connection, String tableNamePattern) throws SQLException {
        ResultSet resultSet = connection.getMetaData().getTables(
                connection.getCatalog(),
                connection.getSchema(),
                tableNamePattern,
                null);
        if (resultSet.next()) {
            Table table = new Table();
            table.setTableName(resultSet.getString("TABLE_NAME"));
            table.setTableType(resultSet.getString("TABLE_TYPE"));
            table.setTableComment(resultSet.getString("REMARKS"));
            return table;
        }
        return null;
    }

    private List<Column> listMetaDataColumn(Connection connection, String tableNamePattern) throws SQLException {
        ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(),
                connection.getSchema(),
                tableNamePattern,
                null);
        List<Column> columns = new ArrayList<>();
        while (resultSet.next()) {
//            boolean ignoreColBoolean = false;
//            for (String ignore:ignoreColList){
//                if (StringUtils.equals(resultSet.getString("COLUMN_NAME"), ignore)){
//                    ignoreColBoolean = true;
//                    break;
//                }
//            }
//            if (ignoreColBoolean){
//                continue;
//            }

            Column column = new Column();
            column.setIgnore(false);
            for (String ignore:ignoreColList){
                if (StringUtils.equals(resultSet.getString("COLUMN_NAME"), ignore)){
                    column.setIgnore(true);
                    break;
                }
            }
            // 设置其为主键
            if (StringUtils.equals(resultSet.getString("COLUMN_NAME"), pkName)){
                column.setIsPk(true);
            } else {
                column.setIsPk(false);
            }
            column.setTableName(resultSet.getString("TABLE_NAME"));
            column.setColumnName(resultSet.getString("COLUMN_NAME"));
            column.setDataType(resultSet.getString("TYPE_NAME"));
            column.setColumnSize(resultSet.getInt("COLUMN_SIZE"));
            column.setColumnComment(resultSet.getString("REMARKS"));

            String nullAble = resultSet.getString("IS_NULLABLE");
            if (nullAble != null) {
                column.setNullAble("YES".equals(nullAble));
            }
            String autoIncrement = resultSet.getString("IS_AUTOINCREMENT");
            if (autoIncrement != null) {
                column.setAutoIncrement("YES".equals(autoIncrement));
            }
            columns.add(column);
        }
        return columns;
    }

    private Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName(getDriverClassName());
        return DriverManager.getConnection(url, username, password);
    }
}
