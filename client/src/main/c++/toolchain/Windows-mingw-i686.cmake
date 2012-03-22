#
# NOTE: These are distro-dependent. Add your favorite
#       distro check & config parameters here.
#
IF ( EXISTS "/etc/fedora-release" )
    SET ( MINGW_PREFIX   /usr/i686-pc-mingw32/sys-root/mingw/ )
    SET ( Boost_COMPILER -gcc45 )
ELSEIF ( EXISTS "/etc/debian_version" )
    SET ( MINGW_PREFIX   /usr/i586-mingw32msvc/ )
ELSE ()
    message ( FATAL_ERROR "Unknown host platform" )
ENDIF()

include(CMakeForceCompiler)

SET ( CMAKE_SYSTEM_NAME Windows )

ADD_DEFINITIONS ( "-D__CYGWIN__" )

SET ( CMAKE_C_COMPILER ${MINGW_PREFIX}-gcc )
SET ( CMAKE_FORCE_CXX_COMPILER ${MINGW_PREFIX}-g++ GNU )
SET ( CMAKE_RC_COMPILER ${MINGW_PREFIX}-windres )

SET ( CMAKE_FIND_ROOT_PATH ${MINGW_PREFIX} )

SET ( CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER )
SET ( CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY )
SET ( CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY )

