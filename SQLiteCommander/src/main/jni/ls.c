#include <dirent.h>
#include <errno.h>
#include <grp.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>

// bits for flags argument
#define LIST_FILTER_MIME_TYPE           (1 << 0)

typedef struct
{
    int flag;
    char *value;
} param;

// simple dynamic array of strings.
typedef struct
{
    int count;
    int capacity;
    void **items;
} strlist_t;
#define STRLIST_INITIALIZER { 0, 0, NULL }
/* Used to iterate over a strlist_t
 * _list   :: pointer to strlist_t object
 * _item   :: name of local variable name defined within the loop with
 *            type 'char*'
 * _stmnt  :: C statement executed in each iteration
 *
 * This macro is only intended for simple uses. Do not add or remove items
 * to/from the list during iteration.
 */
#define  STRLIST_FOREACH(_list, _item, _stmnt) \
    do { \
        int _nn_##__LINE__ = 0; \
        for (;_nn_##__LINE__ < (_list)->count; ++ _nn_##__LINE__) { \
            char* _item = (char*)(_list)->items[_nn_##__LINE__]; \
            _stmnt; \
        } \
    } while (0)

static void dynarray_reserve_more(strlist_t *a, int count)
{
    int old_cap = a->capacity;
    int new_cap = old_cap;
    const int max_cap = INT_MAX / sizeof(void *);
    void **new_items;
    int new_count = a->count + count;
    if (count <= 0)
        return;
    if (count > max_cap - a->count)
        abort();
    new_count = a->count + count;
    while (new_cap < new_count)
    {
        old_cap = new_cap;
        new_cap += (new_cap >> 2) + 4;
        if (new_cap < old_cap || new_cap > max_cap)
        {
            new_cap = max_cap;
        }
    }
    new_items = realloc(a->items, new_cap * sizeof(void *));
    if (new_items == NULL)
        abort();
    a->items = new_items;
    a->capacity = new_cap;
}

void strlist_init(strlist_t *list)
{
    list->count = list->capacity = 0;
    list->items = NULL;
}

// append a new string made of the first 'slen' characters from 'str'
// followed by a trailing zero.
void strlist_append_b(strlist_t *list, const void *str, size_t slen)
{
    char *copy = malloc(slen + 1);
    memcpy(copy, str, slen);
    copy[slen] = '\0';
    if (list->count >= list->capacity)
        dynarray_reserve_more(list, 1);
    list->items[list->count++] = copy;
}

// append the copy of a given input string to a strlist_t.
void strlist_append_dup(strlist_t *list, const char *str)
{
    strlist_append_b(list, str, strlen(str));
}

// note: strlist_done will free all the strings owned by the list.
void strlist_done(strlist_t *list)
{
    STRLIST_FOREACH(list, string, free(string));
    free(list->items);
    list->items = NULL;
    list->count = list->capacity = 0;
}

static int strlist_compare_strings(const void *a, const void *b)
{
    const char *sa = *(const char **) a;
    const char *sb = *(const char **) b;
    return strcmp(sa, sb);
}

/* sort the strings in a given list (using strcmp) */
void strlist_sort(strlist_t *list)
{
    if (list->count > 0)
    {
        qsort(list->items, (size_t) list->count, sizeof(void *), strlist_compare_strings);
    }
}

// fwd
static int listpath(const char *name, param *params);

static void mod2kind(const char *path, mode_t mode, char **out)
{
    switch (mode & S_IFMT)
    {
        case S_IFSOCK:
            *(*out)++ = '-';
            *(*out)++ = 's';
            break;
        case S_IFLNK:
            *(*out)++ = 'l';
            if (path == NULL)
            {
                *(*out)++ = '?';
            }
            else
            {
                struct stat s;
                stat(path, &s);
                char tmp[2];
                char *tmpp = tmp;
                mod2kind(NULL, s.st_mode, &tmpp);
                *(*out)++ = tmp[1];
            }
            break;
        case S_IFREG:
            *(*out)++ = '-';
            *(*out)++ = '-';
            break;
        case S_IFDIR:
            *(*out)++ = '-';
            *(*out)++ = 'd';
            break;
        case S_IFBLK:
            *(*out)++ = '-';
            *(*out)++ = 'b';
            break;
        case S_IFCHR:
            *(*out)++ = '-';
            *(*out)++ = 'c';
            break;
        case S_IFIFO:
            *(*out)++ = '-';
            *(*out)++ = 'p';
            break;
        default:
            *(*out)++ = '?';
            *(*out)++ = '?';
    }
}

void strmode(const char *path, mode_t mode, char *out)
{
    mod2kind(path, mode, &out);
    *out++ = (mode & 0400) ? 'r' : '-';
    *out++ = (mode & 0200) ? 'w' : '-';
    if (mode & 04000)
    {
        *out++ = (mode & 0100) ? 's' : 'S';
    }
    else
    {
        *out++ = (mode & 0100) ? 'x' : '-';
    }
    *out++ = (mode & 040) ? 'r' : '-';
    *out++ = (mode & 020) ? 'w' : '-';
    if (mode & 02000)
    {
        *out++ = (mode & 010) ? 's' : 'S';
    }
    else
    {
        *out++ = (mode & 010) ? 'x' : '-';
    }
    *out++ = (mode & 04) ? 'r' : '-';
    *out++ = (mode & 02) ? 'w' : '-';
    if (mode & 01000)
    {
        *out++ = (mode & 01) ? 't' : 'T';
    }
    else
    {
        *out++ = (mode & 01) ? 'x' : '-';
    }
    *out = 0;
}

static int listfile_long(const char *path, struct stat *s)
{
    char mode[18];
    const char *name;
    if (!s || !path)
    {
        return -1;
    }
    /* name is anything after the final '/', or the whole path if none*/
    name = strrchr(path, '/');
    if (name == 0)
    {
        name = path;
    }
    else
    {
        name++;
    }
    strmode(path, s->st_mode, mode);

// 12345678901234567890123456789012345678901234567890123456789012345678901234567890
// MMMMMMMM UUUUUUUU GGGGGGGGG XXXXXXXX YYYY-MM-DD HH:MM NAME (->LINK)
    switch (s->st_mode & S_IFMT)
    {
        case S_IFBLK:
        case S_IFCHR:
            printf("%s %3d, %3d %ld %s\n",
                   mode,
                   major(s->st_rdev), minor(s->st_rdev),
                   s->st_mtime, name);
            break;
        case S_IFREG:
            printf("%s %8lld %ld %s\n",
                   mode, (long long) s->st_size, s->st_mtime, name);
            break;
        case S_IFLNK:
        {
            char linkto[256];
            ssize_t len;
            len = readlink(path, linkto, 256);
            if (len < 0) return -1;
            if (len > 255)
            {
                linkto[252] = '.';
                linkto[253] = '.';
                linkto[254] = '.';
                linkto[255] = 0;
            }
            else
            {
                linkto[len] = 0;
            }
            printf("%s %8i %ld %s -> %s\n",
                   mode, 0, s->st_mtime, name, linkto);
            break;
        }
        default:
            printf("%s %8i %ld %s\n",
                   mode, 0, s->st_mtime, name);
    }
    return 0;
}

static int listfile(const char *dirname, const char *filename)
{
    struct stat s;
    char tmp[4096];
    const char *pathname = filename;
    if (dirname != NULL)
    {
        snprintf(tmp, sizeof(tmp), "%s/%s", dirname, filename);
        pathname = tmp;
    }
    else
    {
        pathname = filename;
    }
    if (lstat(pathname, &s) < 0)
    {
        fprintf(stderr, "lstat '%s' failed: %s\n", pathname, strerror(errno));
        return -1;
    }

    return listfile_long(pathname, &s);

}

static int checkHeader(const char *dirname, const char *filename, char *filter)
{
    char tmp[4096];
    const char *pathname = filename;
    if (dirname != NULL)
    {
        snprintf(tmp, sizeof(tmp), "%s/%s", dirname, filename);
        pathname = tmp;
    }
    else
    {
        pathname = filename;
    }
    int matches = 0;
    char buffer;
    char bytes[17];
    FILE *f;
    if ((f = fopen(pathname, "r")))
    {
        int count = 0;
        while(fread(&buffer, 1, 1, f) > 0 && count < 16)
        {
            bytes[count] = buffer;
            count++;
        }
        bytes[count] = '\0';

        matches = strcmp(bytes, filter) == 0;

        fclose(f);
    }
    return matches;
}

static int hasFlag(int flag, param *params)
{
    if(!params)
        return 0;
    size_t i = 0;
    for(i = 0; i < sizeof(*params) / sizeof(params[0]); i++)
    {
        if(flag == params[i].flag)
            return 1;
    }
    return 0;
}

static char *getValueForParamFlag(int flag, param *params)
{
    if(!params)
        return 0;
    size_t i = 0;
    for(i = 0; i < sizeof(*params) / sizeof(params[0]); i++)
    {
        if(flag == params[i].flag)
            return params[i].value;
    }
    return 0;
}

static int listdir(const char *name, param *params)
{
    DIR *d;
    struct dirent *de;
    strlist_t files = STRLIST_INITIALIZER;
    d = opendir(name);
    if (d == 0)
    {
        fprintf(stderr, "opendir failed, %s\n", strerror(errno));
        return -1;
    }
    while ((de = readdir(d)) != 0)
    {
        if (!strcmp(de->d_name, ".")/* || !strcmp(de->d_name, "..")*/) continue;
        if(hasFlag(LIST_FILTER_MIME_TYPE, params) && !checkHeader(name, de->d_name, getValueForParamFlag(LIST_FILTER_MIME_TYPE, params)))continue;
        strlist_append_dup(&files, de->d_name);
    }
    strlist_sort(&files);
    STRLIST_FOREACH(&files, filename, listfile(name, filename));
    strlist_done(&files);
    closedir(d);
    return 0;
}

static int listpath(const char *name, param *params)
{
    struct stat s;
    int err;
    /*
     * If the name ends in a '/', use stat() so we treat it like a
     * directory even if it's a symlink.
     */
    if (name[strlen(name) - 1] == '/')
        err = stat(name, &s);
    else
        err = lstat(name, &s);
    if (err < 0)
    {
        perror(name);
        return -1;
    }

    return listdir(name, params);
}

int main(int argc, char **argv)
{
    param *params = 0;
    if (argc > 1)
    {
        //param tmp[(argc - 1) / 2];
        param tmp[10];//TODO calculate exact size of this array
        params = tmp;
        int i;
        int err = 0;
        strlist_t files = STRLIST_INITIALIZER;
        for (i = 1; i < argc; i++)
        {
            if (argv[i][0] == '-')
            {
                /* an option ? */
                const char *arg = argv[i] + 1;
                switch (arg[0])
                {
                    case 'f':
                        params[i - 1].flag = LIST_FILTER_MIME_TYPE;
                        params[i - 1].value = argv[i + 1];
                        if(!params[i - 1].value)
                        {
                            fprintf(stderr, "%s: flag '-%c' requires value. Aborting.\n", "ls", 'f');
                            exit(1);
                        }
                        i++;
                        continue;
                    default:
                        fprintf(stderr, "%s: Unknown option '-%c'. Aborting.\n", "ls", arg[0]);
                        exit(1);
                }
            }
            else
            {
                /* not an option ? */
                strlist_append_dup(&files, argv[i]);
            }
        }
        if (files.count > 0)
        {
            STRLIST_FOREACH(&files, path, {
                if (listpath(path, params) != 0)
                {
                    err = EXIT_FAILURE;
                }
            });
            strlist_done(&files);
            return err;
        }
    }
    // list working directory if no files or directories were specified
    return listpath(".", params);
}
