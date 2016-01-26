#include <stdio.h>
#include <stdlib.h>
#include "sqlite3/sqlite3.h"
#include "sqlite_verify.h"

int main (int argc, char **argv)
{
    if(argc < 3)
    {
        fprintf(stderr, "%s: missing required parameters. Aborting.\n", "sql_cmd");
        exit(0);
    }

    int verified = verify(argv[1]);
    if(verified < 1)
    {
        fprintf(stderr, "%s: file is not database. Aborting.\n", "sql_cmd");
        exit(0);
    }

    sqlite3 *database;

    if (sqlite3_open(argv[1], &database) == SQLITE_OK)
    {
        sqlite3_stmt *stmt;
        char *errMsg;
        if (sqlite3_prepare_v2(database, argv[2], -1, &stmt, NULL) == SQLITE_OK)
        {
            int columnCount = sqlite3_column_count(stmt);
            int i = 0;
            for(i = 0; i < columnCount; i++)
            {
                printf("%s%c", sqlite3_column_name(stmt, i), i == (columnCount - 1) ? '\0' : '|');
            }
            printf("\n");
            while (sqlite3_step(stmt) == SQLITE_ROW)
            {
                i = 0;
                for(i = 0; i < columnCount; i++)
                {
                    printf("%s%c", sqlite3_column_text(stmt, i), i == (columnCount - 1) ? '\0' : '|');
                }
                printf("\n");
            }
            sqlite3_reset(stmt);
        }
        else
        {
            fprintf(stderr, "%s: command failed with error: %s. Aborting.\n", "sql_cmd", sqlite3_errmsg(database));
            exit(0);
        }
    }
    else
    {
        perror("sql_cmd");
    }
    sqlite3_close(database);
}