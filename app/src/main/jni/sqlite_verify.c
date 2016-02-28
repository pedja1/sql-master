#include <stdio.h>
#include <string.h>

int verify(char *fileToVerify)
{
    char buffer;
    char bytes[17];
    FILE *f;
    int result = 0;
    if ((f = fopen(fileToVerify, "r")))
    {
        int count = 0;
        while(fread(&buffer, 1, 1, f) > 0 && count < 16)
        {
            bytes[count] = buffer;
            count++;
        }
        bytes[count] = '\0';

        result = strcmp(bytes, "SQLite format 3") == 0 ? 1 : 0;

        fclose(f);
    }
    else
    {
        return -1;
    }
    return result;
}

