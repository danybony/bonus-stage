#ifndef BS_RHASH_H
#define BS_RHASH_H

/*
 * Hashes the file at path with rcheevos rhash, for every console rhash considers from its name
 * and contents. Writes up to max console ids, and max 32-character hashes back to back (not
 * terminated) into hashes. Returns how many it wrote.
 */
int bs_rhash(const char* path, int* consoles, char* hashes, int max);

#endif
