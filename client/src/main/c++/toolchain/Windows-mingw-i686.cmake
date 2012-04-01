#
# NOTE: These are distro-dependent. Add your favorite
#       distro check & config parameters here.
#
IF ( EXISTS "/etc/fedora-release" )

    SET ( MINGW_PREFIX   "i686-pc-mingw32" )
    SET ( MINGW_SYSROOT  "/usr/${MINGW_PREFIX}/sys-root/mingw/" )
    SET ( Boost_COMPILER "-gcc45" )

ELSEIF ( EXISTS "/etc/debian_version" )

    set ( BOOST_ROOT "~/.cmake/mingw/$ENV{MINGW_ARCH}/" CACHE FILEPATH "" )
    set ( Boost_COMPILER "-mgw46" )

    IF ( EXISTS "/usr2323" ) # Put here "if debian natty"
      IF (CMAKE_SIZEOF_VOID_P MATCHES "8")
        SET ( MINGW_PREFIX   "amd64-mingw32msvc"     )
      ELSE ()
        SET ( MINGW_PREFIX   "i586-mingw32msvc"     )
      ENDIF ()
    ELSE()
      IF (CMAKE_SIZEOF_VOID_P MATCHES "8")
        SET ( MINGW_PREFIX   "x86_64-w64-mingw32"     )
      ELSE ()
        SET ( MINGW_PREFIX   "i686-w64-mingw32"     )
      ENDIF ()
    ENDIF ()

    SET ( MINGW_SYSROOT  "/usr/${MINGW_PREFIX};${BOOST_ROOT}" )
ELSE ()
    message ( FATAL_ERROR "Unknown host platform" )
ENDIF()



include(CMakeForceCompiler)

SET ( CMAKE_SYSTEM_NAME Windows )

ADD_DEFINITIONS ( "-D__CYGWIN__" )

SET ( CMAKE_C_COMPILER ${MINGW_PREFIX}-gcc )
SET ( CMAKE_FORCE_CXX_COMPILER ${MINGW_PREFIX}-g++ GNU )
SET ( CMAKE_RC_COMPILER ${MINGW_PREFIX}-windres )

SET ( CMAKE_FIND_ROOT_PATH ${MINGW_SYSROOT} )

SET ( CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER )
SET ( CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY )
SET ( CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY )


