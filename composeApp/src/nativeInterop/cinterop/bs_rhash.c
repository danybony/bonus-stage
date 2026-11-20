/* A small wrapper so Kotlin does not have to drive rc_hash_iterator_t itself. */
#include <string.h>

#include "bs_rhash.h"
#include "rc_hash.h"

int bs_rhash(const char* path, int* consoles, char* hashes, int max) {
    rc_hash_iterator_t iterator;
    rc_hash_initialize_iterator(&iterator, path, NULL, 0);
    int count = 0;
    char hash[33];
    while (count < max && rc_hash_iterate(hash, &iterator)) {
        consoles[count] = iterator.consoles[iterator.index - 1];
        memcpy(hashes + count * 32, hash, 32);
        count++;
    }
    rc_hash_destroy_iterator(&iterator);
    return count;
}
