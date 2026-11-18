# rhash from rcheevos (MIT, git submodule in third_party/rcheevos), plus the JNI bridge.
cmake_minimum_required(VERSION 3.22)
project(bonusstage_rhash C)

set(RCHEEVOS ${CMAKE_CURRENT_SOURCE_DIR}/../../../../third_party/rcheevos)

add_library(bonusstage_rhash SHARED
    rhash_jni.c
    ${RCHEEVOS}/src/rc_compat.c
    ${RCHEEVOS}/src/rhash/aes.c
    ${RCHEEVOS}/src/rhash/cdreader.c
    ${RCHEEVOS}/src/rhash/hash.c
    ${RCHEEVOS}/src/rhash/hash_disc.c
    ${RCHEEVOS}/src/rhash/hash_encrypted.c
    ${RCHEEVOS}/src/rhash/hash_rom.c
    ${RCHEEVOS}/src/rhash/hash_zip.c
    ${RCHEEVOS}/src/rhash/md5.c
)

target_include_directories(bonusstage_rhash PRIVATE ${RCHEEVOS}/include ${RCHEEVOS}/src)
target_compile_options(bonusstage_rhash PRIVATE -O2 -Wno-unused-parameter)
target_link_libraries(bonusstage_rhash log)
