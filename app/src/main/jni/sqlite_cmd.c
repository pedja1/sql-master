#define _GNU_SOURCE

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "sqlite3/sqlite3.h"
#include "sqlite_verify.h"

int isCountableUpdateOperation(char *sql)
{
    return sql == strcasestr(sql, "update") || sql == strcasestr(sql, "delete") || sql == strcasestr(sql, "insert");
    //return sql == strstr(sql, "update") || sql == strstr(sql, "delete") || sql == strstr(sql, "insert");
}

int main (int argc, char **argv)
{
    if(argc < 3)
    {
        fprintf(stderr, "%s: missing required parameters. Aborting.\n", "sql_cmd");
        return 1;
    }

    int verified = verify(argv[1]);
    if(verified < 1)
    {
        fprintf(stderr, "%s: file is not database. Aborting.\n", "sql_cmd");
        return 1;
    }

    sqlite3 *database;

    if (sqlite3_open(argv[1], &database) == SQLITE_OK)
    {
        sqlite3_stmt *stmt;
        if (sqlite3_prepare_v2(database, argv[2], -1, &stmt, NULL) == SQLITE_OK)
        {
            int columnCount = sqlite3_column_count(stmt);
            int i = 0;
            for(i = 0; i < columnCount; i++)
            {
                printf("header:%s%c", sqlite3_column_name(stmt, i), i == (columnCount - 1) ? '\0' : '|');
            }
            if(columnCount > 0)printf("\n");
            while (sqlite3_step(stmt) == SQLITE_ROW)
            {
                i = 0;
                for(i = 0; i < columnCount; i++)
                {
                    const unsigned char *text;

                    if(sqlite3_column_type(stmt, i) == SQLITE_BLOB)
                    {
                        text = (const unsigned char *) "{data}";
                    }
                    else
                    {
                        text = sqlite3_column_text(stmt, i);
                    }

                    printf("row:%s%c", text, i == (columnCount - 1) ? '\0' : '|');
                }
                printf("\n");
            }
            int affectedRows = sqlite3_changes(database);

            if(isCountableUpdateOperation(argv[2]))
            {
                printf("message:Operation successful. Number of rows affected: %i\n", affectedRows);
            }
            else if(columnCount == 0)
            {
                printf("\n");
            }
            sqlite3_finalize(stmt);
        }
        else
        {
            sqlite3_close(database);
            fprintf(stderr, "%s: command failed with error: %s. Aborting.\n", "sql_cmd", sqlite3_errmsg(database));
            return 1;
        }
    }
    else
    {
        perror("sql_cmd");
    }
    sqlite3_close(database);
    return 0;
}