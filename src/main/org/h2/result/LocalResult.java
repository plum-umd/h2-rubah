/*
 * Copyright 2004-2008 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.result;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.h2.constant.SysProperties;
import org.h2.engine.Database;
import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.expression.ExpressionColumn;
import org.h2.message.Message;
import org.h2.table.Column;
import org.h2.util.ObjectArray;
import org.h2.util.ValueHashMap;
import org.h2.value.DataType;
import org.h2.value.Value;
import org.h2.value.ValueArray;

/**
 * A local result set contains all row data of a result set.
 * This is the object generated by engine,
 * and it is also used directly by the ResultSet class in the embedded mode.
 * If the result does not fit in memory, it is written to a temporary file.
 */
public class LocalResult implements ResultInterface {

    private int maxMemoryRows;
    private Session session;
    private int visibleColumnCount;
    private Expression[] expressions;
    private int rowId, rowCount;
    private ObjectArray rows;
    private SortOrder sort;
    private ValueHashMap distinctRows;
    private Value[] currentRow;
    private int offset, limit;
    private ResultExternal disk;
    private int diskOffset;
    private boolean distinct;
    private boolean closed;

    /**
     * Construct a local result object.
     */
    public LocalResult() {
        // nothing to do
    }

    /**
     * Construct a local result object.
     *
     * @param session the session
     * @param expressions the expression array
     * @param visibleColumnCount the number of visible columns
     */
    public LocalResult(Session session, Expression[] expressions, int visibleColumnCount) {
        this.session = session;
        if (session == null) {
            this.maxMemoryRows = Integer.MAX_VALUE;
        } else {
            this.maxMemoryRows = session.getDatabase().getMaxMemoryRows();
        }
        rows = new ObjectArray();
        this.visibleColumnCount = visibleColumnCount;
        rowId = -1;
        this.expressions = expressions;
    }

    /**
     * Construct a local result object.
     *
     * @param session the session
     * @param expressionList the expression list
     * @param visibleColumnCount the number of visible columns
     */
    public LocalResult(Session session, ObjectArray expressionList, int visibleColumnCount) {
        this(session, getList(expressionList), visibleColumnCount);
    }

    /**
     * Construct a local result set by reading all data from a regular result set.
     *
     * @param session the session
     * @param rs the result set
     * @param maxrows the maximum number of rows to read (0 for no limit)
     * @return the local result set
     */
    public static LocalResult read(Session session, ResultSet rs, int maxrows) throws SQLException {
        ObjectArray cols = getExpressionColumns(session, rs);
        int columnCount = cols.size();
        LocalResult result = new LocalResult(session, cols, columnCount);
        for (int i = 0; (maxrows == 0 || i < maxrows) && rs.next(); i++) {
            Value[] list = new Value[columnCount];
            for (int j = 0; j < columnCount; j++) {
                int type = result.getColumnType(j);
                list[j] = DataType.readValue(session, rs, j + 1, type);
            }
            result.addRow(list);
        }
        result.done();
        return result;
    }

    private static ObjectArray getExpressionColumns(Session session, ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        ObjectArray cols = new ObjectArray(columnCount);
        Database db = session == null ? null : session.getDatabase();
        for (int i = 0; i < columnCount; i++) {
            String name = meta.getColumnLabel(i + 1);
            int type = DataType.convertSQLTypeToValueType(meta.getColumnType(i + 1));
            int precision = meta.getPrecision(i + 1);
            int scale = meta.getScale(i + 1);
            int displaySize = meta.getColumnDisplaySize(i + 1);
            Column col = new Column(name, type, precision, scale, displaySize);
            Expression expr = new ExpressionColumn(db, col);
            cols.add(expr);
        }
        return cols;
    }

    /**
     * Create a shallow copy of the result set. The data and a temporary table
     * (if there is any) is not copied.
     *
     * @param session the session
     * @return the copy
     */
    public LocalResult createShallowCopy(Session session) {
        if (disk == null && (rows == null || rows.size() < rowCount)) {
            return null;
        }
        LocalResult copy = new LocalResult();
        copy.maxMemoryRows = this.maxMemoryRows;
        copy.session = session;
        copy.visibleColumnCount = this.visibleColumnCount;
        copy.expressions = this.expressions;
        copy.rowId = -1;
        copy.rowCount = this.rowCount;
        copy.rows = this.rows;
        copy.sort = this.sort;
        copy.distinctRows = this.distinctRows;
        copy.distinct = distinct;
        copy.currentRow = null;
        copy.offset = 0;
        copy.limit = 0;
        copy.disk = this.disk;
        copy.diskOffset = this.diskOffset;
        return copy;
    }

    private static Expression[] getList(ObjectArray expressionList) {
        Expression[] expressions = new Expression[expressionList.size()];
        expressionList.toArray(expressions);
        return expressions;
    }

    /**
     * Set the sort order.
     *
     * @param sort the sort order
     */
    public void setSortOrder(SortOrder sort) {
        this.sort = sort;
    }

    /**
     * Remove duplicate rows.
     */
    public void setDistinct() {
        distinct = true;
        distinctRows = new ValueHashMap(session.getDatabase());
    }

    /**
     * Remove the row from the result set if it exists.
     *
     * @param values the row
     */
    public void removeDistinct(Value[] values) throws SQLException {
        if (!distinct) {
            Message.throwInternalError();
        }
        if (distinctRows != null) {
            ValueArray array = ValueArray.get(values);
            distinctRows.remove(array);
            rowCount = distinctRows.size();
        } else {
            rowCount = disk.removeRow(values);
        }
    }

    /**
     * Check if this result set contains the given row.
     *
     * @param values the row
     * @return true if the row exists
     */
    public boolean containsDistinct(Value[] values) throws SQLException {
        if (!distinct) {
            Message.throwInternalError();
        }
        if (distinctRows != null) {
            ValueArray array = ValueArray.get(values);
            return distinctRows.get(array) != null;
        }
        return disk.contains(values);
    }

    public void reset() throws SQLException {
        rowId = -1;
        if (disk != null) {
            disk.reset();
            if (diskOffset > 0) {
                for (int i = 0; i < diskOffset; i++) {
                    disk.next();
                }
            }
        }
    }

    public Value[] currentRow() {
        return currentRow;
    }

    public boolean next() throws SQLException {
        if (rowId < rowCount) {
            rowId++;
            if (rowId < rowCount) {
                if (disk != null) {
                    currentRow = disk.next();
                } else {
                    currentRow = (Value[]) rows.get(rowId);
                }
                return true;
            }
            currentRow = null;
        }
        return false;
    }

    public int getRowId() {
        return rowId;
    }

    /**
     * Add a row to this object.
     *
     * @param values the row to add
     */
    public void addRow(Value[] values) throws SQLException {
        if (distinct) {
            if (distinctRows != null) {
                ValueArray array = ValueArray.get(values);
                distinctRows.put(array, values);
                rowCount = distinctRows.size();
                if (rowCount > SysProperties.MAX_MEMORY_ROWS_DISTINCT && session.getDatabase().isPersistent()) {
                    disk = new ResultTempTable(session, sort);
                    disk.addRows(distinctRows.values());
                    distinctRows = null;
                }
            } else {
                rowCount = disk.addRow(values);
            }
            return;
        }
        rows.add(values);
        rowCount++;
        if (rows.size() > maxMemoryRows && session.getDatabase().isPersistent()) {
            if (disk == null) {
                disk = new ResultDiskBuffer(session, sort, values.length);
            }
            addRowsToDisk();
        }
    }

    private void addRowsToDisk() throws SQLException {
        disk.addRows(rows);
        rows.clear();
    }

    public int getVisibleColumnCount() {
        return visibleColumnCount;
    }

    /**
     * This method is called after all rows have been added.
     */
    public void done() throws SQLException {
        if (distinct) {
            if (distinctRows != null) {
                rows = distinctRows.values();
                distinctRows = null;
            } else {
                if (disk != null && sort != null) {
                    // external sort
                    ResultExternal temp = disk;
                    disk = null;
                    temp.reset();
                    rows = new ObjectArray();
                    // TODO use offset directly if possible
                    while (true) {
                        Value[] list = temp.next();
                        if (list == null) {
                            break;
                        }
                        if (disk == null) {
                            disk = new ResultDiskBuffer(session, sort, list.length);
                        }
                        rows.add(list);
                        if (rows.size() > maxMemoryRows) {
                            disk.addRows(rows);
                            rows.clear();
                        }
                    }
                    temp.close();
                    // the remaining data in rows is written in the following lines
                }
            }
        }
        if (disk != null) {
            addRowsToDisk();
            disk.done();
        } else {
            if (sort != null) {
                sort.sort(rows);
            }
        }
        applyOffset();
        applyLimit();
        reset();
    }

    public int getRowCount() {
        return rowCount;
    }

    /**
     * Set the number of rows that this result will return at the maximum.
     *
     * @param limit the limit
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    private void applyLimit() {
        if (limit <= 0) {
            return;
        }
        if (disk == null) {
            if (rows.size() > limit) {
                rows.removeRange(limit, rows.size());
                rowCount = limit;
            }
        } else {
            if (limit < rowCount) {
                rowCount = limit;
            }
        }
    }

    /**
     * Check if this result set is buffered using a temporary file.
     *
     * @return true if it is
     */
    public boolean needToClose() {
        return disk != null;
    }

    public void close() {
        if (disk != null) {
            disk.close();
            disk = null;
            closed = true;
        }
    }

    public String getAlias(int i) {
        return expressions[i].getAlias();
    }

    public String getTableName(int i) {
        return expressions[i].getTableName();
    }

    public String getSchemaName(int i) {
        return expressions[i].getSchemaName();
    }

    public int getDisplaySize(int i) {
        return expressions[i].getDisplaySize();
    }

    public String getColumnName(int i) {
        return expressions[i].getColumnName();
    }

    public int getColumnType(int i) {
        return expressions[i].getType();
    }

    public long getColumnPrecision(int i) {
        return expressions[i].getPrecision();
    }

    public int getNullable(int i) {
        return expressions[i].getNullable();
    }

    public boolean isAutoIncrement(int i) {
        return expressions[i].isAutoIncrement();
    }

    public int getColumnScale(int i) {
        return expressions[i].getScale();
    }

    /**
     * Set the offset of the first row to return.
     *
     * @param offset the offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    private void applyOffset() {
        if (offset <= 0) {
            return;
        }
        if (disk == null) {
            if (offset >= rows.size()) {
                rows.clear();
                rowCount = 0;
            } else {
                // avoid copying the whole array for each row
                int remove = Math.min(offset, rows.size());
                rows.removeRange(0, remove);
                rowCount -= remove;
            }
        } else {
            if (offset >= rowCount) {
                rowCount = 0;
            } else {
                diskOffset = offset;
                rowCount -= offset;
            }
        }
    }

    public String toString() {
        return "columns: " + visibleColumnCount + " rows: " + rowCount + " pos: " + rowId;
    }

    /**
     * Check if this result set is closed.
     *
     * @return true if it is
     */
    public boolean isClosed() {
        return closed;
    }

    public int getFetchSize() {
        return 0;
    }

    public void setFetchSize(int fetchSize) {
        // ignore
    }

}
