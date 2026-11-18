/*
 * JNI bridge to rcheevos rhash. Files come from the Storage Access Framework as file descriptors,
 * so the iterator gets the real file name (rhash picks candidate consoles from the extension) and
 * a file reader that reads that descriptor. Other files a format may reference (for example the
 * tracks of a .cue) cannot be opened this way and make that format fail.
 */
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "rc_hash.h"

static __thread int current_fd = -1;
static __thread const char* current_name = NULL;

static void* RC_CCONV fd_open(const char* path) {
    if (current_fd < 0 || current_name == NULL || strcmp(path, current_name) != 0)
        return NULL;
    int copy = dup(current_fd);
    if (copy < 0)
        return NULL;
    FILE* file = fdopen(copy, "rb");
    if (file == NULL)
        close(copy);
    return file;
}

static void RC_CCONV fd_seek(void* handle, int64_t offset, int origin) {
    fseeko((FILE*)handle, (off_t)offset, origin);
}

static int64_t RC_CCONV fd_tell(void* handle) {
    return (int64_t)ftello((FILE*)handle);
}

static size_t RC_CCONV fd_read(void* handle, void* buffer, size_t bytes) {
    return fread(buffer, 1, bytes, (FILE*)handle);
}

static void RC_CCONV fd_close(void* handle) {
    fclose((FILE*)handle);
}

/* Returns "consoleId:hash" for every console rhash could hash the file for. */
JNIEXPORT jobjectArray JNICALL
Java_net_bonysoft_bonusstage_rom_RHash_nativeHash(JNIEnv* env, jclass clazz, jint fd, jstring name) {
    const char* file_name = (*env)->GetStringUTFChars(env, name, NULL);
    current_fd = fd;
    current_name = file_name;

    rc_hash_iterator_t iterator;
    rc_hash_initialize_iterator(&iterator, file_name, NULL, 0);
    iterator.callbacks.filereader.open = fd_open;
    iterator.callbacks.filereader.seek = fd_seek;
    iterator.callbacks.filereader.tell = fd_tell;
    iterator.callbacks.filereader.read = fd_read;
    iterator.callbacks.filereader.close = fd_close;

    char results[12][48];
    int count = 0;
    char hash[33];
    while (count < 12 && rc_hash_iterate(hash, &iterator)) {
        int console_id = iterator.consoles[iterator.index - 1];
        snprintf(results[count++], sizeof(results[0]), "%d:%s", console_id, hash);
    }
    rc_hash_destroy_iterator(&iterator);

    current_fd = -1;
    current_name = NULL;
    (*env)->ReleaseStringUTFChars(env, name, file_name);

    jobjectArray array = (*env)->NewObjectArray(env, count, (*env)->FindClass(env, "java/lang/String"), NULL);
    for (int i = 0; i < count; i++)
        (*env)->SetObjectArrayElement(env, array, i, (*env)->NewStringUTF(env, results[i]));
    return array;
}
