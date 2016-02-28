#include <iostream>

using namespace std;

int main()
{
    int a = 5;
    int b = 6;
    a = b;
    b = 8;

    int *pa = &a;
    int *pb = &b;
    *pa = *pb;
    cout << a << endl;
    return 0;
}