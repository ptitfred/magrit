#
# NOTE: These are distro-dependent. Add your favorite
#       distro check & config parameters here.
#
IF ( EXISTS "/etc/fedora-release" )
    SET ( MINGW_PREFIX   i686-pc-mingw32 )
    SET ( Boost_COMPILER -gcc45          )
ELSE ()
    message ( FATAL_ERROR "Unknown host platform" )
ENDIF()

include(CMakeForceCompiler)

SET ( CMAKE_SYSTEM_NAME Windows )

ADD_DEFINITIONS ( "-D__CYGWIN__" )

SET ( CMAKE_C_COMPILER ${MINGW_PREFIX}-gcc )
SET ( CMAKE_FORCE_CXX_COMPILER ${MINGW_PREFIX}-g++ GNU )
SET ( CMAKE_RC_COMPILER ${MINGW_PREFIX}-windres )

SET ( CMAKE_FIND_ROOT_PATH /usr/${MINGW_PREFIX}/sys-root/mingw/ )

SET ( CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER )
SET ( CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY )
SET ( CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY )
