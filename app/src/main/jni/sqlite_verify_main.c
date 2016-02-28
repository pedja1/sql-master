#include <stdlib.h>
#include <stdio.h>
#include "sqlite_verify.h"

int main(int argc, char **argv)
{
    if(argc < 2)
    {
        fprintf(stderr, "%s: File to verify is missing. Aborting.\n", "sqlite_verify");
        exit(1);
    }

    char *fileToVerify = argv[1];
    int result = verify(fileToVerify);
    if(result >= 0)
    {
        printf("%i", result);
    }
    else
    {
        perror("sqlite_verify");
    }

    return 0;
}